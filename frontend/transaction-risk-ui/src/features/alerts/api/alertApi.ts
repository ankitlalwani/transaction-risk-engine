import axios from "axios";
import { apiConfig } from "../../../config/apiConfig";
import type { Alert, AlertPriority, AlertStatus, UpdateAlertStatusRequest } from "../types";

const alertClient = axios.create({
  baseURL: apiConfig.alertApiBaseUrl,
});

export async function getAlerts(filters?: {
  status?: AlertStatus;
  priority?: AlertPriority;
  riskLevel?: string;
}): Promise<Alert[]> {
  const response = await alertClient.get<Alert[]>("/api/v1/alerts", {
    params: filters,
  });

  return response.data;
}

export async function getAlertById(alertId: string): Promise<Alert> {
  const response = await alertClient.get<Alert>(`/api/v1/alerts/${alertId}`);
  return response.data;
}

export async function updateAlertStatus(
  alertId: string,
  request: UpdateAlertStatusRequest
): Promise<Alert> {
  const response = await alertClient.patch<Alert>(
    `/api/v1/alerts/${alertId}`,
    request
  );

  return response.data;
}
