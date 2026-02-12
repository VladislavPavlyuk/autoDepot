import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import Swal from "sweetalert2";
import { assignTrip } from "../api/dashboardApi";
import { OrderRow, OrderStatus } from "../types/dashboard";
import { useI18n } from "../i18n";
import { getApiErrorMessage } from "../utils/dialogs";

type OrdersTableProps = {
  orders: OrderRow[];
  onSuccess?: () => void | Promise<unknown>;
};

const statusClass = (status: OrderRow["status"]) => {
  switch (status) {
    case "QUEUED":
      return "queued";
    case "ASSIGNED":
      return "assigned";
    case "READY":
      return "ready";
    default:
      return "queued";
  }
};

const statusLabelKey = (status: OrderRow["status"]) => {
  switch (status) {
    case "QUEUED":
      return "status.queued";
    case "ASSIGNED":
      return "status.assigned";
    case "READY":
      return "status.ready";
    default:
      return "status.queued";
  }
};

const OrdersTable = ({ orders, onSuccess }: OrdersTableProps) => {
  const { t } = useI18n();
  const [statusFilter, setStatusFilter] = useState<"ALL" | OrderStatus>("ALL");
  const assignMutation = useMutation({
    mutationFn: async (orderId: number) => {
      await assignTrip(orderId);
    },
    onSuccess: async () => {
      await Swal.fire({
        icon: "success",
        title: t("dialog.assignSuccess.title"),
        text: t("dialog.assignSuccess.text"),
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
        title: t("dialog.assignFail.title"),
        text: getApiErrorMessage(error, t("dialog.assignFail.text"))
      });
    }
  });

  const handleAssign = async (order: OrderRow) => {
    if (!order.orderId) {
      await Swal.fire({
        icon: "warning",
        title: t("orders.noId"),
        text: t("orders.refreshHint")
      });
      return;
    }
    assignMutation.mutate(order.orderId);
  };

  const visibleOrders =
    statusFilter === "ALL" ? orders : orders.filter((order) => order.status === statusFilter);

  return (
    <div className="card">
      <div className="card-header">
        <h2>{t("orders.title")}</h2>
        <div className="header-actions">
          <select
            className="filter-select"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as "ALL" | OrderStatus)}
          >
            <option value="ALL">{t("filter.all")}</option>
            <option value="QUEUED">{t("status.queued")}</option>
            <option value="ASSIGNED">{t("status.assigned")}</option>
            <option value="READY">{t("status.ready")}</option>
          </select>
        </div>
      </div>
      <div className="table">
        <div className="table-row head">
          <span>{t("orders.col.id")}</span>
          <span>{t("orders.col.cargo")}</span>
          <span>{t("orders.col.destination")}</span>
          <span>{t("orders.col.weight")}</span>
          <span>{t("orders.col.status")}</span>
        </div>
        {visibleOrders.map((order) => (
          <div key={order.id} className="table-row">
            <span>{order.id}</span>
            <span>{order.cargo}</span>
            <span>{order.destination}</span>
            <span>{order.weight}</span>
            <div className="status-cell">
              <span className={`chip ${statusClass(order.status)}`}>{t(statusLabelKey(order.status))}</span>
              <button
                className="button ghost tiny"
                onClick={() => handleAssign(order)}
                disabled={assignMutation.isPending || order.status !== "QUEUED" || !order.orderId}
              >
                {t("orders.assign")}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OrdersTable;
