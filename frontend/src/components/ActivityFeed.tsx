import { useI18n } from "../i18n";

type ActivityFeedProps = {
  items: string[];
};

const ActivityFeed = ({ items }: ActivityFeedProps) => {
  const { t } = useI18n();
  return (
    <div className="card">
      <div className="card-header">
        <h2>{t("activity.title")}</h2>
        <span className="muted">{t("activity.subtitle")}</span>
      </div>
      <ul className="activity">
        {items.length === 0 ? (
          <li className="muted">{t("activity.empty")}</li>
        ) : (
          items.map((item) => <li key={item}>{item}</li>)
        )}
      </ul>
    </div>
  );
};

export default ActivityFeed;
