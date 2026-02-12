import client from "./client";
import { DashboardData } from "../types/dashboard";

type CreateOrderPayload = {
  destination: string;
  cargoType: string;
  weight: number;
};

type CompleteTripPayload = {
  tripId: number;
  carStatus: string;
};

type CreateDriverPayload = {
  name: string;
  licenseYear: number;
  licenseCategories: string[];
};

const normalizeDashboard = (data?: Partial<DashboardData> | null): DashboardData => {
  return {
    stats: Array.isArray(data?.stats) ? data?.stats : [],
    orders: Array.isArray(data?.orders) ? data?.orders : [],
    trips: Array.isArray(data?.trips) ? data?.trips : [],
    activity: Array.isArray(data?.activity) ? data?.activity : [],
    driverPerformance: Array.isArray(data?.driverPerformance) ? data?.driverPerformance : []
  };
};

function isDashboardResponse(value: unknown): value is Record<string, unknown> {
  return value != null && typeof value === "object" && !Array.isArray(value);
}

export const fetchDashboard = async (): Promise<DashboardData> => {
  const response = await client.get<DashboardData>("/dashboard");
  const raw = response.data;
  if (!isDashboardResponse(raw)) {
    throw new Error("Invalid dashboard response");
  }
  return normalizeDashboard(raw);
};

export const createOrder = async (payload: CreateOrderPayload): Promise<void> => {
  await client.post("/orders", payload);
};

export const generateOrder = async (): Promise<void> => {
  await client.post("/orders/generate");
};

export const createDriver = async (payload: CreateDriverPayload): Promise<void> => {
  const body = {
    name: payload.name,
    licenseYear: Number(payload.licenseYear),
    licenseCategories: Array.isArray(payload.licenseCategories) ? payload.licenseCategories : []
  };
  await client.post("/drivers", body, {
    headers: { "Content-Type": "application/json" },
    maxBodyLength: 1024
  });
};

export const updateDriver = async (id: number, payload: CreateDriverPayload): Promise<void> => {
  const body = {
    name: payload.name,
    licenseYear: Number(payload.licenseYear),
    licenseCategories: Array.isArray(payload.licenseCategories) ? payload.licenseCategories : []
  };
  await client.patch(`/drivers/${id}`, body, {
    headers: { "Content-Type": "application/json" }
  });
};

export const assignTrip = async (orderId: number): Promise<void> => {
  await client.post("/trips/assign", { orderId });
};

export const completeTrip = async ({ tripId, carStatus }: CompleteTripPayload): Promise<void> => {
  await client.post(`/trips/${tripId}/complete`, { carStatus });
};

export const reportBreakdown = async (tripId: number): Promise<void> => {
  await client.post(`/trips/${tripId}/breakdown`);
};

export const requestRepair = async (tripId: number): Promise<void> => {
  await client.post(`/trips/${tripId}/repair`);
};

export const simulateBreakdown = async (): Promise<void> => {
  await client.post("/trips/simulate-breakdown");
};
