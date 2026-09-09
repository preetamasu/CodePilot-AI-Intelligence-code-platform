from fastapi import APIRouter
from app.schemas import RetrieveResponse,RetrieveRequest2
from app.services.rag_service import retrieveCode
from app.services.ingestion_services import get_repo_files,index_repository
from app.agent.graph import answer_with_agent

router = APIRouter(prefix="/api/v1/ai")


@router.post("/repositories/{repo_id}/index")
def start_indexing(repo_id: str):
    return index_repository(repo_id)

@router.post("/answer")
def answer(request:RetrieveRequest2):
    return answer_with_agent(request)

@router.get("/health")
def health():
    return {"status": "UP"}