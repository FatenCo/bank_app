import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { StatementImportService } from '../../services/statement-import.service';
import { StatementEntry } from '../../models/statement-entry.model';
import { ImportJob } from '../../models/import-job.model';

@Component({
  selector: 'app-statement-form',
  templateUrl: './statement-form.component.html',
  styleUrls: ['./statement-form.component.css']
})
export class StatementFormComponent implements OnInit {
  form!: FormGroup;
  id?: string;
  job?: ImportJob;

  constructor(
    private fb: FormBuilder,
    private svc: StatementImportService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id') ?? undefined;
    this.buildForm();
    if (this.id && this.id !== 'new') {
      this.svc.get(this.id).subscribe(e => this.form.patchValue(e));
    }
  }

  private buildForm() {
    this.form = this.fb.group({
      contract: ['', Validators.required],
      category: ['', Validators.required],
      consolKey: ['', Validators.required],
      currency: ['', Validators.required],
      customerNo: ['', Validators.required],
      department: ['', Validators.required],
      accountNumber: ['', Validators.required],
      amtFcy: [0],
      amtLcy: [0],
      lclBalConv: [0],
      acctDate: [''],
      locContractType: [''],
      deptLevel: [0],
      residence: ['']
    });
  }

  save(): void {
    if (this.form.invalid) return;
    const entry: StatementEntry = this.form.value;
    if (this.id && this.id !== 'new') {
      this.svc.update(this.id, entry).subscribe(() => this.router.navigate(['/stmts']));
    } else {
      // En mode création, on utilise importManual pour créer un job
      this.svc.importManual([entry]).subscribe(j => {
        this.job = j;
        this.router.navigate(['/stmts']);
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/stmts']);
  }
}
