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

export type ErrorsFilterPolicy = {
  defaultMaxAgeHours: number | null;
  allowedExceptionTypes: string[] | null;
  pageSize: number;
};

export const DEFAULT_ERRORS_POLICY: ErrorsFilterPolicy = {
  defaultMaxAgeHours: 24,
  allowedExceptionTypes: null,
  pageSize: 20
};
