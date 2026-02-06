import axios from "axios";

const client = axios.create({
  baseURL: "/api",
  timeout: 5000
});

client.interceptors.response.use(
  (r) => r,
  (err) => {
    const data = err.response?.data;
    const msg =
      data != null && typeof data === "object" && "message" in data
        ? String((data as { message: unknown }).message)
        : typeof data === "string"
          ? data
          : err.message;
    (err as Error & { serverMessage?: string }).serverMessage = msg;
    return Promise.reject(err);
  }
);

export default client;
