import { useI18n } from "../i18n";

type TopBarProps = {
  onSearch: (value: string) => void;
  onCreateOrder: () => void;
  currentTime: string;
  language: "uk" | "en";
  onLanguageChange: (language: "uk" | "en") => void;
  theme: "dark" | "light";
  onThemeToggle: () => void;
};

const TopBar = ({
  onSearch,
  onCreateOrder,
  currentTime,
  language,
  onLanguageChange,
  theme,
  onThemeToggle
}: TopBarProps) => {
  const { t } = useI18n();
  return (
    <header className="topbar">
      <div className="brand">
        <img className="logo" src="/commodore.png" alt="AutoDepot logo" />
        <div>
          <div className="brand-name">{t("app.name")}</div>
          <div className="brand-sub">
            {t("app.subtitle")} · {currentTime}
          </div>
        </div>
      </div>
      <div className="top-actions">
        <input
          className="search"
          placeholder={t("search.placeholder")}
          onChange={(event) => onSearch(event.target.value)}
        />
        <select
          className="filter-select"
          value={language}
          onChange={(event) => onLanguageChange(event.target.value as "uk" | "en")}
        >
          <option value="uk">{t("language.uk")}</option>
          <option value="en">{t("language.en")}</option>
        </select>
        <button className="button ghost" onClick={onThemeToggle}>
          {theme === "dark" ? t("theme.light") : t("theme.dark")}
        </button>
        <button className="button primary" onClick={onCreateOrder}>
          {t("action.newOrder")}
        </button>
      </div>
    </header>
  );
};

export default TopBar;
