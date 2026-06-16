import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

import { Badge } from "../../../components/ui/Badge";
import { Card } from "../../../components/ui/Card";
import { getAlertById } from "../api/alertApi";
import type {
  AlertPriority,
  AlertStatus,
  AlertTriggeredRule,
} from "../types";

function priorityVariant(priority: AlertPriority) {
  if (priority === "CRITICAL") return "danger";
  if (priority === "HIGH") return "warning";
  if (priority === "MEDIUM") return "neutral";
  return "success";
}

function statusVariant(status: AlertStatus) {
  if (status === "CLOSED" || status === "FALSE_POSITIVE") return "success";
  if (status === "ESCALATED") return "danger";
  if (status === "IN_REVIEW") return "warning";
  return "neutral";
}

function parseTriggeredRules(value: string): AlertTriggeredRule[] {
  try {
    const parsed: unknown = JSON.parse(value);
    return Array.isArray(parsed) ? (parsed as AlertTriggeredRule[]) : [];
  } catch {
    return [];
  }
}

export function AlertDetailPage() {
  const { alertId } = useParams();
  const { data, isLoading, isError } = useQuery({
    queryKey: ["alert", alertId],
    queryFn: () => getAlertById(alertId!),
    enabled: Boolean(alertId),
  });

  if (!alertId) return <p>Alert ID is missing.</p>;
  if (isLoading) return <p>Loading alert information...</p>;
  if (isError || !data) return <p>Failed to load alert information.</p>;

  const triggeredRules = parseTriggeredRules(data.triggeredRules);

  return (
    <div>
      <Link className="back-link" to="/alerts">
        Back to alerts
      </Link>

      <div className="detail-heading">
        <div>
          <h2>{data.alertReference}</h2>
          <p className="page-description">
            Investigation information for this alert.
          </p>
        </div>
        <div className="badge-group">
          <Badge variant={priorityVariant(data.alertPriority)}>
            {data.alertPriority}
          </Badge>
          <Badge variant={statusVariant(data.alertStatus)}>
            {data.alertStatus}
          </Badge>
        </div>
      </div>

      <Card title="Alert Summary">
        <dl className="detail-grid">
          <div>
            <dt>Alert ID</dt>
            <dd>{data.id}</dd>
          </div>
          <div>
            <dt>Assigned To</dt>
            <dd>{data.assignedTo ?? "Unassigned"}</dd>
          </div>
          <div>
            <dt>Risk Score</dt>
            <dd>{data.riskScore}</dd>
          </div>
          <div>
            <dt>Risk Level</dt>
            <dd>{data.riskLevel}</dd>
          </div>
          <div>
            <dt>Decision Status</dt>
            <dd>{data.decisionStatus}</dd>
          </div>
          <div>
            <dt>Risk Decision ID</dt>
            <dd>{data.riskDecisionId ?? "Not available"}</dd>
          </div>
          <div className="detail-grid-wide">
            <dt>Alert Reason</dt>
            <dd>{data.alertReason}</dd>
          </div>
        </dl>
      </Card>

      <div className="detail-section">
        <Card title="Related Transaction">
          <dl className="detail-grid">
            <div>
              <dt>Transaction</dt>
              <dd>
                <Link to={`/risk-decisions/${data.transactionId}`}>
                  {data.transactionReference}
                </Link>
              </dd>
            </div>
            <div>
              <dt>Transaction ID</dt>
              <dd>{data.transactionId}</dd>
            </div>
            <div>
              <dt>Customer ID</dt>
              <dd>{data.customerId}</dd>
            </div>
            <div>
              <dt>Account ID</dt>
              <dd>{data.accountId}</dd>
            </div>
          </dl>
        </Card>
      </div>

      <div className="detail-section">
        <Card title="Alert Timeline">
          <dl className="detail-grid">
            <div>
              <dt>Created At</dt>
              <dd>{new Date(data.createdAt).toLocaleString()}</dd>
            </div>
            <div>
              <dt>Updated At</dt>
              <dd>{new Date(data.updatedAt).toLocaleString()}</dd>
            </div>
            <div>
              <dt>Closed At</dt>
              <dd>
                {data.closedAt
                  ? new Date(data.closedAt).toLocaleString()
                  : "Not closed"}
              </dd>
            </div>
          </dl>
        </Card>
      </div>

      <div className="detail-section">
        <Card title="Triggered Rules">
          {triggeredRules.length === 0 ? (
            <p>No structured risk-rule information is available.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Rule</th>
                  <th>Code</th>
                  <th>Score Impact</th>
                  <th>Reason</th>
                </tr>
              </thead>
              <tbody>
                {triggeredRules.map((rule) => (
                  <tr key={rule.ruleCode}>
                    <td>{rule.ruleName}</td>
                    <td>{rule.ruleCode}</td>
                    <td>+{rule.scoreImpact}</td>
                    <td>{rule.reason}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      </div>
    </div>
  );
}
