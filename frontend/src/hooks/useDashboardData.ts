import { useQuery } from "@tanstack/react-query";
import { fetchDashboard } from "../api/dashboardApi";
import { mockDashboard } from "../api/mockData";

export const useDashboardData = () => {
  return useQuery({
    queryKey: ["dashboard"],
    queryFn: fetchDashboard,
    staleTime: 30_000,
    placeholderData: mockDashboard
  });
};
