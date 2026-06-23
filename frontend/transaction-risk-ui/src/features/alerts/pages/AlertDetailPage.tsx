import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { Badge } from "../../../components/ui/Badge";
import { Button } from "../../../components/ui/Button";
import { Card } from "../../../components/ui/Card";
import {
  getAiExplanationByTransactionId,
  getAiExplanationErrorMessage,
} from "../../ai-explanations/api/aiExplanationApi";
import type { AiExplanation } from "../../ai-explanations/types";
import { getAlertById, updateAlertStatus } from "../api/alertApi";
import type {
  AlertPriority,
  AlertStatus,
  AlertTriggeredRule,
  Alert,
} from "../types";

const alertStatuses: AlertStatus[] = [
  "OPEN",
  "IN_REVIEW",
  "ESCALATED",
  "CLOSED",
  "FALSE_POSITIVE",
];

const assignees = [
  "analyst-1",
  "analyst-2",
  "analyst-3",
  "analyst-4",
  "analyst-5",
];

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
  const queryClient = useQueryClient();
  const [isAiExplanationTimedOut, setIsAiExplanationTimedOut] =
    useState(false);
  const { data, isLoading, isError } = useQuery({
    queryKey: ["alert", alertId],
    queryFn: () => getAlertById(alertId!),
    enabled: Boolean(alertId),
  });
  const aiExplanationQuery = useQuery({
    queryKey: ["ai-explanation", data?.transactionId],
    queryFn: () => getAiExplanationByTransactionId(data!.transactionId),
    enabled: Boolean(data?.transactionId),
    refetchInterval: (query) => {
      if (query.state.data) return false;
      if (isAiExplanationTimedOut) return false;

      return 3_000;
    },
  });

  useEffect(() => {
    if (!data?.transactionId || aiExplanationQuery.data) {
      setIsAiExplanationTimedOut(false);
      return;
    }

    setIsAiExplanationTimedOut(false);
    const timeoutId = window.setTimeout(
      () => setIsAiExplanationTimedOut(true),
      60_000
    );

    return () => window.clearTimeout(timeoutId);
  }, [data?.transactionId, aiExplanationQuery.data]);

  const isAiExplanationGenerating =
    aiExplanationQuery.data === null &&
    !isAiExplanationTimedOut;
  const isAiExplanationUnavailable =
    aiExplanationQuery.data === null &&
    isAiExplanationTimedOut;

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
        <Card title="Update Alert">
          <AlertUpdateForm
            alert={data}
            onUpdated={(updatedAlert) => {
              queryClient.setQueryData(["alert", alertId], updatedAlert);
              queryClient.invalidateQueries({ queryKey: ["alerts"] });
            }}
          />
        </Card>
      </div>

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
        <AiExplanationCard
          explanation={aiExplanationQuery.data}
          isLoading={aiExplanationQuery.isLoading}
          isError={aiExplanationQuery.isError}
          isGenerating={isAiExplanationGenerating}
          isUnavailable={isAiExplanationUnavailable}
          errorMessage={
            aiExplanationQuery.error
              ? getAiExplanationErrorMessage(aiExplanationQuery.error)
              : undefined
          }
        />
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

type AiExplanationCardProps = {
  explanation?: AiExplanation | null;
  isLoading: boolean;
  isError: boolean;
  isGenerating: boolean;
  isUnavailable: boolean;
  errorMessage?: string;
};

function AiExplanationCard({
  explanation,
  isLoading,
  isError,
  isGenerating,
  isUnavailable,
  errorMessage,
}: AiExplanationCardProps) {
  if (isLoading) {
    return (
      <Card title="AI Explanation">
        <p>Loading AI explanation...</p>
      </Card>
    );
  }

  if (isError) {
    return (
      <Card title="AI Explanation">
        <div className="error-box">
          {errorMessage ?? "Failed to load AI explanation."}
        </div>
      </Card>
    );
  }

  if (isGenerating) {
    return (
      <Card title="AI Explanation">
        <p>
          AI explanation is being generated through AI. This card will refresh
          automatically when it is ready.
        </p>
      </Card>
    );
  }

  if (isUnavailable || !explanation) {
    return (
      <Card title="AI Explanation">
        <p>
          No AI explanation is available. The generation request did not
          complete within the expected time.
        </p>
      </Card>
    );
  }

  return (
    <Card title="AI Explanation">
      <dl className="detail-grid">
        <div className="detail-grid-wide">
          <dt>Analyst Summary</dt>
          <dd>{explanation.analystSummary}</dd>
        </div>
        <div className="detail-grid-wide">
          <dt>Explanation</dt>
          <dd>{explanation.explanationText}</dd>
        </div>
        <div className="detail-grid-wide">
          <dt>Recommended Action</dt>
          <dd>{explanation.recommendedAction}</dd>
        </div>
      </dl>
    </Card>
  );
}

type AlertUpdateFormProps = {
  alert: Alert;
  onUpdated: (alert: Alert) => void;
};

function AlertUpdateForm({ alert, onUpdated }: AlertUpdateFormProps) {
  const [selectedStatus, setSelectedStatus] = useState<AlertStatus>(
    alert.alertStatus
  );
  const [selectedAssignee, setSelectedAssignee] = useState(
    alert.assignedTo ?? "analyst-1"
  );
  const updateMutation = useMutation({
    mutationFn: () =>
      updateAlertStatus(alert.id, {
        status: selectedStatus,
        assignedTo: selectedAssignee,
      }),
    onSuccess: onUpdated,
  });

  function handleUpdateAlert(event: FormEvent) {
    event.preventDefault();
    updateMutation.mutate();
  }

  return (
    <>
      <form className="alert-update-form" onSubmit={handleUpdateAlert}>
        <label>
          Alert Status
          <select
            value={selectedStatus}
            onChange={(event) =>
              setSelectedStatus(event.target.value as AlertStatus)
            }
          >
            {alertStatuses.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </label>

        <label>
          Assigned To
          <select
            value={selectedAssignee}
            onChange={(event) => setSelectedAssignee(event.target.value)}
          >
            {assignees.map((assignee) => (
              <option key={assignee} value={assignee}>
                {assignee}
              </option>
            ))}
          </select>
        </label>

        <div className="form-actions">
          <Button type="submit" disabled={updateMutation.isPending}>
            {updateMutation.isPending ? "Updating..." : "Update Alert"}
          </Button>
        </div>
      </form>

      {updateMutation.isSuccess && (
        <div className="success-box">Alert updated successfully.</div>
      )}

      {updateMutation.isError && (
        <div className="error-box">Failed to update alert.</div>
      )}
    </>
  );
}
