import { DashboardData } from "../types/dashboard";

export const mockDashboard: DashboardData = {
  stats: [
    { label: "Active Trips", value: "12", trend: "+8%" },
    { label: "Orders Today", value: "34", trend: "+12%" },
    { label: "Fleet Health", value: "97%", trend: "+1%" },
    { label: "On-Time Rate", value: "92%", trend: "+3%" }
  ],
  orders: [
    { id: "ORD-2041", cargo: "Fragile", destination: "New York", weight: "1,436 kg", status: "Queued" },
    { id: "ORD-2042", cargo: "Hazardous", destination: "Philadelphia", weight: "1,804 kg", status: "Assigned" },
    { id: "ORD-2043", cargo: "Standard", destination: "Chicago", weight: "2,401 kg", status: "Ready" }
  ],
  trips: [
    { id: "TR-011", driver: "James Davis", car: "C-05", status: "Completed", payment: "$258,542.99" },
    { id: "TR-010", driver: "Michael Johnson", car: "C-04", status: "Completed", payment: "$215,402.77" },
    { id: "TR-009", driver: "David Williams", car: "C-01", status: "Repair Requested", payment: "$0.00" }
  ],
  activity: [
    "Trip TR-011 completed successfully",
    "Repair requested for TR-009",
    "Order ORD-2043 queued for dispatch"
  ]
};
