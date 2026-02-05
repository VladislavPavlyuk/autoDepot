import { useMutation } from "@tanstack/react-query";
import Swal from "sweetalert2";
import { assignTrip, generateOrder, requestRepair, simulateBreakdown } from "../api/dashboardApi";
import { OrderRow, TripRow } from "../types/dashboard";

type ActionCenterProps = {
  orders: OrderRow[];
  trips: TripRow[];
  onSuccess?: () => void;
};

const ActionCenter = ({ orders, trips, onSuccess }: ActionCenterProps) => {
  const assignMutation = useMutation({
    mutationFn: async (orderId: number) => {
      await assignTrip(orderId);
    },
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Driver assigned",
        text: "Dispatch started for the next pending order.",
        timer: 1800,
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

  const simulateMutation = useMutation({
    mutationFn: async () => {
      await simulateBreakdown();
    },
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Breakdown simulated",
        text: "A random active trip was marked as broken.",
        timer: 1800,
        showConfirmButton: false
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: "Simulation failed",
        text: error instanceof Error ? error.message : "Unable to simulate breakdown."
      });
    }
  });

  const repairMutation = useMutation({
    mutationFn: async (tripId: number) => {
      await requestRepair(tripId);
    },
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Repair requested",
        text: "The selected trip has been queued for repair.",
        timer: 1800,
        showConfirmButton: false
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: "Repair failed",
        text: error instanceof Error ? error.message : "Unable to request repair."
      });
    }
  });

  const generateMutation = useMutation({
    mutationFn: async () => {
      await generateOrder();
    },
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Orders generated",
        text: "A new order was added to the queue.",
        timer: 1800,
        showConfirmButton: false
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: "Generation failed",
        text: error instanceof Error ? error.message : "Unable to generate orders."
      });
    }
  });

  const handleAssign = async () => {
    const order = orders[0];
    if (!order?.orderId) {
      await Swal.fire({
        icon: "warning",
        title: "No pending orders",
        text: "Create or generate an order before assigning a driver."
      });
      return;
    }
    assignMutation.mutate(order.orderId);
  };

  const handleRepair = async () => {
    const brokenTrip = trips.find((trip) => trip.status === "Broken");
    if (!brokenTrip?.tripId) {
      await Swal.fire({
        icon: "warning",
        title: "No broken trips",
        text: "Simulate a breakdown first or wait for a breakdown event."
      });
      return;
    }
    repairMutation.mutate(brokenTrip.tripId);
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2>Action center</h2>
        <span className="muted">Smart dispatching</span>
      </div>
      <div className="actions">
        <button className="button primary" onClick={handleAssign}>
          Assign best driver
        </button>
        <button className="button ghost" onClick={() => simulateMutation.mutate()}>
          Simulate breakdown
        </button>
        <button className="button ghost" onClick={handleRepair}>
          Request repair
        </button>
        <button className="button ghost" onClick={() => generateMutation.mutate()}>
          Generate daily orders
        </button>
      </div>
    </div>
  );
};

export default ActionCenter;
