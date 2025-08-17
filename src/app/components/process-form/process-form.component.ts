import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProcessService } from '../../services/process.service';
import { Router, ActivatedRoute } from '@angular/router';
import { ProcessType, ProcessMode } from '../../models/process-definition.model';

@Component({
  selector: 'app-process-form',
  templateUrl: './process-form.component.html',
  styleUrls: ['./process-form.component.css']
})
export class ProcessFormComponent implements OnInit {
  form!: FormGroup;
  types = Object.values(ProcessType);
  modes = Object.values(ProcessMode);
  editing = false;
  id?: string;

  constructor(
    private fb: FormBuilder,
    private svc: ProcessService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      type: [ProcessType.RECONCILIATION, Validators.required],
      mode: [ProcessMode.MANUAL, Validators.required],
      enabled: [true],
      scheduledAt: ['']
    });

    this.id = this.route.snapshot.paramMap.get('id') || undefined;
    if (this.id) {
      this.editing = true;
      this.svc.get(this.id).subscribe(pd => {
        const patch: any = {
          ...pd,
          scheduledAt: pd.cronExpression
            ? this.cronToDateTimeLocal(pd.cronExpression)
            : ''
        };
        this.form.patchValue(patch);
      });
    }

    this.form.get('mode')!.valueChanges.subscribe(m => {
      const sched = this.form.get('scheduledAt')!;
      if (m === ProcessMode.SCHEDULED) sched.setValidators(Validators.required);
      else sched.clearValidators();
      sched.updateValueAndValidity();
    });
  }

  // AJOUT: Méthodes pour les noms affichables
  getTypeName(type: string): string {
    switch(type) {
      case 'RECONCILIATION': return 'Réconciliation comptable';
      case 'ACCOUNT_TREATMENT': return 'Traitement des comptes';
      case 'STMT_TREATMENT': return 'Traitement des relevés';
      default: return type;
    }
  }

  getModeName(mode: string): string {
    switch(mode) {
      case 'MANUAL': return 'Manuel';
      case 'SCHEDULED': return 'Planifié';
      default: return mode;
    }
  }

  save() {
    if (this.form.invalid) return;
    const v = this.form.value;
    const pd = {
      name: v.name,
      description: v.description,
      type: v.type,
      mode: v.mode,
      enabled: v.enabled,
      cronExpression: v.mode === ProcessMode.SCHEDULED ? this.dateTimeLocalToCron(v.scheduledAt) : undefined,
    };

    const req = this.editing ? this.svc.update(this.id!, pd) : this.svc.create(pd);
    req.subscribe(() => this.router.navigate(['/processes']));
  }

  back() { this.router.navigate(['/processes']); }

  private dateTimeLocalToCron(dt: string): string {
    const [date, time] = dt.split('T');
    const [year, month, day] = date.split('-');
    const [hour, minute] = time.split(':');
    return `0 ${minute} ${hour} ${day} ${month} *`;
  }

  private cronToDateTimeLocal(cron: string): string {
    const parts = cron.split(' ');
    if (parts.length < 5) return '';
    const [, minute, hour, day, month] = parts;
    const now = new Date();
    const year = now.getFullYear().toString().padStart(4,'0');
    return `${year}-${month.padStart(2,'0')}-${day.padStart(2,'0')}T${hour.padStart(2,'0')}:${minute.padStart(2,'0')}`;
  }
}