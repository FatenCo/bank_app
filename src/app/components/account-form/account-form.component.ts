// src/app/components/account-form/account-form.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountEntryService } from '../../services/account-entry.service';
import { AccountEntry } from '../../models/account-entry.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-account-form',
  templateUrl: './account-form.component.html',
  styleUrls: ['./account-form.component.css']
})
export class AccountFormComponent implements OnInit {
  form: FormGroup;
  id!: string;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private service: AccountEntryService
  ) {
    this.form = this.fb.group({
      dateOperation: ['', Validators.required],
      transactionId: ['', Validators.required],
      amount: [0, Validators.required],
      entity: [''],
      remarks: [''],
      accountNumber: ['', Validators.required],
      total: [null]
    });
  }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')!;
    this.service.get(this.id)
      .subscribe(entry => this.form.patchValue(entry));
  }

  save(): void {
    if (this.form.invalid) return;
    const updated: AccountEntry = { ...this.form.value, id: this.id };
    this.service.update(this.id, updated)
      .subscribe(() => this.router.navigate(['/entries']));
  }

  cancel(): void {
    this.router.navigate(['/entries']);
  }
}
