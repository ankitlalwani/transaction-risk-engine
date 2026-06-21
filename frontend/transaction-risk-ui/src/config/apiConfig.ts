export const apiConfig = {
  transactionApiBaseUrl:
    import.meta.env.VITE_TRANSACTION_API_BASE_URL ?? "http://localhost:8088",

  riskApiBaseUrl:
    import.meta.env.VITE_RISK_API_BASE_URL ?? "http://localhost:8088",

  alertApiBaseUrl:
    import.meta.env.VITE_ALERT_API_BASE_URL ?? "http://localhost:8088",

  aiExplanationApiBaseUrl:
    import.meta.env.VITE_AI_EXPLANATION_API_BASE_URL ?? "http://localhost:8088",

  systemHealthApiBaseUrl:
    import.meta.env.VITE_SYSTEM_HEALTH_API_BASE_URL ?? "http://localhost:8088",
};
