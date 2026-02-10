import { useQuery } from "@tanstack/react-query";
import { fetchDashboard } from "../api/dashboardApi";
import { mockDashboard } from "../api/mockData";

export const useDashboardData = () => {
  const query = useQuery({
    queryKey: ["dashboard"],
    queryFn: fetchDashboard,
    staleTime: 30_000,
    initialData: mockDashboard,
    refetchOnWindowFocus: false
  });
  return {
    ...query,
    data: query.data ?? mockDashboard
  };
};
