export interface AccountEntry {
  id?: string;                // ajouté
  dateOperation: string;
  transactionId: string;
  amount: number;
  entity: string;
  remarks: string;
  accountNumber: string;
  total?: number;
}
