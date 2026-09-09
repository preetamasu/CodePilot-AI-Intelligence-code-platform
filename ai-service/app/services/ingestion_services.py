from app.core.clients import *;
from fastapi import FastAPI,HTTPException
from pydantic import BaseModel
from langchain_text_splitters import Language, RecursiveCharacterTextSplitter
from langchain_core.documents import Document
import pickle
from pathlib import Path

CACHE_DIR = Path("emedding_cache")
CACHE_DIR.mkdir(exist_ok=True)

app = FastAPI(title="Codepilot AI service")




def get_repo_files(repo_id:str):

    response = (
        supabase
        .table("repo_file")
        .select("id,repo_file_id,path,language,content,size_bytes")
        .eq("repo_file_id",repo_id)
        .order("path")
        .execute()
    )
    return response.data


LANGUAGE_MAPPING = {
    "java": Language.JAVA,
    "python": Language.PYTHON,
    "javascript": Language.JS,
    "typescript": Language.TS,
    "c": Language.C,
    "c++": Language.CPP,
    "c#": Language.CSHARP,
    "go": Language.GO,
    "html": Language.HTML,
    "markdown": Language.MARKDOWN,  
}


def get_splitter(language: str):

    lang = language.lower()
    if lang in LANGUAGE_MAPPING:
        return RecursiveCharacterTextSplitter.from_language(
            language=LANGUAGE_MAPPING.get(lang),
            chunk_size = 1500,
            chunk_overlap=200,
            add_start_index=True,
        )
    return RecursiveCharacterTextSplitter(
        chunk_size=1500,
        chunk_overlap=200,
        add_start_index=True,
    )

def chunking(files):
    all_documents  =[]
    for file in files:
        # print(file)
        document = Document(
            page_content = file["content"],
            metadata = {
                "repository_id": file["repo_file_id"],
                "file_id": file["id"],
                "path": file["path"],
                "language": file["language"],
            }
        )
        
        text_splitter = get_splitter(file["language"])
        splitted_docs = text_splitter.split_documents([document])
        all_documents.extend(splitted_docs)

    return all_documents


def generateEmbedding(chunks,batch_size=20):
    all_embeddings = []
    for index in range(0,len(chunks),batch_size):
        batch = chunks[index:index+batch_size]

        contents = [
            chunk.page_content for chunk in batch

        ]
        embedding = bedrock_embeddings.embed_documents(contents)
        all_embeddings.extend(embedding)
    return all_embeddings

def save_embedding(repo_id,chunks,vectors):
    cache_file = CACHE_DIR / f"{repo_id}.pkl"

    with cache_file.open("wb+") as file:
        pickle.dump(
            {
                "chunks": chunks,
                "vectors": vectors
            },
            file
        )

    print("Saved")

def load_embedding(repo_id):
    cache_file = CACHE_DIR / f"{repo_id}.pkl"
    with cache_file.open("rb") as file:
        cached_data = pickle.load(file)

    return cached_data["chunks"],cached_data["vectors"]


def storingVectorDb(vectors,chunks):
   
    ids=[]
    embeddings=[]
    documents=[]
    metadatas=[]

    for index,(chunk,vector) in enumerate(zip(chunks,vectors)):
        ids.append(f"{chunk.metadata['file_id']}--{index}")
        embeddings.append(vector)
        documents.append(chunk.page_content)
        metadatas.append({
            "repository_id": chunk.metadata["repository_id"],
            "file_id": chunk.metadata["file_id"],
            "path": chunk.metadata["path"],
            "language": chunk.metadata["language"]
        }
        )

    

    batch_size = 250

    for start in range(0, len(ids), batch_size):
        end = start + batch_size

        batch_ids = ids[start:end]
        batch_embeddings = embeddings[start:end]
        batch_documents = documents[start:end]
        batch_metadatas = metadatas[start:end]

        print("Uploading batch:", len(batch_ids))

        collection.upsert(
            ids=batch_ids,
            embeddings=batch_embeddings,
            documents=batch_documents,
            metadatas=batch_metadatas
        )

    print("Total stored:", collection.count())

def index_repository(repo_id: str):
    files = get_repo_files(repo_id)

    chunks = chunking(files)

    vectors = generateEmbedding(chunks)

    save_embedding(
        repo_id,
        chunks,
        vectors
    )

    storingVectorDb(
        
        vectors,
        chunks
    )

    return {
        "repository_id": repo_id,
        "files_processed": len(files),
        "chunks_stored": len(chunks)
    }