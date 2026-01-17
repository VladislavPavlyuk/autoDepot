import { OrderRow } from "../types/dashboard";

type OrdersTableProps = {
  orders: OrderRow[];
};

const OrdersTable = ({ orders }: OrdersTableProps) => {
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
            <span className={`chip ${order.status.toLowerCase()}`}>{order.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OrdersTable;
