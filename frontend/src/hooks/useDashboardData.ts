import { useQuery } from "@tanstack/react-query";
import { fetchDashboard } from "../api/dashboardApi";
import { mockDashboard } from "../api/mockData";

export const useDashboardData = () => {
  return useQuery({
    queryKey: ["dashboard"],
    queryFn: async () => {
      try {
        return await fetchDashboard();
      } catch {
        return mockDashboard;
      }
    },
    staleTime: 30_000
  });
};
