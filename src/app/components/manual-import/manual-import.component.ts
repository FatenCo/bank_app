import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { AccountImportService } from '../../services/account-import.service';
import { ImportJob } from '../../models/import-job.model';

@Component({
  selector: 'app-manual-import',
  templateUrl: './manual-import.component.html',
  styleUrls: ['./manual-import.component.css']
})
export class ManualImportComponent {
  manualForm: FormGroup;
  result?: ImportJob;

  constructor(private fb: FormBuilder, private service: AccountImportService) {
    this.manualForm = this.fb.group({
      entries: this.fb.array([])
    });
    this.addEntry();
  }

  get entries(): FormArray {
    return this.manualForm.get('entries') as FormArray;
  }

  addEntry(): void {
    this.entries.push(
      this.fb.group({
        dateOperation: ['', Validators.required],
        transactionId: ['', Validators.required],
        amount: [0, Validators.required],
        entity: [''],
        remarks: [''],
        accountNumber: ['', Validators.required],
        total: [null]
      })
    );
  }

  removeEntry(index: number): void {
    if (this.entries.length > 1) {
      this.entries.removeAt(index);
    }
  }

  submit(): void {
    if (this.manualForm.invalid) return;
    const data = this.manualForm.value.entries;
    this.service.importManual(data).subscribe(job => {
      this.result = job;
    });
  }
}