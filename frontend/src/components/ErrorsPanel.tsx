import { useCallback, useEffect, useState } from "react";
import { fetchErrors, type ErrorsParams } from "../api/errorsApi";
import type { ErrorAuditEntry, ErrorsFilterPolicy } from "../types/errors";

type ErrorsPanelProps = {
  policy: ErrorsFilterPolicy;
  onClose: () => void;
};

function formatDate(isoOrMillis: string | number): string {
  if (typeof isoOrMillis === "number") {
    return new Date(isoOrMillis).toLocaleString();
  }
  const d = new Date(isoOrMillis);
  return Number.isNaN(d.getTime()) ? String(isoOrMillis) : d.toLocaleString();
}

export default function ErrorsPanel({ policy, onClose }: ErrorsPanelProps) {
  const [page, setPage] = useState(0);
  const [sinceEpoch, setSinceEpoch] = useState<number | null>(() =>
    policy.defaultMaxAgeHours != null
      ? Date.now() - policy.defaultMaxAgeHours * 60 * 60 * 1000
      : null
  );
  const [exceptionType, setExceptionType] = useState<string>("");
  const [data, setData] = useState<{
    content: ErrorAuditEntry[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(
    async (params: ErrorsParams) => {
      setLoading(true);
      setError(null);
      try {
        const result = await fetchErrors({
          page: params.page ?? 0,
          size: params.size ?? policy.pageSize,
          exceptionType: params.exceptionType ?? (exceptionType.trim() || null),
          sinceEpochMilli: params.sinceEpochMilli ?? sinceEpoch ?? undefined
        });
        setData(result);
      } catch (e: unknown) {
        const err = e as { response?: { status: number }; serverMessage?: string };
        if (err.response?.status === 401) {
          setError("Login required to view errors.");
        } else {
          setError(err.serverMessage ?? (e instanceof Error ? e.message : "Failed to load errors"));
        }
        setData(null);
      } finally {
        setLoading(false);
      }
    },
    [policy.pageSize, exceptionType, sinceEpoch]
  );

  useEffect(() => {
    load({ page, size: policy.pageSize, sinceEpochMilli: sinceEpoch ?? undefined, exceptionType: exceptionType.trim() || undefined });
  }, [page, sinceEpoch, load, policy.pageSize]);

  const applyFilters = () => {
    setPage(0);
    load({
      page: 0,
      size: policy.pageSize,
      sinceEpochMilli: sinceEpoch ?? undefined,
      exceptionType: exceptionType.trim() || undefined
    });
  };

  const rows = data?.content ?? [];

  return (
    <div className="errors-panel">
      <div className="errors-panel-header">
        <h2>Error audit log</h2>
        <button type="button" className="button ghost" onClick={onClose}>
          Close
        </button>
      </div>
      <div className="errors-filters">
        <label>
          Since (UTC ms)
          <input
            type="number"
            value={sinceEpoch ?? ""}
            onChange={(e) => setSinceEpoch(e.target.value ? Number(e.target.value) : null)}
            placeholder="optional"
          />
        </label>
        <label>
          Exception type
          <input
            type="text"
            value={exceptionType}
            onChange={(e) => setExceptionType(e.target.value)}
            placeholder="e.g. java.lang.IllegalStateException"
          />
        </label>
        <button type="button" className="button primary" onClick={applyFilters}>
          Apply
        </button>
      </div>
      {error && <div className="errors-panel-error">{error}</div>}
      {loading && <div className="errors-panel-loading">Loading…</div>}
      {!loading && data && (
        <>
          <div className="errors-meta">
            Total: {data.totalElements} · Page {data.number + 1} of {data.totalPages}
          </div>
          <div className="errors-table-wrap">
            <table className="errors-table">
              <thead>
                <tr>
                  <th>Id</th>
                  <th>Time</th>
                  <th>Thread</th>
                  <th>Location</th>
                  <th>Exception type</th>
                  <th>Message</th>
                </tr>
              </thead>
              <tbody>
                {rows.length === 0 ? (
                  <tr>
                    <td colSpan={6}>No entries</td>
                  </tr>
                ) : (
                  rows.map((row) => (
                    <tr key={row.id}>
                      <td>{row.id}</td>
                      <td>{formatDate(row.createdAt)}</td>
                      <td>{row.threadName ?? "—"}</td>
                      <td title={row.location ?? ""}>{row.location ? (row.location.length > 40 ? `${row.location.slice(0, 40)}…` : row.location) : "—"}</td>
                      <td title={row.exceptionType}>{row.exceptionType.length > 50 ? `${row.exceptionType.slice(0, 50)}…` : row.exceptionType}</td>
                      <td title={row.message ?? ""}>{row.message ? (row.message.length > 60 ? `${row.message.slice(0, 60)}…` : row.message) : "—"}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
          <div className="errors-pagination">
            <button
              type="button"
              className="button ghost"
              disabled={data.number <= 0}
              onClick={() => setPage((p) => p - 1)}
            >
              Previous
            </button>
            <span>
              Page {data.number + 1} of {data.totalPages}
            </span>
            <button
              type="button"
              className="button ghost"
              disabled={data.number >= data.totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}
