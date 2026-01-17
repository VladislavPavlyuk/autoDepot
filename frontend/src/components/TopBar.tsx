type TopBarProps = {
  onSearch: (value: string) => void;
  onCreateOrder: () => void;
  onExport: () => void;
  currentTime: string;
};

const TopBar = ({ onSearch, onCreateOrder, onExport, currentTime }: TopBarProps) => {
  return (
    <header className="topbar">
      <div className="brand">
        <img className="logo" src="/commodore.png" alt="AutoDepot logo" />
        <div>
          <div className="brand-name">AutoDepot</div>
          <div className="brand-sub">Fleet control center · {currentTime}</div>
        </div>
      </div>
      <div className="top-actions">
        <input
          className="search"
          placeholder="Search orders, trips, drivers"
          onChange={(event) => onSearch(event.target.value)}
        />
        <button className="button ghost" onClick={onExport}>
          Export
        </button>
        <button className="button primary" onClick={onCreateOrder}>
          New Order
        </button>
      </div>
    </header>
  );
};

export default TopBar;
