import axios from "axios";

import { apiConfig } from "../../../config/apiConfig";
import type { ServiceHealth, ServiceHealthStatus } from "../types";

type ActuatorHealthResponse = {
  status?: string;
};

const systemHealthClient = axios.create({
  baseURL: apiConfig.systemHealthApiBaseUrl,
});

const serviceHealthChecks = [
  {
    id: "transaction-service",
    label: "Transaction Service",
    path: "/system-health/transaction-service",
  },
  {
    id: "risk-engine",
    label: "Risk Engine",
    path: "/system-health/risk-engine",
  },
  {
    id: "alert-service",
    label: "Alert Service",
    path: "/system-health/alert-service",
  },
  {
    id: "ai-explanation-service",
    label: "AI Explanation Service",
    path: "/system-health/ai-explanation-service",
  },
] as const;

function normalizeStatus(status?: string): ServiceHealthStatus {
  if (status === "UP") return "UP";
  if (status === "DOWN") return "DOWN";
  return "UNKNOWN";
}

async function getServiceHealth(
  check: (typeof serviceHealthChecks)[number]
): Promise<ServiceHealth> {
  try {
    const response = await systemHealthClient.get<ActuatorHealthResponse>(
      check.path
    );

    return {
      id: check.id,
      label: check.label,
      status: normalizeStatus(response.data.status),
    };
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return {
        id: check.id,
        label: check.label,
        status: "UNKNOWN",
      };
    }

    return {
      id: check.id,
      label: check.label,
      status: "DOWN",
    };
  }
}

export async function getSystemHealth(): Promise<ServiceHealth[]> {
  const services = await Promise.all(serviceHealthChecks.map(getServiceHealth));
  const kafkaStatus = services.every((service) => service.status === "UP")
    ? "UP"
    : "UNKNOWN";

  return [
    ...services,
    {
      id: "kafka",
      label: "Kafka",
      status: kafkaStatus,
    },
  ];
}
