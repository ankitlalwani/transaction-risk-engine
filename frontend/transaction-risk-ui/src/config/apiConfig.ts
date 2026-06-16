export const apiConfig = {
  transactionApiBaseUrl:
    import.meta.env.VITE_TRANSACTION_API_BASE_URL ?? "http://localhost:8080",

  riskApiBaseUrl:
    import.meta.env.VITE_RISK_API_BASE_URL ?? "http://localhost:8081",

  alertApiBaseUrl:
    import.meta.env.VITE_ALERT_API_BASE_URL ?? "http://localhost:8082",
};