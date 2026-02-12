import { useQuery } from "@tanstack/react-query";
import { fetchDashboard } from "../api/dashboardApi";
import type { DashboardData } from "../types/dashboard";

const emptyDashboard: DashboardData = {
  stats: [],
  orders: [],
  trips: [],
  activity: [],
  driverPerformance: []
};

export const useDashboardData = () => {
  const query = useQuery({
    queryKey: ["dashboard"],
    queryFn: fetchDashboard,
    staleTime: 5_000,
    refetchOnWindowFocus: false
  });
  return {
    ...query,
    data: (query.data ?? emptyDashboard) as DashboardData
  };
};
