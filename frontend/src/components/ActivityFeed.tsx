type ActivityFeedProps = {
  items: string[];
};

const ActivityFeed = ({ items }: ActivityFeedProps) => {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Activity</h2>
        <span className="muted">Last 30 minutes</span>
      </div>
      <ul className="activity">
        {items.length === 0 ? (
          <li className="muted">No activity yet.</li>
        ) : (
          items.map((item) => <li key={item}>{item}</li>)
        )}
      </ul>
    </div>
  );
};

export default ActivityFeed;
