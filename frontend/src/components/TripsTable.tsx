import { TripRow } from "../types/dashboard";

type TripsTableProps = {
  trips: TripRow[];
};

const TripsTable = ({ trips }: TripsTableProps) => {
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
            <span>{trip.payment}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default TripsTable;
