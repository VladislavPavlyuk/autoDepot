import { useMutation } from "@tanstack/react-query";
import Swal from "sweetalert2";
import { completeTrip, reportBreakdown, requestRepair } from "../api/dashboardApi";
import { TripRow } from "../types/dashboard";

type TripsTableProps = {
  trips: TripRow[];
  onSuccess?: () => void;
};

const TripsTable = ({ trips, onSuccess }: TripsTableProps) => {
  const completeMutation = useMutation({
    mutationFn: completeTrip,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Trip completed",
        text: "Driver earnings and car status were updated.",
        timer: 1600,
        showConfirmButton: false
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: "Completion failed",
        text: error instanceof Error ? error.message : "Unable to complete trip right now."
      });
    }
  });

  const breakdownMutation = useMutation({
    mutationFn: reportBreakdown,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Breakdown reported",
        text: "Trip status updated to broken.",
        timer: 1600,
        showConfirmButton: false
      });
      onSuccess?.();
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: "Breakdown failed",
        text: error instanceof Error ? error.message : "Unable to report breakdown."
      });
    }
  });

  const repairMutation = useMutation({
    mutationFn: requestRepair,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: "Repair requested",
        text: "The trip is now awaiting repairs.",
        timer: 1600,
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

  const handleComplete = async (trip: TripRow) => {
    if (!trip.tripId) {
      await Swal.fire({
        icon: "warning",
        title: "Missing trip id",
        text: "Reload the dashboard before completing this trip."
      });
      return;
    }
    const result = await Swal.fire({
      title: "Car status after trip",
      input: "select",
      inputOptions: {
        OK: "OK",
        BROKEN: "BROKEN"
      },
      inputValue: "OK",
      showCancelButton: true
    });
    if (!result.isConfirmed || !result.value) {
      return;
    }
    completeMutation.mutate({ tripId: trip.tripId, carStatus: result.value });
  };

  const handleBreakdown = async (trip: TripRow) => {
    if (!trip.tripId) {
      await Swal.fire({
        icon: "warning",
        title: "Missing trip id",
        text: "Reload the dashboard before reporting a breakdown."
      });
      return;
    }
    breakdownMutation.mutate(trip.tripId);
  };

  const handleRepair = async (trip: TripRow) => {
    if (!trip.tripId) {
      await Swal.fire({
        icon: "warning",
        title: "Missing trip id",
        text: "Reload the dashboard before requesting repair."
      });
      return;
    }
    repairMutation.mutate(trip.tripId);
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2>Recent trips</h2>
        <button className="button ghost">Manage</button>
      </div>
      <div className="table">
        <div className="table-row head">
          <span>Trip</span>
          <span>Driver</span>
          <span>Car</span>
          <span>Status</span>
          <span>Payment</span>
        </div>
        {trips.map((trip) => (
          <div key={trip.id} className="table-row">
            <span>{trip.id}</span>
            <span>{trip.driver}</span>
            <span>{trip.car}</span>
            <span className={`chip ${trip.status.toLowerCase().replace(" ", "-")}`}>
              {trip.status}
            </span>
            <div className="payment-cell">
              <span>{trip.payment}</span>
              <div className="table-actions">
                {trip.status === "In progress" && (
                  <>
                    <button className="button ghost tiny" onClick={() => handleComplete(trip)}>
                      Complete
                    </button>
                    <button className="button ghost tiny" onClick={() => handleBreakdown(trip)}>
                      Breakdown
                    </button>
                  </>
                )}
                {trip.status === "Broken" && (
                  <button className="button ghost tiny" onClick={() => handleRepair(trip)}>
                    Repair
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default TripsTable;
