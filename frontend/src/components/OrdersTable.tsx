import { useMutation } from "@tanstack/react-query";
import Swal from "sweetalert2";
import { assignTrip } from "../api/dashboardApi";
import { OrderRow } from "../types/dashboard";

type OrdersTableProps = {
  orders: OrderRow[];
  onSuccess?: () => void;
};

const OrdersTable = ({ orders, onSuccess }: OrdersTableProps) => {
  const assignMutation = useMutation({
    mutationFn: async (orderId: number) => {
      await assignTrip(orderId);
    },
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Driver assigned",
        text: "The order has been moved into an active trip.",
        timer: 1600,
        showConfirmButton: false
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: "Assignment failed",
        text: error instanceof Error ? error.message : "Unable to assign driver right now."
      });
    }
  });

  const handleAssign = async (order: OrderRow) => {
    if (!order.orderId) {
      await Swal.fire({
        icon: "warning",
        title: "Missing order id",
        text: "Reload the dashboard before assigning a driver."
      });
      return;
    }
    assignMutation.mutate(order.orderId);
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2>Orders queue</h2>
        <button className="button ghost">View all</button>
      </div>
      <div className="table">
        <div className="table-row head">
          <span>Order</span>
          <span>Cargo</span>
          <span>Destination</span>
          <span>Weight</span>
          <span>Status</span>
        </div>
        {orders.map((order) => (
          <div key={order.id} className="table-row">
            <span>{order.id}</span>
            <span>{order.cargo}</span>
            <span>{order.destination}</span>
            <span>{order.weight}</span>
            <div className="status-cell">
              <span className={`chip ${order.status.toLowerCase()}`}>{order.status}</span>
              <button
                className="button ghost tiny"
                onClick={() => handleAssign(order)}
                disabled={assignMutation.isPending}
              >
                Assign
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OrdersTable;
