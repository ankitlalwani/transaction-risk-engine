import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import {
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import { Badge } from "../../components/ui/Badge";
import { Card } from "../../components/ui/Card";
import { getAlerts } from "../alerts/api/alertApi";
import type { Alert, AlertStatus } from "../alerts/types";
import { getRiskDecisions } from "../risk-decisions/api/riskDecisionApi";
import type { RiskDecision } from "../risk-decisions/types";
import { getSystemHealth } from "../system-health/api/systemHealthApi";
import type {
  ServiceHealth,
  ServiceHealthStatus,
} from "../system-health/types";
import { getLatestTransaction } from "../transactions/api/transactionApi";

const ONE_DAY_MS = 24 * 60 * 60 * 1000;
const ONE_HOUR_MS = 60 * 60 * 1000;
const SEVEN_DAYS_MS = 7 * ONE_DAY_MS;
const THIRTY_MINUTES_MS = 30 * 60 * 1000;
const TWO_HOURS_MS = 2 * 60 * 60 * 1000;
type TimeRangeFilter = "1h" | "24h" | "7d";
type RiskLevelFilter = "ALL" | RiskDecision["riskLevel"];
type AlertStatusFilter = "ALL" | AlertStatus;
type AnalystFilter = "ALL" | "UNASSIGNED" | string;

const timeRangeOptions: Array<{ value: TimeRangeFilter; label: string; durationMs: number }> = [
  { value: "1h", label: "Last 1h", durationMs: ONE_HOUR_MS },
  { value: "24h", label: "Last 24h", durationMs: ONE_DAY_MS },
  { value: "7d", label: "Last 7d", durationMs: SEVEN_DAYS_MS },
];
const riskLevelOptions: RiskLevelFilter[] = [
  "ALL",
  "LOW",
  "MEDIUM",
  "HIGH",
  "CRITICAL",
];
const alertStatusOptions: AlertStatusFilter[] = [
  "ALL",
  "OPEN",
  "IN_REVIEW",
  "ESCALATED",
  "CLOSED",
  "FALSE_POSITIVE",
];
const analystOptions = [
  "analyst-1",
  "analyst-2",
  "analyst-3",
  "analyst-4",
  "analyst-5",
];
const riskDistributionConfig = [
  { level: "LOW", color: "#16a34a" },
  { level: "MEDIUM", color: "#0284c7" },
  { level: "HIGH", color: "#d97706" },
  { level: "CRITICAL", color: "#dc2626" },
] as const;

function isWithinTimeRange(value: string, range: TimeRangeFilter) {
  const selectedRange = timeRangeOptions.find((option) => option.value === range);

  return (
    Date.now() - new Date(value).getTime() <=
    (selectedRange?.durationMs ?? ONE_DAY_MS)
  );
}

function formatDateTime(value?: string) {
  return value ? new Date(value).toLocaleString() : "Waiting for event";
}

function formatAge(value: string) {
  const diffMs = Date.now() - new Date(value).getTime();
  const minutes = Math.max(0, Math.floor(diffMs / (60 * 1000)));

  if (minutes < 60) return `${minutes}m`;

  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ${minutes % 60}m`;

  const days = Math.floor(hours / 24);
  return `${days}d ${hours % 24}h`;
}

function ageInMs(value: string) {
  return Date.now() - new Date(value).getTime();
}

function priorityRank(alert: Alert) {
  const priorityWeight = {
    CRITICAL: 4,
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1,
  }[alert.alertPriority];
  const statusWeight =
    alert.alertStatus === "OPEN"
      ? 4
      : alert.alertStatus === "ESCALATED"
        ? 3
        : alert.alertStatus === "IN_REVIEW"
          ? 2
          : 1;
  const assigneeWeight = alert.assignedTo ? 0 : 1;

  return priorityWeight * 100 + statusWeight * 10 + assigneeWeight;
}

function riskVariant(level: Alert["riskLevel"]) {
  if (level === "CRITICAL") return "danger";
  if (level === "HIGH") return "warning";
  if (level === "MEDIUM") return "neutral";
  return "success";
}

function alertStatusVariant(status: Alert["alertStatus"]) {
  if (status === "CLOSED" || status === "FALSE_POSITIVE") return "success";
  if (status === "ESCALATED") return "danger";
  if (status === "IN_REVIEW") return "warning";
  return "neutral";
}

function statusFromQuery(
  isSuccess: boolean,
  isError: boolean,
  fallback?: ServiceHealthStatus
): ServiceHealthStatus {
  if (isSuccess) return "UP";
  if (isError) return fallback === "UP" ? "UP" : "DOWN";
  return fallback ?? "UNKNOWN";
}

export function DashboardPage() {
  const [timeRange, setTimeRange] = useState<TimeRangeFilter>("24h");
  const [riskLevel, setRiskLevel] = useState<RiskLevelFilter>("ALL");
  const [alertStatus, setAlertStatus] = useState<AlertStatusFilter>("ALL");
  const [assignedAnalyst, setAssignedAnalyst] = useState<AnalystFilter>("ALL");
  const alertsQuery = useQuery({
    queryKey: ["alerts"],
    queryFn: () => getAlerts(),
    refetchInterval: 5000,
  });

  const decisionsQuery = useQuery({
    queryKey: ["risk-decisions"],
    queryFn: getRiskDecisions,
    refetchInterval: 5000,
  });

  const latestTransactionQuery = useQuery({
    queryKey: ["latest-transaction"],
    queryFn: getLatestTransaction,
    refetchInterval: 5000,
  });
  const { data: healthChecks = [] } = useQuery({
    queryKey: ["system-health"],
    queryFn: getSystemHealth,
    refetchInterval: 10000,
  });
  const alerts = alertsQuery.data ?? [];
  const decisions = decisionsQuery.data ?? [];
  const latestTransaction = latestTransactionQuery.data;
  const visibleAlerts = useMemo(
    () =>
      alerts.filter((alert) => {
        const matchesTimeRange = isWithinTimeRange(alert.createdAt, timeRange);
        const matchesRisk =
          riskLevel === "ALL" || alert.riskLevel === riskLevel;
        const matchesStatus =
          alertStatus === "ALL" || alert.alertStatus === alertStatus;
        const matchesAnalyst =
          assignedAnalyst === "ALL" ||
          (assignedAnalyst === "UNASSIGNED"
            ? !alert.assignedTo
            : alert.assignedTo === assignedAnalyst);

        return (
          matchesTimeRange &&
          matchesRisk &&
          matchesStatus &&
          matchesAnalyst
        );
      }),
    [alerts, alertStatus, assignedAnalyst, riskLevel, timeRange]
  );
  const visibleDecisions = useMemo(
    () =>
      decisions.filter((decision) => {
        const matchesTimeRange = isWithinTimeRange(decision.evaluatedAt, timeRange);
        const matchesRisk =
          riskLevel === "ALL" || decision.riskLevel === riskLevel;

        return matchesTimeRange && matchesRisk;
      }),
    [decisions, riskLevel, timeRange]
  );
  const selectedTimeRangeLabel =
    timeRangeOptions.find((option) => option.value === timeRange)?.label ??
    "Last 24h";
  const healthById = Object.fromEntries(
    healthChecks.map((service) => [service.id, service.status])
  ) as Record<string, ServiceHealthStatus | undefined>;
  const transactionServiceStatus = statusFromQuery(
    latestTransactionQuery.isSuccess,
    latestTransactionQuery.isError,
    healthById["transaction-service"]
  );
  const riskEngineStatus = statusFromQuery(
    decisionsQuery.isSuccess,
    decisionsQuery.isError,
    healthById["risk-engine"]
  );
  const alertServiceStatus = statusFromQuery(
    alertsQuery.isSuccess,
    alertsQuery.isError,
    healthById["alert-service"]
  );
  const aiExplanationServiceStatus =
    healthById["ai-explanation-service"] ?? "UNKNOWN";
  const kafkaStatus =
    transactionServiceStatus === "UP" &&
    riskEngineStatus === "UP" &&
    alertServiceStatus === "UP"
      ? "UP"
      : "UNKNOWN";
  const systemHealth: ServiceHealth[] = [
    {
      id: "transaction-service",
      label: "Transaction Service",
      status: transactionServiceStatus,
    },
    {
      id: "risk-engine",
      label: "Risk Engine",
      status: riskEngineStatus,
    },
    {
      id: "alert-service",
      label: "Alert Service",
      status: alertServiceStatus,
    },
    {
      id: "ai-explanation-service",
      label: "AI Explanation Service",
      status: aiExplanationServiceStatus,
    },
    {
      id: "kafka",
      label: "Kafka",
      status: kafkaStatus,
    },
  ];

  const highCriticalInRange = visibleDecisions.filter(
    (decision) =>
      decision.riskLevel === "HIGH" || decision.riskLevel === "CRITICAL"
  ).length;
  const openCriticalAlerts = visibleAlerts.filter(
    (alert) =>
      alert.alertStatus === "OPEN" && alert.alertPriority === "CRITICAL"
  ).length;
  const averageRiskScore =
    visibleDecisions.length > 0
      ? Math.round(
          visibleDecisions.reduce(
            (total, decision) => total + decision.riskScore,
            0
          ) / visibleDecisions.length
        )
      : 0;
  const alertConversionRate =
    visibleDecisions.length > 0
      ? Math.round(
          (visibleAlerts.length / visibleDecisions.length) *
            100
        )
      : 0;
  const riskDistribution = riskDistributionConfig.map((item) => ({
    ...item,
    count: visibleDecisions.filter((decision) => decision.riskLevel === item.level)
      .length,
  }));
  const hasRiskDistribution = riskDistribution.some((item) => item.count > 0);
  const urgentAlerts = [...visibleAlerts]
    .filter((alert) => alert.alertStatus !== "CLOSED")
    .sort((a, b) => {
      const rankDifference = priorityRank(b) - priorityRank(a);

      if (rankDifference !== 0) return rankDifference;

      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    })
    .slice(0, 6);
  const criticalAlertsOver30Minutes = visibleAlerts.filter(
    (alert) =>
      alert.alertPriority === "CRITICAL" &&
      alert.alertStatus !== "CLOSED" &&
      alert.alertStatus !== "FALSE_POSITIVE" &&
      ageInMs(alert.createdAt) > THIRTY_MINUTES_MS
  );
  const inReviewAlertsOver2Hours = visibleAlerts.filter(
    (alert) =>
      alert.alertStatus === "IN_REVIEW" &&
      ageInMs(alert.updatedAt) > TWO_HOURS_MS
  );
  const unassignedHighRiskAlerts = visibleAlerts.filter(
    (alert) =>
      (alert.riskLevel === "HIGH" || alert.riskLevel === "CRITICAL") &&
      alert.alertStatus !== "CLOSED" &&
      alert.alertStatus !== "FALSE_POSITIVE" &&
      !alert.assignedTo
  );
  const slaExceptions = [
    ...criticalAlertsOver30Minutes.map((alert) => ({
      alert,
      label: "Critical > 30m",
      severity: "critical" as const,
      ageSource: alert.createdAt,
    })),
    ...inReviewAlertsOver2Hours.map((alert) => ({
      alert,
      label: "In Review > 2h",
      severity: "warning" as const,
      ageSource: alert.updatedAt,
    })),
    ...unassignedHighRiskAlerts.map((alert) => ({
      alert,
      label: "Unassigned High Risk",
      severity: "warning" as const,
      ageSource: alert.createdAt,
    })),
  ]
    .sort((a, b) => {
      const severityDifference =
        (b.severity === "critical" ? 1 : 0) -
        (a.severity === "critical" ? 1 : 0);

      if (severityDifference !== 0) return severityDifference;

      return (
        new Date(a.ageSource).getTime() - new Date(b.ageSource).getTime()
      );
    })
    .slice(0, 5);
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
  const alertExpected =
    latestDecision?.riskLevel === "HIGH" ||
    latestDecision?.riskLevel === "CRITICAL";
  const alertNotCreated = Boolean(latestDecision && !alertExpected && !latestAlert);
  const alertCreationStatus = latestAlert
    ? "Completed"
    : alertNotCreated
      ? "Skipped"
      : "Pending";
  const riskEvaluationStatus = latestDecision
    ? `Completed (${latestDecision.riskLevel})`
    : "Pending";
  const overallStatus = latestAlert
    ? "Alert Created"
    : alertNotCreated
      ? `Alert Skipped (${latestDecision?.riskLevel} Risk)`
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

      <div className="dashboard-filter-section">
        <Card title="Filters" variant="slate">
          <div className="dashboard-filters">
            <label>
              Time range
              <select
                value={timeRange}
                onChange={(event) =>
                  setTimeRange(event.target.value as TimeRangeFilter)
                }
              >
                {timeRangeOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Risk level
              <select
                value={riskLevel}
                onChange={(event) =>
                  setRiskLevel(event.target.value as RiskLevelFilter)
                }
              >
                {riskLevelOptions.map((option) => (
                  <option key={option} value={option}>
                    {option === "ALL" ? "All Risk Levels" : option}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Alert status
              <select
                value={alertStatus}
                onChange={(event) =>
                  setAlertStatus(event.target.value as AlertStatusFilter)
                }
              >
                {alertStatusOptions.map((option) => (
                  <option key={option} value={option}>
                    {option === "ALL" ? "All Alert Statuses" : option}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Assigned analyst
              <select
                value={assignedAnalyst}
                onChange={(event) =>
                  setAssignedAnalyst(event.target.value as AnalystFilter)
                }
              >
                <option value="ALL">All Analysts</option>
                <option value="UNASSIGNED">Unassigned</option>
                {analystOptions.map((analyst) => (
                  <option key={analyst} value={analyst}>
                    {analyst}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <p className="filter-summary">
            Showing {visibleDecisions.length} risk decisions and{" "}
            {visibleAlerts.length} alerts for {selectedTimeRangeLabel.toLowerCase()}.
          </p>
        </Card>
      </div>

      <div className="dashboard-section">
        <Card title="System Health" variant="slate">
          <div className="system-health-grid">
            {systemHealth.length === 0 ? (
              <p className="empty-state">Checking platform health...</p>
            ) : (
              systemHealth.map((service) => (
                <SystemHealthTile key={service.id} service={service} />
              ))
            )}
          </div>
        </Card>
      </div>

      <div className="dashboard-section">
        <div className="section-heading">
          <div>
            <h3>Operational Metrics</h3>
            <p className="section-description">
              Operational view scoped to the selected dashboard filters.
            </p>
          </div>
          <Badge variant="neutral">{selectedTimeRangeLabel}</Badge>
        </div>

        <div className="metric-grid">
          <Card title="Risk Decisions" variant="blue">
            <div className="metric-value">{visibleDecisions.length}</div>
          </Card>

          <Card title="High/Critical Risk" variant="amber">
            <div className="metric-value">{highCriticalInRange}</div>
          </Card>

          <Card title="Alerts Created" variant="green">
            <div className="metric-value">{visibleAlerts.length}</div>
          </Card>

          <Card title="Open Critical Alerts" variant="red">
            <div className="metric-value">{openCriticalAlerts}</div>
          </Card>

          <Card title="Avg Risk Score" variant="purple">
            <div className="metric-value">{averageRiskScore}</div>
          </Card>

          <Card title="Alert Conversion" variant="slate">
            <div className="metric-value">{alertConversionRate}%</div>
          </Card>
        </div>
      </div>

      <div className="dashboard-section">
        <div className="section-heading">
          <div>
            <h3>SLA & Aging Indicators</h3>
            <p className="section-description">
              Exceptions that need attention based on alert age and ownership.
            </p>
          </div>
          <Badge variant="neutral">Attention Now</Badge>
        </div>

        <div className="sla-grid">
          <Card title="Critical Alerts > 30m" variant="red">
            <div className="metric-value">
              {criticalAlertsOver30Minutes.length}
            </div>
          </Card>

          <Card title="In Review > 2h" variant="amber">
            <div className="metric-value">{inReviewAlertsOver2Hours.length}</div>
          </Card>

          <Card title="Unassigned High Risk" variant="amber">
            <div className="metric-value">{unassignedHighRiskAlerts.length}</div>
          </Card>
        </div>

        <Card title="Aging Exceptions" variant="slate">
          {slaExceptions.length === 0 ? (
            <p className="empty-state">No SLA exceptions are currently active.</p>
          ) : (
            <div className="sla-exception-list">
              {slaExceptions.map(({ alert, label, severity, ageSource }) => (
                <div key={`${label}-${alert.id}`} className="sla-exception-row">
                  <div>
                    <Badge
                      variant={severity === "critical" ? "danger" : "warning"}
                    >
                      {label}
                    </Badge>
                    <Link to={`/alerts/${alert.id}`}>
                      {alert.alertReference}
                    </Link>
                  </div>
                  <span>{alert.assignedTo ?? "Unassigned"}</span>
                  <strong>{formatAge(ageSource)}</strong>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      <div className="dashboard-section">
        <Card title="Risk Distribution" variant="slate">
          {hasRiskDistribution ? (
            <div className="risk-distribution-card">
              <div className="risk-chart">
                <ResponsiveContainer width="100%" height={240}>
                  <PieChart>
                    <Pie
                      data={riskDistribution}
                      dataKey="count"
                      nameKey="level"
                      innerRadius={62}
                      outerRadius={92}
                      paddingAngle={3}
                    >
                      {riskDistribution.map((item) => (
                        <Cell key={item.level} fill={item.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(value, name) => [
                        value,
                        `${name} Risk`,
                      ]}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <div className="risk-distribution-legend">
                {riskDistribution.map((item) => (
                  <div key={item.level} className="risk-legend-row">
                    <span
                      className="risk-legend-dot"
                      style={{ backgroundColor: item.color }}
                    />
                    <span>{item.level}</span>
                    <strong>{item.count}</strong>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <p className="empty-state">
              No risk decisions are available for distribution yet.
            </p>
          )}
        </Card>
      </div>

      <div className="dashboard-section">
        <Card title="Alert Work Queue Preview" variant="slate">
          {urgentAlerts.length === 0 ? (
            <p className="empty-state">No open alerts need analyst attention.</p>
          ) : (
            <div className="work-queue-table">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Alert</th>
                    <th>Risk Level</th>
                    <th>Status</th>
                    <th>Assigned To</th>
                    <th>Age</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {urgentAlerts.map((alert) => (
                    <tr key={alert.id}>
                      <td>
                        <Link to={`/alerts/${alert.id}`}>
                          {alert.alertReference}
                        </Link>
                      </td>
                      <td>
                        <Badge variant={riskVariant(alert.riskLevel)}>
                          {alert.riskLevel}
                        </Badge>
                      </td>
                      <td>
                        <Badge variant={alertStatusVariant(alert.alertStatus)}>
                          {alert.alertStatus}
                        </Badge>
                      </td>
                      <td>{alert.assignedTo ?? "Unassigned"}</td>
                      <td>{formatAge(alert.createdAt)}</td>
                      <td>
                        <Link
                          className="table-action-link"
                          to={`/alerts/${alert.id}`}
                        >
                          Review
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </div>

      <div className="dashboard-section">
        <Card title="Latest Pipeline Run" variant="slate">
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
                    RISK_EVALUATED{" "}
                    {latestDecision ? `\u2713 ${latestDecision.riskLevel}` : "Pending"}
                  </Badge>
                  <Badge
                    variant={
                      latestAlert
                        ? "success"
                        : alertNotCreated
                          ? "neutral"
                          : "default"
                    }
                  >
                    ALERT_CREATED{" "}
                    {latestAlert
                      ? "\u2713"
                      : alertNotCreated
                        ? "Skipped"
                        : "Pending"}
                  </Badge>
                </div>
              </div>

              <div className="pipeline-flow" aria-label="Latest event flow">
                <PipelineStep
                  title="Transaction Submitted"
                  status="Completed"
                  detail="Transaction ingestion accepted the event."
                  timestamp={latestKnownTransaction.createdAt}
                  variant="complete"
                />
                <PipelineStep
                  title="Risk Evaluated"
                  status={latestDecision ? "Completed" : "Pending"}
                  detail={
                    latestDecision
                      ? `${latestDecision.riskLevel} risk, score ${latestDecision.riskScore}`
                      : "Waiting for risk engine decision."
                  }
                  timestamp={latestDecision?.evaluatedAt}
                  variant={latestDecision ? "complete" : "pending"}
                />
                <PipelineStep
                  title="Alert Handling"
                  status={alertCreationStatus}
                  detail={
                    latestAlert
                      ? `${latestAlert.alertPriority} alert ${latestAlert.alertReference}`
                      : alertNotCreated
                        ? `${latestDecision?.riskLevel} risk does not require alert creation.`
                        : "Waiting for alert decision."
                  }
                  timestamp={latestAlert?.createdAt}
                  footer={
                    alertNotCreated
                      ? `Completed after ${latestDecision?.riskLevel} risk evaluation`
                      : undefined
                  }
                  variant={
                    latestAlert
                      ? "complete"
                      : alertNotCreated
                        ? "skipped"
                        : "pending"
                  }
                />
              </div>

              <dl className="pipeline-summary">
                <div>
                  <dt>Transaction Ingestion</dt>
                  <dd>Completed</dd>
                </div>
                <div>
                  <dt>Risk Evaluation</dt>
                  <dd>{riskEvaluationStatus}</dd>
                </div>
                <div>
                  <dt>Alert Creation</dt>
                  <dd>{alertCreationStatus}</dd>
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

type SystemHealthTileProps = {
  service: ServiceHealth;
};

function SystemHealthTile({ service }: SystemHealthTileProps) {
  const label =
    service.id === "kafka" && service.status === "UP" ? "CONNECTED" : service.status;

  return (
    <div className={`system-health-tile system-health-${service.status.toLowerCase()}`}>
      <span className="system-health-dot" />
      <div>
        <strong>{service.label}</strong>
        <span>{label}</span>
      </div>
    </div>
  );
}

type PipelineStepProps = {
  title: string;
  status: string;
  detail: string;
  timestamp?: string;
  footer?: string;
  variant: "complete" | "pending" | "skipped";
};

function PipelineStep({
  title,
  status,
  detail,
  timestamp,
  footer,
  variant,
}: PipelineStepProps) {
  return (
    <div className={`pipeline-timeline-step pipeline-timeline-step-${variant}`}>
      <div className="pipeline-step-topline">
        <span>{title}</span>
        <Badge
          variant={
            variant === "complete"
              ? "success"
              : variant === "skipped"
                ? "neutral"
                : "default"
          }
        >
          {status}
        </Badge>
      </div>
      <p>{detail}</p>
      <time>{footer ?? formatDateTime(timestamp)}</time>
    </div>
  );
}
