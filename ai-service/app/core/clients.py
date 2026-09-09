from app.core.config import *;
from supabase import create_client, Client
import chromadb
from langchain_aws import ChatBedrockConverse
from langchain_aws import BedrockEmbeddings

supabase: Client = create_client(url,key)

chroma_client = chromadb.CloudClient(
    api_key= os.getenv("CHROMA_API_KEY"),
    tenant=os.getenv("CHROMA_TENANT"),
    database= os.getenv("CHROMA_DATABASE")
)

collection = chroma_client.get_or_create_collection(name="codepilot",embedding_function=None)


llm = ChatBedrockConverse(
    model_id = os.getenv("BEDROCK_MODEL"),
    region_name = os.getenv("BEDROCK_REGION"),
    temperature = 0,
    max_tokens = 1500

)

bedrock_embeddings = BedrockEmbeddings(
    model_id="amazon.nova-2-multimodal-embeddings-v1:0",
    region_name="us-east-1"
)

