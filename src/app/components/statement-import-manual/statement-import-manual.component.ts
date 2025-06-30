import { Component } from '@angular/core';
import { StatementImportService } from '../../services/statement-import.service';
import { StatementEntry } from '../../models/statement-entry.model';
import { ImportJob } from '../../models/import-job.model';

@Component({
  selector: 'app-statement-import-manual',
  templateUrl: './statement-import-manual.component.html',
  styleUrls: ['./statement-import-manual.component.css']
})
export class StatementImportManualComponent {
  entries: StatementEntry[] = [];
  job?: ImportJob;

  constructor(private service: StatementImportService) {}

  addEntry(): void {
    this.entries.push({
      contract: '',
      category: '',
      consolKey: '',
      currency: '',
      customerNo: '',
      department: '',
      accountNumber: '',
      amtFcy: 0,
      amtLcy: 0,
      residence: '',
      lclBalConv: 0,
      acctDate: '',         // format YYYY-MM
      locContractType: '',
      deptLevel: 0
    });
  }

  removeEntry(index: number): void {
    this.entries.splice(index, 1);
  }

  submit(): void {
    if (!this.entries.length) return;
    this.service.importManual(this.entries)
      .subscribe(job => this.job = job);
  }
}
