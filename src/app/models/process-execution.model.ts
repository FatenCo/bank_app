export enum ProcessExecStatus {
  RUNNING = 'RUNNING',
  SUCCESS = 'SUCCESS',
  FAILED  = 'FAILED'
}

export interface ProcessExecution {
  id: string;
  status: ProcessExecStatus;
  message: string;
  startTime: string;
  endTime: string | null;
}