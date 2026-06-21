import axios from "axios";

import { apiConfig } from "../../../config/apiConfig";
import type { AiExplanation } from "../types";

const aiExplanationClient = axios.create({
  baseURL: apiConfig.aiExplanationApiBaseUrl,
});

export function getAiExplanationErrorMessage(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return "Failed to load AI explanation.";
  }

  const backendMessage = error.response?.data?.message;
  if (typeof backendMessage === "string" && backendMessage.trim()) {
    return backendMessage;
  }

  if (!error.response) {
    return `Cannot reach the AI explanation service at ${apiConfig.aiExplanationApiBaseUrl}.`;
  }

  return `AI explanation service returned ${error.response.status}.`;
}

export async function getAiExplanationByTransactionId(
  transactionId: string
): Promise<AiExplanation | null> {
  try {
    const response = await aiExplanationClient.get<AiExplanation>(
      `/api/v1/ai-explanations/transaction/${transactionId}`
    );

    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }

    throw error;
  }
}
