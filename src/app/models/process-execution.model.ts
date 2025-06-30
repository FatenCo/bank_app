// src/app/models/process-execution.model.ts
export enum ProcessExecStatus {
  RUNNING = 'RUNNING',
  SUCCESS = 'SUCCESS',
  FAILED  = 'FAILED'
}

export interface ProcessExecution {
  id: string;
  status: string;
  message: string;
  startTime: string;
  endTime: string;
  definition: {
    id: string;
    name: string;
}
}
