from fastapi import FastAPI
from app.api.retrieval import router as retrieval_router

app = FastAPI(title="CodePilot AI Service")

app.include_router(retrieval_router)


