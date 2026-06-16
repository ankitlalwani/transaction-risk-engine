import axios from "axios";
import { apiConfig } from "../../../config/apiConfig";
import type { RiskDecision } from "../types";

const riskClient = axios.create({
  baseURL: apiConfig.riskApiBaseUrl,
});

export async function getRiskDecisions(): Promise<RiskDecision[]> {
  const response = await riskClient.get<RiskDecision[]>("/api/v1/risk-decisions");
  return response.data;
}

export async function getRiskDecisionByTransactionId(
  transactionId: string
): Promise<RiskDecision> {
  const response = await riskClient.get<RiskDecision>(
    `/api/v1/risk-decisions/${transactionId}`
  );

  return response.data;
}