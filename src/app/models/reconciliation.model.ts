export interface Reconciliation {
  id: string;
  accountEntry: {
    id: string;
    accountNumber: string;
    amount: number;
    total: number;
    dateOperation?: string;
    remarks?: string;
  };
  statementEntry: {
    id: string;
    accountNumber: string;
    amtFcy: number;
    acctDate?: string;
    remarks?: string;
  };
  matched: boolean;
  unmatched: boolean;
  reconciliationDate: string;
  matchingAmount: number;
  autoMatched?: boolean;
  manualMatched?: boolean;
}