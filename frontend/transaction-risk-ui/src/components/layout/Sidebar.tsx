import { NavLink } from "react-router-dom";

const navItems = [
  { to: "/", label: "Dashboard" },
  { to: "/transactions/new", label: "Create Transaction" },
  { to: "/risk-decisions", label: "Risk Decisions" },
  { to: "/alerts", label: "Alerts" },
  { to: "/system-flow", label: "System Flow" },
];

export function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="brand">
        <div className="brand-title">Transaction Risk Engine</div>
        <div className="brand-subtitle">Analyst Dashboard</div>
      </div>

      <nav className="nav-list">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              isActive ? "nav-link nav-link-active" : "nav-link"
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}