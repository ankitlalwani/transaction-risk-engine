import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";

import { Badge } from "../../../components/ui/Badge";
import { Card } from "../../../components/ui/Card";
import { getRiskDecisionByTransactionId } from "../api/riskDecisionApi";
import type { RiskDecision } from "../types";

function riskVariant(level: RiskDecision["riskLevel"]) {
  if (level === "CRITICAL") return "danger";
  if (level === "HIGH") return "warning";
  if (level === "MEDIUM") return "neutral";
  return "success";
}

export function RiskDecisionDetailPage() {
  const { transactionId } = useParams();
  const { data, isLoading, isError } = useQuery({
    queryKey: ["risk-decision", transactionId],
    queryFn: () => getRiskDecisionByTransactionId(transactionId!),
    enabled: Boolean(transactionId),
  });

  if (!transactionId) return <p>Transaction ID is missing.</p>;
  if (isLoading) return <p>Loading transaction risk information...</p>;
  if (isError || !data) return <p>Failed to load transaction risk information.</p>;

  return (
    <div>
      <Link className="back-link" to="/risk-decisions">
        Back to risk decisions
      </Link>

      <div className="detail-heading">
        <div>
          <h2>{data.transactionReference}</h2>
          <p className="page-description">
            Risk evaluation information for this transaction.
          </p>
        </div>
        <Badge variant={riskVariant(data.riskLevel)}>{data.riskLevel}</Badge>
      </div>

      <Card title="Decision Summary">
        <dl className="detail-grid">
          <div>
            <dt>Transaction ID</dt>
            <dd>{data.transactionId}</dd>
          </div>
          <div>
            <dt>Risk Score</dt>
            <dd>{data.riskScore}</dd>
          </div>
          <div>
            <dt>Decision Status</dt>
            <dd>{data.decisionStatus}</dd>
          </div>
          <div>
            <dt>Evaluated At</dt>
            <dd>{new Date(data.evaluatedAt).toLocaleString()}</dd>
          </div>
          <div className="detail-grid-wide">
            <dt>Decision Reason</dt>
            <dd>{data.decisionReason}</dd>
          </div>
        </dl>
      </Card>

      <div className="detail-section">
        <Card title="Triggered Rules">
          {data.triggeredRules.length === 0 ? (
            <p>No risk rules were triggered.</p>
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
                {data.triggeredRules.map((rule) => (
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
