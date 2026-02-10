import { DashboardData } from "../types/dashboard";

export const mockDashboard: DashboardData = {
  stats: [
    { label: "Active Trips", value: "12", trend: "+8%" },
    { label: "Orders Today", value: "34", trend: "+12%" },
    { label: "Fleet Health", value: "97%", trend: "+1%" },
    { label: "On-Time Rate", value: "92%", trend: "+3%" }
  ],
  orders: [
    { id: "ORD-2041", orderId: 2041, cargo: "FRAGILE", destination: "Berlin", weight: "1,436 kg", status: "QUEUED" },
    { id: "ORD-2042", orderId: 2042, cargo: "HAZARDOUS", destination: "Vienna", weight: "1,804 kg", status: "ASSIGNED" },
    { id: "ORD-2043", orderId: 2043, cargo: "STANDARD", destination: "Madrid", weight: "2,401 kg", status: "READY" }
  ],
  trips: [
    { id: "TR-011", tripId: 11, driver: "Dmytro Bondarenko", car: "C-05", status: "COMPLETED", payment: "€258,542.99" },
    { id: "TR-010", tripId: 10, driver: "Mykhailo Kovalenko", car: "C-04", status: "COMPLETED", payment: "€215,402.77" },
    { id: "TR-009", tripId: 9, driver: "Oleksandr Shevchenko", car: "C-01", status: "REPAIR_REQUESTED", payment: "€0.00" }
  ],
  activity: [
    "Trip TR-011 completed successfully",
    "Repair requested for TR-009",
    "Order ORD-2043 queued for dispatch"
  ],
  driverPerformance: [
    {
      driverName: "Ivan Petrenko",
      tripCount: 12,
      totalWeight: 18500,
      earnings: 124500,
      licenseCategories: ["B", "C"],
      experience: 12
    },
    {
      driverName: "Mykhailo Kovalenko",
      tripCount: 9,
      totalWeight: 13200,
      earnings: 102300,
      licenseCategories: ["B"],
      experience: 9
    },
    {
      driverName: "Oleksandr Shevchenko",
      tripCount: 6,
      totalWeight: 9100,
      earnings: 78400,
      licenseCategories: ["B", "D"],
      experience: 6
    }
  ]
};
