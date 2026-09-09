import os

from dotenv import load_dotenv

load_dotenv()

url = os.getenv("SUPABASE_URL")
key = os.getenv("SUPABASE_KEY")

api_key= os.getenv("CHROMA_API_KEY"),
tenant=os.getenv("CHROMA_TENANT"),
database= os.getenv("CHROMA_DATABASE")

model_id = os.getenv("BEDROCK_MODEL")
region_name = os.getenv("BEDROCK_REGION")