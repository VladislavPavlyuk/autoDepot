import { StatCard as StatCardType } from "../types/dashboard";

type StatCardProps = {
  stat: StatCardType;
};

const StatCard = ({ stat }: StatCardProps) => {
  return (
    <div className="card stat-card">
      <div className="stat-label">{stat.label}</div>
      <div className="stat-row">
        <div className="stat-value">{stat.value}</div>
        <div className="stat-trend">{stat.trend}</div>
      </div>
    </div>
  );
};

export default StatCard;
