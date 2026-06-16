import { Card } from "../../components/ui/Card";

export function SystemFlowPage() {
  return (
    <div>
      <h2>System Flow</h2>
      <p className="page-description">
        End-to-end event-driven flow across backend services.
      </p>

      <Card title="Event Pipeline">
        <div className="flow">
          <div className="flow-box">Client / UI</div>
          <div className="flow-arrow">↓</div>
          <div className="flow-box">transaction-ingestion-service</div>
          <div className="flow-topic">Kafka: transaction.created.v1</div>
          <div className="flow-arrow">↓</div>
          <div className="flow-box">risk-engine-service</div>
          <div className="flow-topic">Kafka: risk.evaluated.v1</div>
          <div className="flow-arrow">↓</div>
          <div className="flow-box">alert-service</div>
          <div className="flow-arrow">↓</div>
          <div className="flow-box">alerts table</div>
        </div>
      </Card>
    </div>
  );
}