export interface LogEntry {
    line: number;
    level: 'INFO'|'ERROR'|'ALERT';
    message: string;
  }