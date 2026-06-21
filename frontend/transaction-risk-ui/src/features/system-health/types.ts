export type ServiceHealthStatus = "UP" | "DOWN" | "UNKNOWN";

export type ServiceHealth = {
  id: string;
  label: string;
  status: ServiceHealthStatus;
};
