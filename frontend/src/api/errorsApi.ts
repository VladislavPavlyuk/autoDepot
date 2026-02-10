import client from "./client";
import type { ErrorsPage } from "../types/errors";

export type ErrorsParams = {
  page?: number;
  size?: number;
  exceptionType?: string | null;
  sinceEpochMilli?: number | null;
};

export async function fetchErrors(params: ErrorsParams = {}): Promise<ErrorsPage> {
  const search = new URLSearchParams();
  if (params.page != null) search.set("page", String(params.page));
  if (params.size != null) search.set("size", String(params.size));
  if (params.exceptionType != null && params.exceptionType !== "") {
    search.set("exceptionType", params.exceptionType);
  }
  if (params.sinceEpochMilli != null) {
    search.set("sinceEpochMilli", String(params.sinceEpochMilli));
  }
  const qs = search.toString();
  const url = `/errors${qs ? `?${qs}` : ""}`;
  const response = await client.get<ErrorsPage>(url);
  return response.data;
}
