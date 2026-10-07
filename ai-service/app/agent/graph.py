from langgraph.graph import StateGraph, START,END
from app.core.clients import llm
from app.services.rag_service import search_repo
from app.agent.tools import search_repository,list_repo_files,get_repository_file
from langgraph.checkpoint.memory import MemorySaver
from app.agent.state import CodePilotState
from langgraph.prebuilt import ToolNode, tools_condition
from app.schemas import RetrieveRequest2
from langchain_core.messages import HumanMessage, ToolMessage


memory = MemorySaver()
tools = [search_repository,list_repo_files,get_repository_file]

SYSTEM_PROMPT = """
You are CodePilot, an AI assistant for explaining software repositories.

Use the repository tools only when needed.
Do not call the same tool repeatedly.
For architecture or overview questions, usually call list_repo_files once, then answer.
For specific implementation questions, use search_repository or get_repository_file as needed.
After you have enough evidence, write the final answer instead of calling more tools.
Use at most 2 tool calls unless the user explicitly asks for deeper inspection.
"""

llm_with_tools = llm.bind_tools(tools)

def chatbot(state: CodePilotState):

    tool_results_used = sum(
        1 for message in state["messages"]
        if isinstance(message, ToolMessage)
    )

    if tool_results_used >= 2:
        response = llm.invoke(
            state["messages"] + [
                HumanMessage(
                    content=(
                        "Use the repository tool results already provided and write the final answer now. "
                        "Do not call any more tools."
                    )
                )
            ]
        )
    else:
        response = llm_with_tools.invoke(state["messages"])

    return {
        "messages": [response]
    }



builder = StateGraph(CodePilotState)

builder.add_node("chatbot_node",chatbot)
builder.add_node("tools",ToolNode(tools))


builder.add_edge(START,"chatbot_node")
builder.add_conditional_edges("chatbot_node",tools_condition)
builder.add_edge("tools","chatbot_node")
builder.add_edge("chatbot_node",END)

graph = builder.compile(checkpointer=memory)


def answer_with_agent(request: RetrieveRequest2):

    config = {
        'configurable':{
            "thread_id": request.thread_id 
        },
        "recursion_limit": 10
    }
    result = graph.invoke(
        {
            "repository_id":request.repo_id,
            "messages": [
                {"role":"system","content":SYSTEM_PROMPT},
                {"role":"user","content":request.question}
            ]
        },
        config=config
    )

    sources_results = search_repo(
        {
            "repo_id":request.repo_id,
            "question": request.question
        }
    )

    sources = []

    metadatas = sources_results["metadatas"][0]

    seen_paths = set()
    for metadata in metadatas:

        if metadata["path"] not in seen_paths:
            sources.append({
                "path":metadata["path"],
                "language": metadata["language"]
            })

            seen_paths.add(metadata["path"])

    return {
        "answer": result["messages"][-1].content,
        "sources":sources,
        "thread_id": request.thread_id

    }


# config1 = {
#     'configurable':{
#         "thread_id":"1"
#     }
# }


# msg = "Explain the repository structure"
# message = {"role":"user","content":msg}
# result = graph.invoke(
#     {
#         "repository_id":"0b284fdd-81ea-435e-9477-9ef2d4ed7031",

#         "messages":[message]
#     },
#     config=config1
# )

# config2 = {
#     'configurable':{
#         "thread_id":"2"
#     }
# }

# msg =   "Use the repository file listing tool and explain the high-level project structure. Do not guess files."
# message = {"role":"user","content":msg}
# result = graph.invoke(
#     {
#         "repository_id":"5396f2d3-1aff-4b30-9ee5-03b316b7e898",

#         "messages":[message]
#     },
#     config=config2
# )


# config3 = {
#     'configurable':{
#         "thread_id": "structure-test-2"
#     }
# }

# msg =   "Use the repository file listing tool and explain the high-level project structure. Do not guess files."
# message = {"role":"user","content":msg}
# result = graph.invoke(
#     {
#         "repository_id":"0b284fdd-81ea-435e-9477-9ef2d4ed7031",

#         "messages":[message]
#     },
#     config=config3
# )



# config5 = {
#     'configurable':{
#         "thread_id":"5"
#     }
# }

# config6 = {
#     'configurable':{
#         "thread_id":"test"
#     }
# }

# msg =   "Retrieve the complete file at src/test/java/com/example/resume/screeningresult/ScreeningResultServiceTest.java and explain what it does. Use the get_repository_file tool and do not guess any missing details."
# message = {"role":"user","content":msg}
# result = graph.invoke(
#     {
#         "repository_id":"0b284fdd-81ea-435e-9477-9ef2d4ed7031",

#         "messages":[message]
#     },
#     config=config6
# )

# print(result["messages"][-1].content)
