import client from "./client";
import { DashboardData } from "../types/dashboard";

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

export const triggerAction = async (action: string): Promise<void> => {
  await client.post("/actions", { action });
};
