import { useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import ActivityFeed from "./components/ActivityFeed";
import ActionCenter from "./components/ActionCenter";
import Hero from "./components/Hero";
import OrdersTable from "./components/OrdersTable";
import StatCard from "./components/StatCard";
import TopBar from "./components/TopBar";
import TripsTable from "./components/TripsTable";
import { useDashboardData } from "./hooks/useDashboardData";

const App = () => {
  const { data } = useDashboardData();
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
    await Swal.fire({
      icon: "info",
      title: "Order dialog",
      text: "Connect this to your OrderApplicationService endpoint."
    });
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
          <OrdersTable orders={filteredOrders} />
          <TripsTable trips={filteredTrips} />
        </section>

        <section className="grid two-columns">
          <ActionCenter />
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
