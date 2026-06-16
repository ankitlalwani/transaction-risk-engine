import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

import { Badge } from "../../../components/ui/Badge";
import { Card } from "../../../components/ui/Card";
import { getAlerts } from "../api/alertApi";
import type { AlertPriority, AlertStatus } from "../types";

type AlertTab = {
  label: string;
  filters?: {
    status?: AlertStatus;
    riskLevel?: "HIGH" | "CRITICAL";
  };
};

const alertTabs: AlertTab[] = [
  { label: "All" },
  { label: "Open", filters: { status: "OPEN" } },
  { label: "Critical", filters: { riskLevel: "CRITICAL" } },
  { label: "High", filters: { riskLevel: "HIGH" } },
  { label: "In Review", filters: { status: "IN_REVIEW" } },
  { label: "Closed", filters: { status: "CLOSED" } },
];

function priorityVariant(priority: AlertPriority) {
  if (priority === "CRITICAL") return "danger";
  if (priority === "HIGH") return "warning";
  if (priority === "MEDIUM") return "neutral";
  return "success";
}

export function AlertsPage() {
  const [activeTab, setActiveTab] = useState(alertTabs[0]);
  const { data, isLoading, isError } = useQuery({
    queryKey: ["alerts", activeTab.label],
    queryFn: () => getAlerts(activeTab.filters),
    refetchInterval: 5000,
  });

  if (isLoading) return <p>Loading alerts...</p>;
  if (isError) return <p>Failed to load alerts.</p>;

  return (
    <div>
      <h2>Alerts</h2>
      <p className="page-description">
        Operational alerts created for high-risk transactions.
      </p>

      <Card>
        <div className="tabs" role="tablist" aria-label="Alert filters">
          {alertTabs.map((tab) => (
            <button
              key={tab.label}
              type="button"
              role="tab"
              aria-selected={activeTab.label === tab.label}
              className={`tab ${activeTab.label === tab.label ? "tab-active" : ""}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {data?.length === 0 ? (
          <p className="empty-state">No alerts match this filter.</p>
        ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Alert</th>
              <th>Transaction</th>
              <th>Priority</th>
              <th>Status</th>
              <th>Risk</th>
              <th>Reason</th>
              <th>Created</th>
            </tr>
          </thead>

          <tbody>
            {data?.map((alert) => (
              <tr key={alert.id}>
                <td>
                  <Link to={`/alerts/${alert.id}`}>{alert.alertReference}</Link>
                </td>
                <td>{alert.transactionReference}</td>
                <td>
                  <Badge variant={priorityVariant(alert.alertPriority)}>
                    {alert.alertPriority}
                  </Badge>
                </td>
                <td>{alert.alertStatus}</td>
                <td>{alert.riskLevel}</td>
                <td>{alert.alertReason}</td>
                <td>{new Date(alert.createdAt).toLocaleString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
        )}
      </Card>
    </div>
  );
}
