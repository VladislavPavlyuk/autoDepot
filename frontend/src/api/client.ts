import axios from "axios";

const client = axios.create({
  baseURL: "/api",
  timeout: 15_000,
  withCredentials: true
});

function getMessageFromResponse(data: unknown): string | null {
  if (data == null) return null;
  if (typeof data === "object" && "message" in data) {
    return String((data as { message: unknown }).message);
  }
  if (typeof data === "string") {
    try {
      const parsed = JSON.parse(data) as { message?: unknown };
      if (parsed && typeof parsed.message === "string") return parsed.message;
    } catch {
      // ignore
    }
    return data;
  }
  return null;
}

client.interceptors.response.use(
  (r) => r,
  (err) => {
    const msg = getMessageFromResponse(err.response?.data) ?? err.message;
    (err as Error & { serverMessage?: string }).serverMessage = msg;
    if (err.response?.status === 401) {
      window.location.href = "/login";
      return new Promise(() => {});
    }
    return Promise.reject(err);
  }
);

export default client;
