from pydantic import BaseModel

class RetrieveRequest(BaseModel):
    repo_id: str
    question: str

class RetrieveRequest2(BaseModel):
    repo_id: str
    question: str
    thread_id: str

class RetrieveResponse(BaseModel):
    answer: str
    sources: list[str]

class RetrieveRequest1(BaseModel):
    repo_id: str