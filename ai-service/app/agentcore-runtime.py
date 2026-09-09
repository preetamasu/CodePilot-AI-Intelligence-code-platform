from bedrock_agentcore.runtime import BedrockAgentCoreApp
from app.schemas import RetrieveRequest2
from app.agent.graph import answer_with_agent

app = BedrockAgentCoreApp()

@app.entrypoint
def agent_invocation(payload,context):
    request = RetrieveRequest2(
        repo_id= payload.get("repo_id"),
        question = payload.get("question"),
        thread_id = payload.get("thread_id") or context.session_id
    )

    return answer_with_agent(request)

if __name__ == "__main__":
      app.run()