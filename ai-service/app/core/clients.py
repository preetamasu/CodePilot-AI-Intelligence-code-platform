from app.core.config import *;
from supabase import create_client, Client
import chromadb
from langchain_groq import ChatGroq
from langchain_community.embeddings import FastEmbedEmbeddings

supabase: Client = create_client(url,key)

chroma_client = chromadb.CloudClient(
    api_key= os.getenv("CHROMA_API_KEY"),
    tenant=os.getenv("CHROMA_TENANT"),
    database= os.getenv("CHROMA_DATABASE")
)

collection = chroma_client.get_or_create_collection(name="codepilot-fastembed",embedding_function=None)


llm = ChatGroq( 
          model=os.getenv("GROQ_MODEL", "openai/gpt-oss-120b"), 
          groq_api_key=os.getenv("GROQ_API_KEY"), 
          temperature=0, 
)

bedrock_embeddings = FastEmbedEmbeddings(
    model_name="BAAI/bge-small-en-v1.5"
)

