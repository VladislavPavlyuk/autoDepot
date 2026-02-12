export type StatCard = {
  label: string;
  value: string;
  trend: string;
};

export type DriverStat = {
  driverId?: number;
  driverName: string;
  tripCount: number;
  totalWeight: number;
  earnings: number;
  licenseCategories: string[];
  experience: number;
  licenseYear?: number;
};

export type OrderStatus = "QUEUED" | "ASSIGNED" | "READY";

export type OrderRow = {
  id: string;
  orderId?: number;
  cargo: string;
  destination: string;
  weight: string;
  status: OrderStatus;
};

export type TripStatus = "IN_PROGRESS" | "BROKEN" | "REPAIR_REQUESTED" | "COMPLETED";

export type TripRow = {
  id: string;
  tripId?: number;
  driver: string;
  car: string;
  status: TripStatus;
  payment: string;
};

export type DashboardData = {
  stats: StatCard[];
  orders: OrderRow[];
  trips: TripRow[];
  activity: string[];
  driverPerformance?: DriverStat[];
};
