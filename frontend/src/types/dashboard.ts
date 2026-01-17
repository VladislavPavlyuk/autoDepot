export type StatCard = {
  label: string;
  value: string;
  trend: string;
};

export type OrderRow = {
  id: string;
  cargo: string;
  destination: string;
  weight: string;
  status: "Queued" | "Assigned" | "Ready";
};

export type TripRow = {
  id: string;
  driver: string;
  car: string;
  status: "Completed" | "Repair Requested";
  payment: string;
};

export type DashboardData = {
  stats: StatCard[];
  orders: OrderRow[];
  trips: TripRow[];
  activity: string[];
};
