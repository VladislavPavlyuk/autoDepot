import { StatCard as StatCardType } from "../types/dashboard";
import { useI18n } from "../i18n";

type StatCardProps = {
  stat: StatCardType;
};

const StatCard = ({ stat }: StatCardProps) => {
  const { t } = useI18n();
  const translatedLabel = t(`stat.${stat.label}`);
  const label = translatedLabel === `stat.${stat.label}` ? stat.label : translatedLabel;
  return (
    <div className="card stat-card">
      <div className="stat-label">{label}</div>
      <div className="stat-row">
        <div className="stat-value">{stat.value}</div>
        {stat.trend ? <div className="stat-trend">{stat.trend}</div> : null}
      </div>
    </div>
  );
};

export default StatCard;
