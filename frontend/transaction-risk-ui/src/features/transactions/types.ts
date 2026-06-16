export type CreateTransactionRequest = {
  idempotencyKey: string;
  externalCustomerId: string;
  externalAccountId: string;
  transactionType: string;
  paymentChannel: string;
  amount: number;
  currency: string;
  merchantName?: string | null;
  merchantCategory?: string | null;
  merchantCountry?: string | null;
  sourceIp?: string | null;
  deviceId?: string | null;
  transactionTime: string;
};

export type CreateTransactionResponse = {
  transactionId: string;
  transactionReference: string;
  status: string;
  message: string;
};

export type LatestTransaction = {
  transactionId: string;
  transactionReference: string;
  status: string;
  createdAt: string;
};
