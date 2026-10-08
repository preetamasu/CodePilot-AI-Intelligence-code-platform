from app.core.config import *;
from supabase import create_client, Client
import chromadb
from langchain_groq import ChatGroq
from langchain_google_genai import GoogleGenerativeAIEmbeddings

supabase: Client = create_client(url,key)

chroma_client = chromadb.CloudClient(
    api_key= os.getenv("CHROMA_API_KEY"),
    tenant=os.getenv("CHROMA_TENANT"),
    database= os.getenv("CHROMA_DATABASE")
)

collection = chroma_client.get_or_create_collection(name="codepilot-google-embed",embedding_function=None)


llm = ChatGroq(
          model=os.getenv("GROQ_MODEL", "openai/gpt-oss-120b"),
          groq_api_key=os.getenv("GROQ_API_KEY"),
          temperature=0,
)

bedrock_embeddings = GoogleGenerativeAIEmbeddings(
    model="gemini-embedding-2",
    google_api_key=os.getenv("GOOGLE_API_KEY")
)

