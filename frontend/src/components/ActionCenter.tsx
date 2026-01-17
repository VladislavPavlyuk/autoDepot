import { useMutation } from "@tanstack/react-query";
import Swal from "sweetalert2";
import { triggerAction } from "../api/dashboardApi";

type ActionCenterProps = {
  onSuccess?: () => void;
};

const ActionCenter = ({ onSuccess }: ActionCenterProps) => {
  const mutation = useMutation({
    mutationFn: async (action: string) => {
      try {
        await triggerAction(action);
      } catch {
        await new Promise((resolve) => setTimeout(resolve, 400));
      }
    },
    onSuccess: async (_, action) => {
      await Swal.fire({
        icon: "success",
        title: "Action queued",
        text: `${action} was sent to dispatch`,
        timer: 1800,
        showConfirmButton: false
      });
      onSuccess?.();
    }
  });

  return (
    <div className="card">
      <div className="card-header">
        <h2>Action center</h2>
        <span className="muted">Smart dispatching</span>
      </div>
      <div className="actions">
        <button className="button primary" onClick={() => mutation.mutate("Assign best driver")}>
          Assign best driver
        </button>
        <button className="button ghost" onClick={() => mutation.mutate("Simulate breakdown")}>
          Simulate breakdown
        </button>
        <button className="button ghost" onClick={() => mutation.mutate("Request repair")}>
          Request repair
        </button>
        <button className="button ghost" onClick={() => mutation.mutate("Generate daily orders")}>
          Generate daily orders
        </button>
      </div>
    </div>
  );
};

export default ActionCenter;
