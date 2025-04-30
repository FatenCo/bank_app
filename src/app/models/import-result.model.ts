import { LogEntry } from './log-entry.model';
export interface ImportResult {
  total: number;
  successCount: number;
  failureCount: number;
  failureRate: number;
  alert: boolean;
  logs: LogEntry[];
}