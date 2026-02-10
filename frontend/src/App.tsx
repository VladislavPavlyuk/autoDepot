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
import { createDriver, createOrder, generateOrder } from "./api/dashboardApi";
import { useDashboardData } from "./hooks/useDashboardData";
import { useI18n } from "./i18n";
import { DEFAULT_ERRORS_POLICY } from "./types/errors";

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
    const escapeHtml = (value: string) =>
      value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

    const renderOptions = (values: string[], placeholder: string) => {
      const options = values
        .map((value) => `<option value="${escapeHtml(value)}">${escapeHtml(value)}</option>`)
        .join("");
      return `
        <option value="">${escapeHtml(placeholder)}</option>
        ${options}
        <option value="__custom__">${escapeHtml(t("dialog.newOrder.customValue"))}</option>
      `;
    };

    const result = await Swal.fire({
      title: t("dialog.newOrder.title"),
      html:
        `<select id="order-destination" class="swal2-select">
          ${renderOptions(destinationOptions, t("dialog.newOrder.selectDestination"))}
        </select>` +
        `<input id="order-destination-custom" class="swal2-input" placeholder="${t(
          "dialog.newOrder.customDestination"
        )}">` +
        `<select id="order-cargo" class="swal2-select">
          ${renderOptions(cargoOptions, t("dialog.newOrder.selectCargo"))}
        </select>` +
        `<input id="order-cargo-custom" class="swal2-input" placeholder="${t(
          "dialog.newOrder.customCargo"
        )}">` +
        `<input id="order-weight" class="swal2-input" placeholder="${t(
          "dialog.newOrder.weight"
        )}" type="number" min="0" step="0.1">`,
      focusConfirm: false,
      showCancelButton: true,
      didOpen: () => {
        const popup = Swal.getPopup();
        const destinationSelect = popup?.querySelector<HTMLSelectElement>("#order-destination");
        const cargoSelect = popup?.querySelector<HTMLSelectElement>("#order-cargo");
        const destinationCustom = popup?.querySelector<HTMLInputElement>("#order-destination-custom");
        const cargoCustom = popup?.querySelector<HTMLInputElement>("#order-cargo-custom");

        const toggleCustom = (
          select?: HTMLSelectElement | null,
          input?: HTMLInputElement | null
        ) => {
          if (!select || !input) return;
          const show = select.value === "__custom__";
          input.style.display = show ? "block" : "none";
          if (show) {
            input.focus();
          }
          if (!show) {
            input.value = "";
          }
        };

        toggleCustom(destinationSelect, destinationCustom);
        toggleCustom(cargoSelect, cargoCustom);

        destinationSelect?.addEventListener("change", () =>
          toggleCustom(destinationSelect, destinationCustom)
        );
        cargoSelect?.addEventListener("change", () => toggleCustom(cargoSelect, cargoCustom));
      },
      preConfirm: () => {
        const popup = Swal.getPopup();
        const destinationSelect =
          popup?.querySelector<HTMLSelectElement>("#order-destination")?.value?.trim() ?? "";
        const cargoSelect =
          popup?.querySelector<HTMLSelectElement>("#order-cargo")?.value?.trim() ?? "";
        const destinationCustom =
          popup?.querySelector<HTMLInputElement>("#order-destination-custom")?.value?.trim() ?? "";
        const cargoCustom =
          popup?.querySelector<HTMLInputElement>("#order-cargo-custom")?.value?.trim() ?? "";

        const destination =
          destinationSelect === "__custom__" || !destinationSelect
            ? destinationCustom
            : destinationSelect;
        const cargoType =
          cargoSelect === "__custom__" || !cargoSelect ? cargoCustom : cargoSelect;
        const weightValue = popup?.querySelector<HTMLInputElement>("#order-weight")?.value;
        const weight = weightValue ? Number(weightValue) : NaN;

        if (!destination || !cargoType || Number.isNaN(weight) || weight <= 0) {
          Swal.showValidationMessage(t("dialog.newOrder.validation"));
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
      const msg =
        (error as Error & { serverMessage?: string }).serverMessage ??
        (error instanceof Error ? error.message : t("dialog.generateFail.text"));
      await Swal.fire({
        icon: "error",
        title: t("dialog.generateFail.title"),
        text: msg
      });
    }
  };

  const handleAddDriver = async () => {
    const nowYear = new Date().getFullYear();
    const yearOptions = Array.from({ length: nowYear - 1969 }, (_, index) => String(1970 + index));
    const categories = ["A", "B", "C", "D", "E"];
    const result = await Swal.fire({
      title: t("dialog.addDriver.title"),
      html:
        `<input id="driver-name" class="swal2-input" placeholder="${t(
          "dialog.addDriver.name"
        )}">` +
        `<div class="driver-categories" style="text-align:left; margin: 1rem 0;">
          <p style="margin:0 0 8px; font-size: 0.9em; color: var(--muted);">${t(
            "dialog.addDriver.licenseCategory"
          )}</p>
          ${categories
            .map(
              (cat) =>
                `<label style="display:inline-block; margin-right: 1rem;"><input type="checkbox" name="driver-cat" value="${cat}"> ${cat}</label>`
            )
            .join("")}
        </div>` +
        `<select id="driver-license-year" class="swal2-select">
          <option value="">${t("dialog.addDriver.selectYear")}</option>
          ${yearOptions.map((year) => `<option value="${year}">${year}</option>`).join("")}
        </select>`,
      focusConfirm: false,
      showCancelButton: true,
      preConfirm: () => {
        const popup = Swal.getPopup();
        const name = popup?.querySelector<HTMLInputElement>("#driver-name")?.value?.trim() ?? "";
        const checked = popup?.querySelectorAll<HTMLInputElement>('input[name="driver-cat"]:checked');
        const licenseCategories = checked ? Array.from(checked).map((el) => el.value) : [];
        const yearValue =
          popup?.querySelector<HTMLSelectElement>("#driver-license-year")?.value?.trim() ?? "";
        const year = yearValue ? Number(yearValue) : NaN;

        if (!name || licenseCategories.length === 0 || Number.isNaN(year) || year < 1970 || year > nowYear) {
          Swal.showValidationMessage(t("dialog.addDriver.validation"));
          return null;
        }

        return {
          name,
          licenseYear: year,
          licenseCategories
        };
      }
    });

    if (!result.isConfirmed || !result.value) {
      return;
    }

    try {
      await createDriver(result.value);
    } catch (err: unknown) {
      const ax = err as { response?: { data?: unknown; status?: number }; serverMessage?: string };
      let text =
        ax.serverMessage ??
        (ax.response?.data != null && typeof ax.response.data === "string"
          ? ax.response.data
          : ax.response?.data != null && typeof ax.response.data === "object"
            ? [("message" in ax.response.data && (ax.response.data as { message: unknown }).message), ("error" in ax.response.data && (ax.response.data as { error: unknown }).error)]
                .filter(Boolean)
                .map(String)
                .join(" — ")
            : "");
      if (!text && ax.response?.status) text = `${ax.response.status}`;
      if (!text) text = err instanceof Error ? err.message : t("dialog.addDriver.fail.text");
      await Swal.fire({
        icon: "error",
        title: t("dialog.addDriver.fail.title"),
        text: text || t("dialog.addDriver.fail.text")
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
          <DriversTable drivers={data?.driverPerformance ?? []} onAddDriver={handleAddDriver} />
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
