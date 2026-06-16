import axios from "axios";
import { apiConfig } from "../../../config/apiConfig";
import type {
  CreateTransactionRequest,
  CreateTransactionResponse,
  LatestTransaction,
} from "../types";

const transactionClient = axios.create({
  baseURL: apiConfig.transactionApiBaseUrl,
});

export function getTransactionErrorMessage(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return "Failed to submit transaction.";
  }

  const backendMessage = error.response?.data?.message;
  if (typeof backendMessage === "string" && backendMessage.trim()) {
    return backendMessage;
  }

  if (!error.response) {
    return `Cannot reach the transaction service at ${apiConfig.transactionApiBaseUrl}.`;
  }

  return `Transaction service returned ${error.response.status}.`;
}

export async function createTransaction(
  request: CreateTransactionRequest
): Promise<CreateTransactionResponse> {
  const response = await transactionClient.post<CreateTransactionResponse>(
    "/api/transactions",
    request
  );

  return response.data;
}

export async function getLatestTransaction(): Promise<LatestTransaction | null> {
  try {
    const response = await transactionClient.get<LatestTransaction>(
      "/api/transactions/latest"
    );
    return response.data || null;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw error;
  }
}
