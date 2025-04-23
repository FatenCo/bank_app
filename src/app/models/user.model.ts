export interface User {
    id?: number;
    username: string;
    role: string;
    lastPasswordChange?: string;
    failedAttempts?: number;
    locked?: boolean;
    password?: string;
  }