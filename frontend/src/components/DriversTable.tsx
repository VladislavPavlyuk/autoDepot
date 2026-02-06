import { useMemo } from "react";
import { DriverStat } from "../types/dashboard";
import { useI18n } from "../i18n";

type DriversTableProps = {
  drivers: DriverStat[];
  onAddDriver?: () => void;
};

const DriversTable = ({ drivers, onAddDriver }: DriversTableProps) => {
  const { t, language } = useI18n();
  const sortedDrivers = useMemo(() => {
    return [...drivers].sort((a, b) => b.earnings - a.earnings);
  }, [drivers]);

  const formatWeight = (value: number) => {
    const locale = language === "uk" ? "uk-UA" : "en-US";
    return `${value.toLocaleString(locale)} ${t("unit.kg")}`;
  };

  const formatMoney = (value: number) => {
    const locale = language === "uk" ? "uk-UA" : "en-US";
    const currency = language === "uk" ? "UAH" : "USD";
    return new Intl.NumberFormat(locale, { style: "currency", currency }).format(value);
  };

  return (
    <div className="card">
      <div className="card-header">
        <h2>{t("drivers.title")}</h2>
        <div className="header-actions">
          <span className="muted">{t("drivers.subtitle")}</span>
          {onAddDriver && (
            <button className="button ghost tiny" onClick={onAddDriver}>
              {t("drivers.add")}
            </button>
          )}
        </div>
      </div>
      <div className="table">
        <div className="table-row head drivers">
          <span>{t("drivers.col.name")}</span>
          <span>{t("drivers.col.category")}</span>
          <span>{t("drivers.col.experience")}</span>
          <span>{t("drivers.col.trips")}</span>
          <span>{t("drivers.col.weight")}</span>
          <span>{t("drivers.col.earnings")}</span>
        </div>
        {sortedDrivers.map((driver, index) => (
          <div key={driver.driverName} className="table-row drivers">
            <span>{driver.driverName}</span>
            <span>{(driver.licenseCategories ?? []).join(", ")}</span>
            <span>{driver.experience}</span>
            <span>{driver.tripCount}</span>
            <span>{formatWeight(driver.totalWeight)}</span>
            <span>{formatMoney(driver.earnings)}</span>
          </div>
        ))}
        {sortedDrivers.length === 0 && (
          <div className="empty-row">{t("drivers.empty")}</div>
        )}
      </div>
    </div>
  );
};

export default DriversTable;
