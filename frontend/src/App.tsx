import { useMemo, useState } from "react";
import {
  AlertCircle,
  Bot,
  CheckCircle2,
  Clock3,
  Code2,
  FileCode2,
  GitBranch,
  Github,
  Layers3,
  Loader2,
  MessageSquareText,
  Plus,
  SearchCode,
  Send,
  Sparkles,
  TerminalSquare,
} from "lucide-react";
import {
  askRepository,
  createRepository,
  type AnswerSource,
  type CodeRepoResponse,
} from "./api/codepilotApi";

type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  sources?: AnswerSource[];
};

const SUGGESTED_QUESTIONS = [
  "Explain the high-level architecture",
  "What are the main backend modules?",
  "Where does repository indexing happen?",
  "Show the important classes for AI questions",
];

const STATUS_STYLES: Record<string, string> = {
  READY: "border-emerald-200 bg-emerald-50 text-emerald-700",
  FAILED: "border-rose-200 bg-rose-50 text-rose-700",
  INDEXING: "border-amber-200 bg-amber-50 text-amber-800",
  PARSING: "border-amber-200 bg-amber-50 text-amber-800",
  CLONING: "border-amber-200 bg-amber-50 text-amber-800",
  QUEUED: "border-sky-200 bg-sky-50 text-sky-700",
  NOT_CONNECTED: "border-slate-200 bg-slate-100 text-slate-600",
};

function makeThreadId() {
  return `chat-${crypto.randomUUID()}`;
}

function formatStatus(status?: CodeRepoResponse["status"]) {
  return status ?? "NOT_CONNECTED";
}

function compactId(value: string) {
  return value.length > 18 ? `${value.slice(0, 18)}...` : value;
}

export default function App() {
  const [repoUrl, setRepoUrl] = useState("");
  const [repo, setRepo] = useState<CodeRepoResponse | null>(null);
  const [threadId, setThreadId] = useState(makeThreadId);
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isCreatingRepo, setIsCreatingRepo] = useState(false);
  const [isAsking, setIsAsking] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const latestSources = useMemo(() => {
    const assistantMessages = messages.filter(
      (message) => message.role === "assistant",
    );
    return assistantMessages[assistantMessages.length - 1]?.sources ?? [];
  }, [messages]);

  const assistantCount = messages.filter(
    (message) => message.role === "assistant",
  ).length;

  async function handleCreateRepo(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!repoUrl.trim()) {
      setError("Repository URL is required.");
      return;
    }

    setIsCreatingRepo(true);
    setError(null);

    try {
      const createdRepo = await createRepository(repoUrl.trim());
      setRepo(createdRepo);
      setThreadId(makeThreadId());
      setMessages([]);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Unable to create repository.",
      );
    } finally {
      setIsCreatingRepo(false);
    }
  }

  async function submitQuestion(nextQuestion: string) {
    if (!repo) {
      setError("Create or select a repository first.");
      return;
    }

    if (!nextQuestion.trim()) {
      setError("Question is required.");
      return;
    }

    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: "user",
      content: nextQuestion.trim(),
    };

    setMessages((current) => [...current, userMessage]);
    setQuestion("");
    setIsAsking(true);
    setError(null);

    try {
      const response = await askRepository(repo.id, userMessage.content, threadId);
      const assistantMessage: ChatMessage = {
        id: crypto.randomUUID(),
        role: "assistant",
        content: response.answer,
        sources: response.sources,
      };

      setMessages((current) => [...current, assistantMessage]);
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : "Unable to ask CodePilot.",
      );
    } finally {
      setIsAsking(false);
    }
  }

  function handleAsk(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void submitQuestion(question);
  }

  const status = formatStatus(repo?.status);

  return (
    <main className="min-h-screen bg-slate-950 text-slate-100">
      <div className="flex min-h-screen flex-col bg-[radial-gradient(circle_at_top_left,rgba(20,184,166,0.22),transparent_34%),linear-gradient(135deg,#020617_0%,#0f172a_42%,#111827_100%)]">
        <header className="flex h-16 shrink-0 items-center justify-between border-b border-white/10 bg-slate-950/70 px-6 backdrop-blur-xl">
          <div className="flex items-center gap-3">
            <div className="grid h-10 w-10 place-items-center rounded-lg bg-teal-400 text-slate-950 shadow-lg shadow-teal-950/40">
              <Code2 size={22} />
            </div>
            <div>
              <h1 className="text-lg font-semibold tracking-normal text-white">
                CodePilot
              </h1>
              <p className="text-xs font-medium text-slate-400">
                AI repository intelligence
              </p>
            </div>
          </div>

          <div className="hidden items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-slate-300 shadow-sm md:flex">
            <span className="h-2 w-2 rounded-full bg-emerald-400 shadow-[0_0_0_4px_rgba(52,211,153,0.12)]" />
            Spring Boot API
            <span className="text-slate-500">localhost:8080</span>
          </div>
        </header>

        <div className="grid min-h-0 flex-1 grid-cols-1 xl:grid-cols-[380px_minmax(0,1fr)_360px]">
          <aside className="border-b border-white/10 bg-slate-950/45 p-5 backdrop-blur-xl xl:border-b-0 xl:border-r">
            <section className="rounded-xl border border-white/10 bg-white/[0.06] p-4 shadow-2xl shadow-black/20">
              <div className="mb-4 flex items-center justify-between gap-3">
                <div className="flex items-center gap-2">
                  <Github size={18} className="text-teal-300" />
                  <h2 className="text-sm font-semibold text-white">
                    Repository Intake
                  </h2>
                </div>
                <span className="rounded-full border border-teal-300/20 bg-teal-300/10 px-2.5 py-1 text-[11px] font-semibold text-teal-200">
                  RAG Enabled
                </span>
              </div>

              <form className="space-y-3" onSubmit={handleCreateRepo}>
                <label
                  className="block text-xs font-semibold uppercase text-slate-400"
                  htmlFor="repo-url"
                >
                  GitHub repository URL
                </label>
                <div className="grid grid-cols-[minmax(0,1fr)_44px] gap-2">
                  <input
                    id="repo-url"
                    value={repoUrl}
                    onChange={(event) => setRepoUrl(event.target.value)}
                    placeholder="https://github.com/org/repository"
                    className="h-11 min-w-0 rounded-lg border border-white/10 bg-slate-950/70 px-3 text-sm text-white outline-none ring-0 transition placeholder:text-slate-600 focus:border-teal-300/60 focus:ring-4 focus:ring-teal-400/10"
                  />
                  <button
                    className="grid h-11 w-11 place-items-center rounded-lg bg-teal-300 text-slate-950 shadow-lg shadow-teal-950/30 transition hover:-translate-y-0.5 hover:bg-teal-200 disabled:hover:translate-y-0"
                    type="submit"
                    disabled={isCreatingRepo}
                    title="Add repository"
                  >
                    {isCreatingRepo ? (
                      <Loader2 className="animate-spin" size={18} />
                    ) : (
                      <Plus size={18} />
                    )}
                  </button>
                </div>
              </form>

              <div className="mt-5 rounded-lg border border-white/10 bg-slate-950/55 p-4">
                <div className="mb-3 flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-[11px] font-semibold uppercase text-slate-500">
                      Current repository
                    </p>
                    <h3 className="mt-1 truncate text-sm font-semibold text-white">
                      {repo?.name ?? "No repository selected"}
                    </h3>
                  </div>
                  <span
                    className={`shrink-0 rounded-full border px-2.5 py-1 text-[11px] font-bold ${STATUS_STYLES[status]}`}
                  >
                    {status}
                  </span>
                </div>
                <p className="break-all text-xs leading-5 text-slate-400">
                  {repo?.url ?? "Submit a repository URL to clone and index it."}
                </p>
              </div>
            </section>

            <section className="mt-4 grid grid-cols-3 gap-3">
              <div className="rounded-xl border border-white/10 bg-white/[0.05] p-3">
                <Layers3 size={17} className="mb-3 text-cyan-300" />
                <p className="text-[11px] font-medium text-slate-500">Answers</p>
                <strong className="text-lg text-white">{assistantCount}</strong>
              </div>
              <div className="rounded-xl border border-white/10 bg-white/[0.05] p-3">
                <FileCode2 size={17} className="mb-3 text-violet-300" />
                <p className="text-[11px] font-medium text-slate-500">Sources</p>
                <strong className="text-lg text-white">{latestSources.length}</strong>
              </div>
              <div className="rounded-xl border border-white/10 bg-white/[0.05] p-3">
                <Clock3 size={17} className="mb-3 text-amber-300" />
                <p className="text-[11px] font-medium text-slate-500">Session</p>
                <strong className="block truncate text-sm text-white">
                  {compactId(threadId)}
                </strong>
              </div>
            </section>

            <section className="mt-4 rounded-xl border border-white/10 bg-white/[0.05] p-4">
              <div className="mb-3 flex items-center gap-2">
                <Sparkles size={18} className="text-amber-300" />
                <h2 className="text-sm font-semibold text-white">Prompt Starters</h2>
              </div>
              <div className="grid gap-2">
                {SUGGESTED_QUESTIONS.map((prompt) => (
                  <button
                    key={prompt}
                    type="button"
                    disabled={!repo || isAsking}
                    onClick={() => void submitQuestion(prompt)}
                    className="rounded-lg border border-white/10 bg-slate-950/45 px-3 py-2.5 text-left text-sm text-slate-300 transition hover:border-teal-300/40 hover:bg-teal-300/10 hover:text-white disabled:hover:border-white/10 disabled:hover:bg-slate-950/45 disabled:hover:text-slate-300"
                  >
                    {prompt}
                  </button>
                ))}
              </div>
            </section>
          </aside>

          <section className="flex min-h-[680px] min-w-0 flex-col border-b border-white/10 bg-slate-900/30 xl:border-b-0">
            <div className="flex items-start justify-between gap-4 border-b border-white/10 px-5 py-4">
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <MessageSquareText size={18} className="text-teal-300" />
                  <h2 className="text-base font-semibold text-white">
                    Repository Chat
                  </h2>
                </div>
                <p className="mt-1 truncate text-sm text-slate-400">
                  {repo
                    ? `Connected to ${repo.name}`
                    : "Add a repository to begin analysis."}
                </p>
              </div>
              <div className="hidden rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium text-slate-400 lg:block">
                {threadId}
              </div>
            </div>

            {error ? (
              <div className="mx-5 mt-4 flex items-center gap-2 rounded-lg border border-rose-400/20 bg-rose-400/10 px-3 py-2.5 text-sm text-rose-100">
                <AlertCircle size={17} />
                <span>{error}</span>
              </div>
            ) : null}

            <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-auto px-5 py-5">
              {messages.length === 0 ? (
                <div className="grid h-full place-items-center">
                  <div className="max-w-xl rounded-2xl border border-white/10 bg-white/[0.06] p-8 text-center shadow-2xl shadow-black/20">
                    <div className="mx-auto grid h-16 w-16 place-items-center rounded-xl bg-teal-300 text-slate-950 shadow-lg shadow-teal-950/30">
                      <SearchCode size={32} />
                    </div>
                    <h3 className="mt-5 text-xl font-semibold text-white">
                      Ask precise questions about any indexed repository
                    </h3>
                    <p className="mt-3 text-sm leading-6 text-slate-400">
                      CodePilot searches stored source files, uses LangGraph tools,
                      and returns answers with evidence paths from the repository.
                    </p>
                  </div>
                </div>
              ) : (
                messages.map((message) => (
                  <article
                    className={`max-w-[880px] rounded-xl border px-4 py-3 shadow-xl shadow-black/10 ${
                      message.role === "user"
                        ? "ml-auto border-teal-300/20 bg-teal-300 text-slate-950"
                        : "mr-auto border-white/10 bg-white/[0.07] text-slate-100"
                    }`}
                    key={message.id}
                  >
                    <div
                      className={`mb-2 flex items-center gap-2 text-xs font-bold uppercase ${
                        message.role === "user"
                          ? "text-teal-950/70"
                          : "text-teal-300"
                      }`}
                    >
                      {message.role === "user" ? (
                        <TerminalSquare size={15} />
                      ) : (
                        <Bot size={15} />
                      )}
                      <span>{message.role === "user" ? "You" : "CodePilot"}</span>
                    </div>
                    <p className="whitespace-pre-wrap text-sm leading-6">
                      {message.content}
                    </p>
                  </article>
                ))
              )}

              {isAsking ? (
                <article className="mr-auto max-w-[620px] rounded-xl border border-white/10 bg-white/[0.07] px-4 py-3 text-slate-100 shadow-xl shadow-black/10">
                  <div className="mb-2 flex items-center gap-2 text-xs font-bold uppercase text-teal-300">
                    <Loader2 className="animate-spin" size={15} />
                    <span>CodePilot</span>
                  </div>
                  <p className="text-sm leading-6 text-slate-300">
                    Analyzing repository context and tool results...
                  </p>
                </article>
              ) : null}
            </div>

            <form className="border-t border-white/10 p-5" onSubmit={handleAsk}>
              <div className="grid grid-cols-[minmax(0,1fr)_48px] gap-3 rounded-xl border border-white/10 bg-slate-950/70 p-2 shadow-2xl shadow-black/20">
                <input
                  value={question}
                  onChange={(event) => setQuestion(event.target.value)}
                  placeholder="Ask about controllers, services, indexing, tests, or a specific file"
                  disabled={!repo || isAsking}
                  className="h-12 min-w-0 rounded-lg border-0 bg-transparent px-3 text-sm text-white outline-none placeholder:text-slate-600"
                />
                <button
                  className="grid h-12 w-12 place-items-center rounded-lg bg-teal-300 text-slate-950 transition hover:-translate-y-0.5 hover:bg-teal-200 disabled:hover:translate-y-0"
                  type="submit"
                  disabled={!repo || isAsking}
                  title="Send question"
                >
                  {isAsking ? (
                    <Loader2 className="animate-spin" size={18} />
                  ) : (
                    <Send size={18} />
                  )}
                </button>
              </div>
            </form>
          </section>

          <aside className="bg-slate-950/55 p-5 backdrop-blur-xl">
            <div className="mb-4 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <GitBranch size={18} className="text-teal-300" />
                <h2 className="text-sm font-semibold text-white">Evidence</h2>
              </div>
              {latestSources.length > 0 ? (
                <CheckCircle2 size={18} className="text-emerald-300" />
              ) : null}
            </div>

            {latestSources.length === 0 ? (
              <div className="rounded-xl border border-dashed border-white/15 bg-white/[0.04] p-6 text-center">
                <FileCode2 className="mx-auto text-slate-500" size={30} />
                <p className="mt-3 text-sm leading-6 text-slate-400">
                  Source files from the latest answer will appear here.
                </p>
              </div>
            ) : (
              <ul className="grid gap-3">
                {latestSources.map((source, index) => (
                  <li
                    className="grid grid-cols-[34px_minmax(0,1fr)] gap-3 rounded-xl border border-white/10 bg-white/[0.06] p-3 shadow-xl shadow-black/10"
                    key={`${source.path}-${source.language}`}
                  >
                    <span className="grid h-8 w-8 place-items-center rounded-lg bg-teal-300/15 text-xs font-bold text-teal-200">
                      {index + 1}
                    </span>
                    <div className="min-w-0">
                      <strong className="block break-words text-sm font-semibold leading-5 text-white">
                        {source.path}
                      </strong>
                      <span className="mt-2 inline-flex rounded-full border border-white/10 bg-slate-950/60 px-2 py-1 text-[11px] font-semibold text-slate-300">
                        {source.language}
                      </span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </aside>
        </div>
      </div>
    </main>
  );
}
