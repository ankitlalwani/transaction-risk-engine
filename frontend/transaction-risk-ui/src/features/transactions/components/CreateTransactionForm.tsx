import { useState } from "react";
import type { FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";

import { Button } from "../../../components/ui/Button";
import { Card } from "../../../components/ui/Card";
import {
  createTransaction,
  getTransactionErrorMessage,
} from "../api/transactionApi";
import type { CreateTransactionRequest } from "../types";

type DemoScenario = {
  label: string;
  payload: Omit<
    CreateTransactionRequest,
    "idempotencyKey" | "transactionTime"
  >;
};

const demoScenarios: DemoScenario[] = [
  {
    label: "Create Low Risk Card Purchase",
    payload: {
      externalCustomerId: "CUST-1002",
      externalAccountId: "ACC-9003",
      transactionType: "PURCHASE",
      paymentChannel: "CARD",
      amount: 189.99,
      currency: "USD",
      merchantName: "Demo Online Store",
      merchantCategory: "ECOMMERCE",
      merchantCountry: "USA",
      sourceIp: "172.16.10.45",
      deviceId: "demo-web-device-001",
    },
  },
  {
    label: "Create Medium Risk Mobile Transfer",
    payload: {
      externalCustomerId: "CUST-1001",
      externalAccountId: "ACC-9001",
      transactionType: "TRANSFER",
      paymentChannel: "MOBILE_APP",
      amount: 6000,
      currency: "USD",
      merchantName: null,
      merchantCategory: "PERSON_TO_PERSON",
      merchantCountry: "USA",
      sourceIp: "192.168.1.20",
      deviceId: "demo-mobile-device-002",
    },
  },
  {
    label: "Create Critical Wire Transfer",
    payload: {
      externalCustomerId: "CUST-1001",
      externalAccountId: "ACC-9001",
      transactionType: "TRANSFER",
      paymentChannel: "WIRE",
      amount: 15000,
      currency: "USD",
      merchantName: null,
      merchantCategory: "WIRE_TRANSFER",
      merchantCountry: "MEX",
      sourceIp: "10.10.20.30",
      deviceId: "demo-desktop-device-003",
    },
  },
  {
    label: "Create Missing Device/IP Transaction",
    payload: {
      externalCustomerId: "CUST-1002",
      externalAccountId: "ACC-9003",
      transactionType: "PURCHASE",
      paymentChannel: "CARD",
      amount: 250,
      currency: "USD",
      merchantName: "Demo Retail Store",
      merchantCategory: "RETAIL",
      merchantCountry: "USA",
      sourceIp: null,
      deviceId: null,
    },
  },
];

const defaultForm: CreateTransactionRequest = {
  idempotencyKey: `REQ-${Date.now()}`,
  externalCustomerId: "CUST-1001",
  externalAccountId: "ACC-9001",
  transactionType: "TRANSFER",
  paymentChannel: "WIRE",
  amount: 15000,
  currency: "USD",
  merchantName: "",
  merchantCategory: "WIRE_TRANSFER",
  merchantCountry: "MEX",
  sourceIp: "10.10.20.30",
  deviceId: "desktop-device-004",
  transactionTime: new Date().toISOString(),
};

export function CreateTransactionForm() {
  const [form, setForm] = useState<CreateTransactionRequest>(defaultForm);
  const [activeScenario, setActiveScenario] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: createTransaction,
    onSettled: () => setActiveScenario(null),
  });

  function updateField<K extends keyof CreateTransactionRequest>(
    key: K,
    value: CreateTransactionRequest[K]
  ) {
    setForm((current) => ({
      ...current,
      [key]: value,
    }));
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setActiveScenario(null);

    mutation.mutate({
      ...form,
      amount: Number(form.amount),
      merchantName: form.merchantName || null,
      merchantCategory: form.merchantCategory || null,
      merchantCountry: form.merchantCountry || null,
      sourceIp: form.sourceIp || null,
      deviceId: form.deviceId || null,
    });
  }

  function createDemoTransaction(scenario: DemoScenario) {
    const submittedAt = new Date();
    const request: CreateTransactionRequest = {
      ...scenario.payload,
      idempotencyKey: `DEMO-${submittedAt.getTime()}`,
      transactionTime: submittedAt.toISOString(),
    };

    setForm(request);
    setActiveScenario(scenario.label);
    mutation.mutate(request);
  }

  return (
    <Card title="New Transaction">
      <section className="demo-scenarios">
        <div>
          <h3>Create Demo Transaction</h3>
          <p>
            Submit a preset scenario and watch it move through the event
            pipeline.
          </p>
        </div>

        <div className="demo-scenario-grid">
          {demoScenarios.map((scenario) => (
            <Button
              key={scenario.label}
              type="button"
              variant="secondary"
              disabled={mutation.isPending}
              onClick={() => createDemoTransaction(scenario)}
            >
              {activeScenario === scenario.label && mutation.isPending
                ? "Creating..."
                : scenario.label}
            </Button>
          ))}
        </div>
      </section>

      <div className="form-divider">
        <span>Or enter transaction details manually</span>
      </div>

      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Idempotency Key
          <input
            value={form.idempotencyKey}
            onChange={(e) => updateField("idempotencyKey", e.target.value)}
          />
        </label>

        <label>
          Customer
          <input
            value={form.externalCustomerId}
            onChange={(e) => updateField("externalCustomerId", e.target.value)}
          />
        </label>

        <label>
          Account
          <input
            value={form.externalAccountId}
            onChange={(e) => updateField("externalAccountId", e.target.value)}
          />
        </label>

        <label>
          Transaction Type
          <select
            value={form.transactionType}
            onChange={(e) => updateField("transactionType", e.target.value)}
          >
            <option value="PURCHASE">PURCHASE</option>
            <option value="TRANSFER">TRANSFER</option>
            <option value="WITHDRAWAL">WITHDRAWAL</option>
            <option value="DEPOSIT">DEPOSIT</option>
            <option value="PAYMENT">PAYMENT</option>
          </select>
        </label>

        <label>
          Payment Channel
          <select
            value={form.paymentChannel}
            onChange={(e) => updateField("paymentChannel", e.target.value)}
          >
            <option value="CARD">CARD</option>
            <option value="ACH">ACH</option>
            <option value="WIRE">WIRE</option>
            <option value="ATM">ATM</option>
            <option value="ONLINE_BANKING">ONLINE_BANKING</option>
            <option value="MOBILE_APP">MOBILE_APP</option>
          </select>
        </label>

        <label>
          Amount
          <input
            type="number"
            value={form.amount}
            onChange={(e) => updateField("amount", Number(e.target.value))}
          />
        </label>

        <label>
          Currency
          <input
            value={form.currency}
            onChange={(e) => updateField("currency", e.target.value)}
          />
        </label>

        <label>
          Merchant Category
          <input
            value={form.merchantCategory ?? ""}
            onChange={(e) => updateField("merchantCategory", e.target.value)}
          />
        </label>

        <label>
          Merchant Country
          <input
            value={form.merchantCountry ?? ""}
            onChange={(e) => updateField("merchantCountry", e.target.value)}
          />
        </label>

        <label>
          Source IP
          <input
            value={form.sourceIp ?? ""}
            onChange={(e) => updateField("sourceIp", e.target.value)}
          />
        </label>

        <label>
          Device ID
          <input
            value={form.deviceId ?? ""}
            onChange={(e) => updateField("deviceId", e.target.value)}
          />
        </label>

        <div className="form-actions">
          <Button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? "Submitting..." : "Submit Transaction"}
          </Button>
        </div>
      </form>

      {mutation.isSuccess && (
        <div className="success-box">
          <strong>Transaction submitted.</strong>
          <pre>{JSON.stringify(mutation.data, null, 2)}</pre>
        </div>
      )}

      {mutation.isError && (
        <div className="error-box">
          {getTransactionErrorMessage(mutation.error)}
        </div>
      )}
    </Card>
  );
}
