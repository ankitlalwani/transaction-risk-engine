import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Badge } from "../../components/ui/Badge";
import { Card } from "../../components/ui/Card";
import { getAlerts } from "../alerts/api/alertApi";
import { getRiskDecisions } from "../risk-decisions/api/riskDecisionApi";
import { getLatestTransaction } from "../transactions/api/transactionApi";

export function DashboardPage() {
  const { data: alerts = [] } = useQuery({
    queryKey: ["alerts"],
    queryFn: () => getAlerts(),
    refetchInterval: 5000,
  });

  const { data: decisions = [] } = useQuery({
    queryKey: ["risk-decisions"],
    queryFn: getRiskDecisions,
    refetchInterval: 5000,
  });

  const { data: latestTransaction } = useQuery({
    queryKey: ["latest-transaction"],
    queryFn: getLatestTransaction,
    refetchInterval: 5000,
  });

  const openAlerts = alerts.filter((a) => a.alertStatus === "OPEN").length;
  const criticalAlerts = alerts.filter((a) => a.alertPriority === "CRITICAL").length;
  const highRiskDecisions = decisions.filter(
    (d) => d.riskLevel === "HIGH" || d.riskLevel === "CRITICAL"
  ).length;
  const latestKnownTransaction =
    latestTransaction ??
    (decisions[0]
      ? {
          transactionId: decisions[0].transactionId,
          transactionReference: decisions[0].transactionReference,
          status: "RECEIVED",
          createdAt: decisions[0].evaluatedAt,
        }
      : null);
  const latestDecision = latestKnownTransaction
    ? decisions.find(
        (decision) =>
          decision.transactionId === latestKnownTransaction.transactionId
      )
    : undefined;
  const latestAlert = latestKnownTransaction
    ? alerts.find(
        (alert) => alert.transactionId === latestKnownTransaction.transactionId
      )
    : undefined;
  const overallStatus = latestAlert
    ? "Alert Created"
    : latestDecision
      ? "Risk Evaluated"
      : latestKnownTransaction
        ? "Transaction Ingested"
        : "No Pipeline Runs";

  return (
    <div>
      <h2>Dashboard</h2>
      <p className="page-description">
        Live overview of risk decisions and alerts.
      </p>

      <div className="metric-grid">
        <Card title="Risk Decisions">
          <div className="metric-value">{decisions.length}</div>
        </Card>

        <Card title="High/Critical Risk">
          <div className="metric-value">{highRiskDecisions}</div>
        </Card>

        <Card title="Open Alerts">
          <div className="metric-value">{openAlerts}</div>
        </Card>

        <Card title="Critical Alerts">
          <div className="metric-value">{criticalAlerts}</div>
        </Card>
      </div>

      <div className="dashboard-section">
        <Card title="Latest Pipeline Run">
          {latestKnownTransaction ? (
            <>
              <div className="pipeline-header">
                <div>
                  <Link
                    className="pipeline-reference"
                    to={
                      latestAlert
                        ? `/alerts/${latestAlert.id}`
                        : latestDecision
                          ? `/risk-decisions/${latestKnownTransaction.transactionId}`
                          : "/transactions/new"
                    }
                  >
                    {latestKnownTransaction.transactionReference}
                  </Link>
                  <div className="pipeline-time">
                    {new Date(latestKnownTransaction.createdAt).toLocaleString()}
                  </div>
                </div>

                <div className="pipeline-chips">
                  <Badge variant="success">INGESTED &#10003;</Badge>
                  <Badge variant={latestDecision ? "success" : "default"}>
                    RISK_EVALUATED {latestDecision ? "\u2713" : "Pending"}
                  </Badge>
                  <Badge variant={latestAlert ? "success" : "default"}>
                    ALERT_CREATED {latestAlert ? "\u2713" : "Pending"}
                  </Badge>
                </div>
              </div>

              <div className="pipeline-flow" aria-label="Latest event flow">
                <div className="pipeline-step pipeline-step-complete">
                  <span>Transaction Submitted</span>
                  <strong>Completed</strong>
                </div>
                <span className="pipeline-connector">→</span>
                <div
                  className={`pipeline-step ${
                    latestDecision ? "pipeline-step-complete" : ""
                  }`}
                >
                  <span>Risk Evaluated</span>
                  <strong>{latestDecision ? "Completed" : "Pending"}</strong>
                </div>
                <span className="pipeline-connector">→</span>
                <div
                  className={`pipeline-step ${
                    latestAlert ? "pipeline-step-complete" : ""
                  }`}
                >
                  <span>Alert Created</span>
                  <strong>{latestAlert ? "Completed" : "Pending"}</strong>
                </div>
              </div>

              <dl className="pipeline-summary">
                <div>
                  <dt>Transaction Ingestion</dt>
                  <dd>Completed</dd>
                </div>
                <div>
                  <dt>Risk Evaluation</dt>
                  <dd>{latestDecision ? "Completed" : "Pending"}</dd>
                </div>
                <div>
                  <dt>Alert Creation</dt>
                  <dd>{latestAlert ? "Completed" : "Pending"}</dd>
                </div>
                <div className="pipeline-overall">
                  <dt>Overall Status</dt>
                  <dd>{overallStatus}</dd>
                </div>
              </dl>
            </>
          ) : (
            <p>No transactions have been submitted yet.</p>
          )}
        </Card>
      </div>
    </div>
  );
}
