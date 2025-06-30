export interface StatementEntry {
  id?: string;
  contract: string;
  category: string;
  consolKey: string;
  currency: string;
  customerNo: string;
  department: string;
  amtFcy?: number;
  amtLcy?: number;
  residence?: string;
  accountNumber: string;
  lclBalConv?: number;
  acctDate?: string;    // format 'YYYY-MM'
  locContractType?: string;
  deptLevel?: number;
}