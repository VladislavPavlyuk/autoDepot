import { useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import ActivityFeed from "./components/ActivityFeed";
import ActionCenter from "./components/ActionCenter";
import Hero from "./components/Hero";
import OrdersTable from "./components/OrdersTable";
import StatCard from "./components/StatCard";
import TopBar from "./components/TopBar";
import TripsTable from "./components/TripsTable";
import { createOrder } from "./api/dashboardApi";
import { useDashboardData } from "./hooks/useDashboardData";

const App = () => {
  const { data, refetch } = useDashboardData();
  const [search, setSearch] = useState("");
  const [currentTime, setCurrentTime] = useState("");

  useEffect(() => {
    const updateClock = () => {
      const now = new Date();
      setCurrentTime(now.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }));
    };
    updateClock();
    const timer = setInterval(updateClock, 30_000);
    return () => clearInterval(timer);
  }, []);

  const filteredOrders = useMemo(() => {
    const orders = data?.orders ?? [];
    if (!search.trim()) return orders;
    return orders.filter((order) =>
      `${order.id} ${order.cargo} ${order.destination}`.toLowerCase().includes(search.toLowerCase())
    );
  }, [data, search]);

  const filteredTrips = useMemo(() => {
    const trips = data?.trips ?? [];
    if (!search.trim()) return trips;
    return trips.filter((trip) =>
      `${trip.id} ${trip.driver} ${trip.car}`.toLowerCase().includes(search.toLowerCase())
    );
  }, [data, search]);

  const handleCreateOrder = async () => {
    const result = await Swal.fire({
      title: "Create order",
      html:
        '<input id="order-destination" class="swal2-input" placeholder="Destination">' +
        '<input id="order-cargo" class="swal2-input" placeholder="Cargo type">' +
        '<input id="order-weight" class="swal2-input" placeholder="Weight (kg)" type="number" min="0" step="0.1">',
      focusConfirm: false,
      showCancelButton: true,
      preConfirm: () => {
        const popup = Swal.getPopup();
        const destination = popup?.querySelector<HTMLInputElement>("#order-destination")?.value?.trim();
        const cargoType = popup?.querySelector<HTMLInputElement>("#order-cargo")?.value?.trim();
        const weightValue = popup?.querySelector<HTMLInputElement>("#order-weight")?.value;
        const weight = weightValue ? Number(weightValue) : NaN;

        if (!destination || !cargoType || Number.isNaN(weight) || weight <= 0) {
          Swal.showValidationMessage("Enter destination, cargo type, and a valid weight.");
          return null;
        }

        return { destination, cargoType, weight };
      }
    });

    if (!result.isConfirmed || !result.value) {
      return;
    }

    await createOrder(result.value);
    await Swal.fire({
      icon: "success",
      title: "Order created",
      text: "The order is now in the dispatch queue.",
      timer: 1600,
      showConfirmButton: false
    });
    refetch();
  };

  const handleExport = async () => {
    await Swal.fire({
      icon: "success",
      title: "Export ready",
      text: "A CSV report would be downloaded here."
    });
  };

  const stats = data?.stats ?? [];

  return (
    <div className="page">
      <TopBar
        onSearch={setSearch}
        onCreateOrder={handleCreateOrder}
        onExport={handleExport}
        currentTime={currentTime}
      />

      <main className="content">
        <Hero subtitle="Real-time fleet performance, shipments, and service health." tags={["Live tracking", "PostgreSQL", "Testcontainers"]} />

        <section className="grid stats">
          {stats.map((stat) => (
            <StatCard key={stat.label} stat={stat} />
          ))}
        </section>

        <section className="grid two-columns">
          <OrdersTable orders={filteredOrders} onSuccess={refetch} />
          <TripsTable trips={filteredTrips} onSuccess={refetch} />
        </section>

        <section className="grid two-columns">
          <ActionCenter
            orders={data?.orders ?? []}
            trips={data?.trips ?? []}
            onSuccess={refetch}
          />
          <ActivityFeed items={data?.activity ?? []} />
        </section>
      </main>

      <footer className="footer">
        <span>AutoDepot UI · v0.1</span>
        <span>Dispatching made clear and fast</span>
      </footer>
    </div>
  );
};

export default App;
