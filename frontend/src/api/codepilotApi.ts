export type RepositoryStatus =
  | "QUEUED"
  | "CLONING"
  | "PARSING"
  | "INDEXING"
  | "READY"
  | "FAILED";

export type CodeRepoResponse = {
  id: string;
  name: string;
  url: string;
  status: RepositoryStatus;
  errorMessage: string | null;
  createdAt: string;
};

export type AnswerSource = {
  path: string;
  language: string;
};

export type FastApiResponse = {
  answer: string;
  sources: AnswerSource[];
  thread_id: string;
};

const API_BASE_URL =
  import.meta.env.VITE_CODEPILOT_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string, init: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {}),
    },
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function createRepository(url: string): Promise<CodeRepoResponse> {
  return request<CodeRepoResponse>("/api/v1/code/", {
    method: "POST",
    body: JSON.stringify({ url }),
  });
}

export function askRepository(
  repoId: string,
  question: string,
  threadId: string,
): Promise<FastApiResponse> {
  return request<FastApiResponse>("/api/v1/repositories/questions", {
    method: "POST",
    body: JSON.stringify({
      repo_id: repoId,
      question,
      thread_id: threadId,
    }),
  });
}
