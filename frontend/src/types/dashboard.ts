export type StatCard = {
  label: string;
  value: string;
  trend: string;
};

export type OrderRow = {
  id: string;
  orderId?: number;
  cargo: string;
  destination: string;
  weight: string;
  status: "Queued" | "Assigned" | "Ready";
};

export type TripRow = {
  id: string;
  tripId?: number;
  driver: string;
  car: string;
  status: "Completed" | "Repair Requested" | "In progress" | "Broken";
  payment: string;
};

export type DashboardData = {
  stats: StatCard[];
  orders: OrderRow[];
  trips: TripRow[];
  activity: string[];
};
