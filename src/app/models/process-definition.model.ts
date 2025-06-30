// src/app/models/process-definition.model.ts
export enum ProcessType {
  ACCOUNT_TREATMENT = 'ACCOUNT_TREATMENT',
  STMT_TREATMENT   = 'STMT_TREATMENT',
  RECONCILIATION  = 'RECONCILIATION'
}

export enum ProcessMode {
  MANUAL    = 'MANUAL',
  SCHEDULED = 'SCHEDULED'
}

export interface ProcessDefinition {
  id?: string;
  name: string;
  description?: string;
  type: ProcessType;
  mode: ProcessMode;
  enabled: boolean;
  cronExpression?: string;
  cronDescription?: string;
}
