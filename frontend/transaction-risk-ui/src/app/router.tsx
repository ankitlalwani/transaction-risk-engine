import { createBrowserRouter } from "react-router-dom";

import { AppLayout } from "../components/layout/AppLayout";
import { DashboardPage } from "../features/dashboard/DashboardPage";
import { CreateTransactionPage } from "../features/transactions/pages/CreateTransactionPage";
import { RiskDecisionsPage } from "../features/risk-decisions/pages/RiskDecisionsPage";
import { RiskDecisionDetailPage } from "../features/risk-decisions/pages/RiskDecisionDetailPage";
import { AlertsPage } from "../features/alerts/pages/AlertsPage";
import { AlertDetailPage } from "../features/alerts/pages/AlertDetailPage";
import { SystemFlowPage } from "../features/system-flow/SystemFlowPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <DashboardPage /> },
      { path: "transactions/new", element: <CreateTransactionPage /> },
      { path: "risk-decisions", element: <RiskDecisionsPage /> },
      {
        path: "risk-decisions/:transactionId",
        element: <RiskDecisionDetailPage />,
      },
      { path: "alerts", element: <AlertsPage /> },
      { path: "alerts/:alertId", element: <AlertDetailPage /> },
      { path: "system-flow", element: <SystemFlowPage /> },
    ],
  },
]);
