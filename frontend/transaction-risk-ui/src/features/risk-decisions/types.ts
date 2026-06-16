export type TriggeredRule = {
  ruleCode: string;
  ruleName: string;
  scoreImpact: number;
  reason: string;
};

export type RiskDecision = {
  transactionId: string;
  transactionReference: string;
  riskScore: number;
  riskLevel: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  decisionStatus: "APPROVED" | "MONITOR" | "REVIEW_REQUIRED" | "BLOCK_RECOMMENDED";
  decisionReason: string;
  triggeredRules: TriggeredRule[];
  evaluatedAt: string;
};
