import axios from "axios";

type BackendErrorData = {
  message?: string;
  error?: string;
  validationErrors?: Record<string, string>;
};

export function getApiErrorMessage(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError<BackendErrorData>(error)) {
    const backendData = error.response?.data;

    if (backendData?.validationErrors) {
      return Object.values(backendData.validationErrors).join("\n");
    }

    if (backendData?.message) {
      return backendData.message;
    }

    if (backendData?.error) {
      return backendData.error;
    }

    if (error.message) {
      return error.message;
    }
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallbackMessage;
}
