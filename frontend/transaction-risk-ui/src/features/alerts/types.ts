export type AlertStatus =
  | "OPEN"
  | "IN_REVIEW"
  | "ESCALATED"
  | "CLOSED"
  | "FALSE_POSITIVE";

export type AlertPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export type AlertTriggeredRule = {
  ruleCode: string;
  ruleName: string;
  scoreImpact: number;
  reason: string;
};

export type Alert = {
  id: string;
  alertReference: string;
  transactionId: string;
  transactionReference: string;
  riskDecisionId?: string;
  customerId: string;
  accountId: string;
  riskScore: number;
  riskLevel: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  decisionStatus: string;
  alertStatus: AlertStatus;
  alertPriority: AlertPriority;
  alertReason: string;
  triggeredRules: string;
  assignedTo?: string | null;
  createdAt: string;
  updatedAt: string;
  closedAt?: string | null;
};

export type UpdateAlertStatusRequest = {
  status: AlertStatus;
  assignedTo?: string | null;
};
