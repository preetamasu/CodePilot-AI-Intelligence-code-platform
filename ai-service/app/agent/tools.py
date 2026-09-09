from langchain.tools import tool, ToolRuntime
from app.services.rag_service import search_repo
from app.core.clients import supabase
@tool
def search_repository(query:str,runtime: ToolRuntime):
    '''
    Search the current repository relevant code
    '''
    repository_id = runtime.state["repository_id"]

    results = search_repo({
        "repo_id": repository_id,
        "question":query
    })



    documents = results["documents"][0]
    metadatas = results["metadatas"][0]
    if not documents:
        return {"message": "Couldn't find anything with that question"}
    context_parts = []

    for document,metadata in zip(documents,metadatas):
        context_parts.append(
            f"FILE: {metadata['path']}\n"
            f"LANGUAGE: {metadata['language']}\n"
            f"CODE:\n{document}"
        )

    return "\n\n---\n\n".join(context_parts)


@tool
def list_repo_files(runtime: ToolRuntime):
    """
    List all files in the current repository with their paths,
    languages, and sizes.

    Use this tool when the user asks about repository structure,
    available files, modules, folders, or technologies.
    """
    repo_id = runtime.state["repository_id"]

    response = (
        supabase
        .table("repo_file")
        .select("path,language,size_bytes")
        .eq("repo_file_id",repo_id)
        .order("path")
        .execute()
    )


    files = response.data
    print("Repository:", repo_id)
    print("Files found:", len(files))
    print(files)
    if not files:
        return "No files found for this repository"

    file_lines = []
    for file in files:
        file_lines.append(
            f"FILE: {file['path']}\n"
            f"LANGUAGE: {file['language']}\n"
            f"SIZE:\n{file['size_bytes']}"
        )
    return "\n".join(file_lines)

@tool
def get_repository_file(path:str,runtime: ToolRuntime)-> str:
    """
    Retrieve the complete content of one file from the current repository.

    Use this tool when the user asks about a specific file or when code-search
    results provide only a partial chunk and the complete implementation is
    needed. The path must exactly match a path returned by list_repo_files or
    search_repository.

    Do not use this tool to discover files. Use list_repo_files first when the
    correct path is unknown.
    """

    repo_id = runtime.state["repository_id"]
    response = (
            supabase
            .table("repo_file")
            .select("content,path")
            .eq("repo_file_id",repo_id)
            .eq("path",path)
            .order("path")
            .limit(1)
            .execute()
            
    )
    files = response.data

    if not files:
        return "No file with that path"
    content = []
    
    content.append(
        f"PATH: {files[0]}\n"
        f"CONTENT: {files[0]}"
    )
    return content




    
