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

const normalizeDashboard = (data?: Partial<DashboardData> | null): DashboardData => {
  return {
    stats: Array.isArray(data?.stats) ? data?.stats : [],
    orders: Array.isArray(data?.orders) ? data?.orders : [],
    trips: Array.isArray(data?.trips) ? data?.trips : [],
    activity: Array.isArray(data?.activity) ? data?.activity : []
  };
};

export const fetchDashboard = async (): Promise<DashboardData> => {
  const response = await client.get<DashboardData>("/dashboard");
  return normalizeDashboard(response.data);
};

export const createOrder = async (payload: CreateOrderPayload): Promise<void> => {
  await client.post("/orders", payload);
};

export const generateOrder = async (): Promise<void> => {
  await client.post("/orders/generate");
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
