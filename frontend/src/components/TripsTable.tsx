import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import Swal from "sweetalert2";
import { completeTrip, confirmRepairComplete, reportBreakdown, requestRepair } from "../api/dashboardApi";
import { TripRow, TripStatus } from "../types/dashboard";
import { useI18n } from "../i18n";
import { getApiErrorMessage } from "../utils/dialogs";

type TripsTableProps = {
  trips: TripRow[];
  onSuccess?: () => void | Promise<unknown>;
};

const statusClass = (status: TripRow["status"]) => {
  switch (status) {
    case "IN_PROGRESS":
      return "in-progress";
    case "BROKEN":
      return "broken";
    case "REPAIR_REQUESTED":
      return "repair-requested";
    case "COMPLETED":
      return "completed";
    default:
      return "in-progress";
  }
};

const statusLabelKey = (status: TripRow["status"]) => {
  switch (status) {
    case "IN_PROGRESS":
      return "status.inProgress";
    case "BROKEN":
      return "status.broken";
    case "REPAIR_REQUESTED":
      return "status.repairRequested";
    case "COMPLETED":
      return "status.completed";
    default:
      return "status.inProgress";
  }
};

const TripsTable = ({ trips, onSuccess }: TripsTableProps) => {
  const { t } = useI18n();
  const [statusFilter, setStatusFilter] = useState<"ALL" | TripStatus>("ALL");
  const completeMutation = useMutation({
    mutationFn: completeTrip,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: t("dialog.tripComplete.title"),
        text: t("dialog.tripComplete.text"),
        timer: 1600,
        showConfirmButton: false
      });
      try {
        await onSuccess?.();
      } catch {}
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: t("dialog.tripCompleteFail.title"),
        text: getApiErrorMessage(error, t("dialog.tripCompleteFail.text"))
      });
    }
  });

  const breakdownMutation = useMutation({
    mutationFn: reportBreakdown,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: t("dialog.breakdown.title"),
        text: t("dialog.breakdown.text"),
        timer: 1600,
        showConfirmButton: false
      });
      try {
        await onSuccess?.();
      } catch {}
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: t("dialog.breakdownFail.title"),
        text: getApiErrorMessage(error, t("dialog.breakdownFail.text"))
      });
    }
  });

  const repairMutation = useMutation({
    mutationFn: requestRepair,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: t("dialog.repair.title"),
        text: t("dialog.repair.text"),
        timer: 1600,
        showConfirmButton: false
      });
      try {
        await onSuccess?.();
      } catch {}
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: t("dialog.repairFail.title"),
        text: getApiErrorMessage(error, t("dialog.repairFail.text"))
      });
    }
  });

  const confirmRepairMutation = useMutation({
    mutationFn: confirmRepairComplete,
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: t("dialog.repairComplete.title"),
        text: t("dialog.repairComplete.text"),
        timer: 1600,
        showConfirmButton: false
      });
      try {
        await onSuccess?.();
      } catch {}
    },
    onError: async (error) => {
      await Swal.fire({
        icon: "error",
        title: t("dialog.repairCompleteFail.title"),
        text: getApiErrorMessage(error, t("dialog.repairCompleteFail.text"))
      });
    }
  });

  const handleComplete = async (trip: TripRow) => {
    if (!trip.tripId) {
      await Swal.fire({
        icon: "warning",
        title: t("dialog.tripIdMissing.title"),
        text: t("dialog.tripIdMissing.complete")
      });
      return;
    }
    const result = await Swal.fire({
      title: t("dialog.carStatus.title"),
      input: "select",
      inputOptions: {
        OK: t("dialog.carStatus.ok"),
        BROKEN: t("dialog.carStatus.broken")
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
        title: t("dialog.tripIdMissing.title"),
        text: t("dialog.tripIdMissing.breakdown")
      });
      return;
    }
    breakdownMutation.mutate(trip.tripId);
  };

  const handleRepair = async (trip: TripRow) => {
    if (!trip.tripId) {
      await Swal.fire({
        icon: "warning",
        title: t("dialog.tripIdMissing.title"),
        text: t("dialog.tripIdMissing.repair")
      });
      return;
    }
    repairMutation.mutate(trip.tripId);
  };

  const handleConfirmRepair = async (trip: TripRow) => {
    if (!trip.tripId) {
      await Swal.fire({
        icon: "warning",
        title: t("dialog.tripIdMissing.title"),
        text: t("dialog.tripIdMissing.repairComplete")
      });
      return;
    }
    confirmRepairMutation.mutate(trip.tripId);
  };

  const visibleTrips =
    statusFilter === "ALL" ? trips : trips.filter((trip) => trip.status === statusFilter);

  return (
    <div className="card">
      <div className="card-header">
        <h2>{t("trips.title")}</h2>
        <div className="header-actions">
          <select
            className="filter-select"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as "ALL" | TripStatus)}
          >
            <option value="ALL">{t("filter.all")}</option>
            <option value="IN_PROGRESS">{t("status.inProgress")}</option>
            <option value="BROKEN">{t("status.broken")}</option>
            <option value="REPAIR_REQUESTED">{t("status.repairRequested")}</option>
            <option value="COMPLETED">{t("status.completed")}</option>
          </select>
        </div>
      </div>
      <div className="table">
        <div className="table-row head">
          <span>{t("trips.col.id")}</span>
          <span>{t("trips.col.driver")}</span>
          <span>{t("trips.col.car")}</span>
          <span>{t("trips.col.status")}</span>
          <span>{t("trips.col.payment")}</span>
        </div>
        {visibleTrips.map((trip) => (
          <div key={trip.id} className="table-row">
            <span>{trip.id}</span>
            <span>{trip.driver}</span>
            <span>{trip.car}</span>
            <span className={`chip ${statusClass(trip.status)}`}>{t(statusLabelKey(trip.status))}</span>
            <div className="payment-cell">
              <span>{trip.payment}</span>
              <div className="table-actions">
                {trip.status === "IN_PROGRESS" && (
                  <>
                    <button className="button ghost tiny" onClick={() => handleComplete(trip)}>
                      {t("trips.complete")}
                    </button>
                    <button className="button ghost tiny" onClick={() => handleBreakdown(trip)}>
                      {t("trips.breakdown")}
                    </button>
                  </>
                )}
                {trip.status === "BROKEN" && (
                  <button className="button ghost tiny" onClick={() => handleRepair(trip)}>
                    {t("trips.repair")}
                  </button>
                )}
                {trip.status === "REPAIR_REQUESTED" && (
                  <button className="button ghost tiny" onClick={() => handleConfirmRepair(trip)}>
                    {t("trips.confirmRepair")}
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
