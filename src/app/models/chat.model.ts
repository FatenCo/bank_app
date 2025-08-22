// chat.model.ts
export interface BankingContextData {
  currentPage?: string;
  selectedAccountId?: string;
  accountNumber?: string;
  accountBalance?: number;
  reconciliationStatus?: string;
  lastImportJobId?: string;
}

export interface ChatRequest {
  message: string;
  userId: string;
  sessionId: string;
  contextData?: BankingContextData;
}

export interface SuggestedAction {
  type: string;
  label: string;
  action: string;
  data?: any;
}

export interface ChatResponse {
  response: string;
  success: boolean;
  sessionId?: string;
  suggestedActions?: SuggestedAction[];
  contextData?: { [key: string]: any };
}

export interface ChatMessage {
  id: string;
  content: string;
  isUser: boolean;
  timestamp: Date;
  suggestedActions?: SuggestedAction[];
  isLoading?: boolean;
  isError?: boolean;
}

export interface ChatbotHealth {
  status: string;
  service: string;
  timestamp: number;
}

export interface ChatbotStatus {
  chatbotService: string;
  version: string;
  uptime: number;
}