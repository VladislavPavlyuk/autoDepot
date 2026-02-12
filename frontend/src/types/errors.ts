export type ErrorAuditEntry = {
  id: number;
  createdAt: string | number;
  threadName: string | null;
  location: string | null;
  exceptionType: string;
  message: string | null;
};

export type ErrorsPage = {
  content: ErrorAuditEntry[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

/** Policy for which errors to show by default (filter presets). */
export type ErrorsFilterPolicy = {
  /** Default "since" in hours; null = no default time filter. */
  defaultMaxAgeHours: number | null;
  /** Allowed exception types for filter dropdown; null = allow any (from API). */
  allowedExceptionTypes: string[] | null;
  /** Page size for list. */
  pageSize: number;
};

export const DEFAULT_ERRORS_POLICY: ErrorsFilterPolicy = {
  defaultMaxAgeHours: 24,
  allowedExceptionTypes: null,
  pageSize: 20
};
