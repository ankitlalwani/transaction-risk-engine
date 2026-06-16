import { CreateTransactionForm } from "../components/CreateTransactionForm";

export function CreateTransactionPage() {
  return (
    <div>
      <h2>Create Transaction</h2>
      <p className="page-description">
        Submit a transaction to start the ingestion → risk evaluation → alert pipeline.
      </p>

      <CreateTransactionForm />
    </div>
  );
}