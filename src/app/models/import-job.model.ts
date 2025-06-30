export interface ImportJob {
  id: string;
  source: string;
  startedAt: string;
  completedAt?: string;
  totalRecords?: number;
  processedRecords?: number;
  status: string;
  errorMessage?: string;
}