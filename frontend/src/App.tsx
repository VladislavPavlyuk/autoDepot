import { useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import ActivityFeed from "./components/ActivityFeed";
import DriversTable from "./components/DriversTable";
import ErrorsPanel from "./components/ErrorsPanel";
import Hero from "./components/Hero";
import OrdersTable from "./components/OrdersTable";
import StatCard from "./components/StatCard";
import TopBar from "./components/TopBar";
import TripsTable from "./components/TripsTable";
import { createDriver, createOrder, generateOrder, updateDriver } from "./api/dashboardApi";
import { useDashboardData } from "./hooks/useDashboardData";
import { useI18n } from "./i18n";
import { DEFAULT_ERRORS_POLICY } from "./types/errors";
import {
  buildAddDriverHtml,
  buildEditDriverHtml,
  buildNewOrderHtml,
  getAddDriverFormValues,
  getApiErrorMessage,
  getNewOrderFormValues,
  setupNewOrderCustomInputs
} from "./utils/dialogs";

const App = () => {
  const { data, refetch } = useDashboardData();
  const [search, setSearch] = useState("");
  const [currentTime, setCurrentTime] = useState("");
  const [showErrorsPanel, setShowErrorsPanel] = useState(false);
  const [theme, setTheme] = useState<"dark" | "light">(() => {
    if (typeof window === "undefined") return "dark";
    const saved = window.localStorage.getItem("autodepot.theme");
    return saved === "light" ? "light" : "dark";
  });
  const { t, language, setLanguage } = useI18n();

  useEffect(() => {
    const updateClock = () => {
      const now = new Date();
      setCurrentTime(now.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" }));
    };
    updateClock();
    const timer = setInterval(updateClock, 30_000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    document.documentElement.style.colorScheme = theme;
    window.localStorage.setItem("autodepot.theme", theme);
  }, [theme]);

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

  const destinationOptions = useMemo(() => {
    const values = (data?.orders ?? [])
      .map((order) => order.destination)
      .filter((value): value is string => Boolean(value && value.trim()));
    return Array.from(new Set(values)).sort((a, b) => a.localeCompare(b));
  }, [data]);

  const cargoOptions = useMemo(() => {
    const values = (data?.orders ?? [])
      .map((order) => order.cargo)
      .filter((value): value is string => Boolean(value && value.trim()));
    return Array.from(new Set(values)).sort((a, b) => a.localeCompare(b));
  }, [data]);

  const sortedOrders = useMemo(() => {
    const priority: Record<string, number> = {
      QUEUED: 0,
      ASSIGNED: 1,
      READY: 2
    };
    return [...filteredOrders].sort((a, b) => {
      const pa = priority[a.status] ?? 99;
      const pb = priority[b.status] ?? 99;
      if (pa !== pb) return pa - pb;
      const aId = a.orderId ?? Number(a.id.replace(/\D/g, "")) ?? 0;
      const bId = b.orderId ?? Number(b.id.replace(/\D/g, "")) ?? 0;
      return aId - bId;
    });
  }, [filteredOrders]);

  const sortedTrips = useMemo(() => {
    const priority: Record<string, number> = {
      IN_PROGRESS: 0,
      BROKEN: 1,
      REPAIR_REQUESTED: 2,
      COMPLETED: 3
    };
    return [...filteredTrips].sort((a, b) => {
      const pa = priority[a.status] ?? 99;
      const pb = priority[b.status] ?? 99;
      if (pa !== pb) return pa - pb;
      const aId = a.tripId ?? Number(a.id.replace(/\D/g, "")) ?? 0;
      const bId = b.tripId ?? Number(b.id.replace(/\D/g, "")) ?? 0;
      return aId - bId;
    });
  }, [filteredTrips]);

  const handleCreateOrder = async () => {
    const result = await Swal.fire({
      title: t("dialog.newOrder.title"),
      html: buildNewOrderHtml(destinationOptions, cargoOptions, t),
      focusConfirm: false,
      showCancelButton: true,
      didOpen: () => setupNewOrderCustomInputs(Swal),
      preConfirm: () =>
        getNewOrderFormValues(Swal.getPopup(), t("dialog.newOrder.validation"), Swal)
    });
    if (!result.isConfirmed || !result.value) return;
    await createOrder(result.value);
    await Swal.fire({
      icon: "success",
      title: t("dialog.orderCreated.title"),
      text: t("dialog.orderCreated.text"),
      timer: 1600,
      showConfirmButton: false
    });
    refetch();
  };

  const handleGenerateOrder = async () => {
    try {
      await generateOrder();
      await Swal.fire({
        icon: "success",
        title: t("dialog.orderCreated.title"),
        text: t("dialog.orderCreated.text"),
        timer: 1600,
        showConfirmButton: false
      });
      refetch();
    } catch (error) {
      await Swal.fire({
        icon: "error",
        title: t("dialog.generateFail.title"),
        text: getApiErrorMessage(error, t("dialog.generateFail.text"))
      });
    }
  };

  const handleAddDriver = async () => {
    const nowYear = new Date().getFullYear();
    const result = await Swal.fire({
      title: t("dialog.addDriver.title"),
      html: buildAddDriverHtml(t, nowYear),
      focusConfirm: false,
      showCancelButton: true,
      preConfirm: () =>
        getAddDriverFormValues(
          Swal.getPopup(),
          nowYear,
          t("dialog.addDriver.validation"),
          Swal
        )
    });
    if (!result.isConfirmed || !result.value) return;
    try {
      await createDriver(result.value);
    } catch (err: unknown) {
      await Swal.fire({
        icon: "error",
        title: t("dialog.addDriver.fail.title"),
        text: getApiErrorMessage(err, t("dialog.addDriver.fail.text")) || t("dialog.addDriver.fail.text")
      });
      return;
    }
    try {
      await Swal.fire({
        icon: "success",
        title: t("dialog.addDriver.success.title"),
        text: t("dialog.addDriver.success.text"),
        timer: 1600,
        showConfirmButton: false
      });
      await refetch();
    } catch {
      await Swal.fire({
        icon: "success",
        title: t("dialog.addDriver.success.title"),
        text: t("dialog.addDriver.refetchFail") ?? "Driver added. Refresh the page to see the list."
      });
      refetch();
    }
  };

  const handleEditDriver = async (driver: {
    driverId?: number;
    driverName: string;
    licenseCategories?: string[];
    licenseYear?: number;
    experience?: number;
  }) => {
    if (driver.driverId == null) return;
    const nowYear = new Date().getFullYear();
    const licenseYear =
      driver.licenseYear ?? (driver.experience != null ? nowYear - driver.experience : nowYear);
    const result = await Swal.fire({
      title: t("dialog.editDriver.title"),
      html: buildEditDriverHtml(t, nowYear, {
        driverName: driver.driverName,
        licenseCategories: driver.licenseCategories ?? [],
        licenseYear
      }),
      focusConfirm: false,
      showCancelButton: true,
      preConfirm: () =>
        getAddDriverFormValues(
          Swal.getPopup(),
          nowYear,
          t("dialog.addDriver.validation"),
          Swal
        )
    });
    if (!result.isConfirmed || !result.value) return;
    try {
      await updateDriver(driver.driverId, result.value);
    } catch (err: unknown) {
      await Swal.fire({
        icon: "error",
        title: t("dialog.editDriver.fail.title"),
        text: getApiErrorMessage(err, t("dialog.editDriver.fail.text")) ?? t("dialog.editDriver.fail.text")
      });
      return;
    }
    try {
      await Swal.fire({
        icon: "success",
        title: t("dialog.editDriver.success.title"),
        text: t("dialog.editDriver.success.text"),
        timer: 1600,
        showConfirmButton: false
      });
      await refetch();
    } catch {
      refetch();
    }
  };

  const stats = data?.stats ?? [];

  return (
    <div className="page">
      <TopBar
        onSearch={setSearch}
        onCreateOrder={handleCreateOrder}
        onOpenErrors={() => setShowErrorsPanel(true)}
        currentTime={currentTime}
        language={language}
        onLanguageChange={setLanguage}
        theme={theme}
        onThemeToggle={() => setTheme(theme === "dark" ? "light" : "dark")}
      />

      {showErrorsPanel && (
        <div className="errors-panel-overlay" onClick={() => setShowErrorsPanel(false)} role="presentation">
          <div onClick={(e) => e.stopPropagation()} role="dialog" aria-label="Error audit log">
            <ErrorsPanel policy={DEFAULT_ERRORS_POLICY} onClose={() => setShowErrorsPanel(false)} />
          </div>
        </div>
      )}

      <main className="content">
        <Hero
          subtitle={t("hero.subtitle")}
          title={t("hero.title")}
          onGenerateOrder={handleGenerateOrder}
          generateLabel={t("action.generateOrder")}
        />

        <section className="grid stats">
          {stats.map((stat) => (
            <StatCard key={stat.label} stat={stat} />
          ))}
        </section>

        <section className="grid two-columns">
          <OrdersTable orders={sortedOrders} onSuccess={refetch} />
          <TripsTable trips={sortedTrips} onSuccess={refetch} />
        </section>

        <section className="grid">
          <DriversTable drivers={data?.driverPerformance ?? []} onAddDriver={handleAddDriver} onEditDriver={handleEditDriver} />
        </section>

        <section className="grid">
          <ActivityFeed items={data?.activity ?? []} />
        </section>
      </main>

      <footer className="footer">
        <span>{t("app.footer")}</span>
      </footer>
    </div>
  );
};

export default App;
