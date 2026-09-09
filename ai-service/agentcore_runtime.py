from bedrock_agentcore.runtime import BedrockAgentCoreApp

from app.agent.graph import answer_with_agent
from app.schemas import RetrieveRequest2


app = BedrockAgentCoreApp()


@app.entrypoint
def agent_invocation(payload, context):
    repository_id = payload.get("repo_id")
    question = payload.get("question")
    thread_id = payload.get("thread_id") or context.session_id

    if not repository_id:
        return {"error": "repo_id is required"}

    if not question or not question.strip():
        return {"error": "question is required"}

    request = RetrieveRequest2(
        repo_id=repository_id,
        question=question,
        thread_id=thread_id,
    )

    return answer_with_agent(request)


if __name__ == "__main__":
    app.run()
