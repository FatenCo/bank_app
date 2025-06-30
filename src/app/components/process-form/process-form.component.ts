import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProcessService }    from '../../services/process.service';
import {
  ProcessDefinition,
  ProcessType,
  ProcessMode
} from '../../models/process-definition.model';

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
      name:        ['', Validators.required],
      description: [''],
      type:        [ProcessType.ACCOUNT_TREATMENT, Validators.required],
      mode:        [ProcessMode.MANUAL, Validators.required],
      enabled:     [true],
      // remplace cronExpression/cronDescription
      scheduledAt: ['']  // ISO datetime-local string
    });

    this.id = this.route.snapshot.paramMap.get('id') || undefined;
    if (this.id) {
      this.editing = true;
      this.svc.get(this.id).subscribe(pd => {
        // si défini en mode SCHEDULED, on extrait scheduledAt du cronExpression
        const patch: any = {
          ...pd,
          scheduledAt: pd.cronExpression
            ? this.cronToDateTimeLocal(pd.cronExpression)
            : ''
        };
        this.form.patchValue(patch);
      });
    }

    // Validation de scheduledAt uniquement si scheduled
    this.form.get('mode')!.valueChanges.subscribe(m => {
      const sched = this.form.get('scheduledAt')!;
      if (m === ProcessMode.SCHEDULED) sched.setValidators(Validators.required);
      else                             sched.clearValidators();
      sched.updateValueAndValidity();
    });
  }

  save() {
    if (this.form.invalid) return;
    const v = this.form.value;
    const pd: ProcessDefinition = {
      name: v.name,
      description: v.description,
      type: v.type,
      mode: v.mode,
      enabled: v.enabled,
      // on retransforme scheduledAt en cronExpression (00 minute heure jour mois *)
      cronExpression: v.mode === ProcessMode.SCHEDULED && v.scheduledAt
        ? this.dateTimeLocalToCron(v.scheduledAt)
        : undefined,
      cronDescription: v.mode === ProcessMode.SCHEDULED
        ? `Planifié le ${v.scheduledAt.replace('T',' à ')}`
        : undefined
    };

    const req = this.editing
      ? this.svc.update(this.id!, pd)
      : this.svc.create(pd);

    req.subscribe(() => this.router.navigate(['/processes']));
  }

  back() { this.router.navigate(['/processes']); }

  private dateTimeLocalToCron(dt: string): string {
    // dt = "YYYY-MM-DDTHH:mm"
    const [date, time] = dt.split('T');
    const [year, month, day] = date.split('-');
    const [hour, minute] = time.split(':');
    // Spring Cron: sec min hour day month weekday
    return `0 ${minute} ${hour} ${day} ${month} *`;
  }

  private cronToDateTimeLocal(cron: string): string {
    // attend format "0 mm HH dd MM *"
    const parts = cron.split(' ');
    if (parts.length < 5) return '';
    const [ , minute, hour, day, month ] = parts;
    // construction "YYYY-MM-DDTHH:mm"
    const now = new Date();
    const year = now.getFullYear().toString().padStart(4,'0');
    return `${year}-${month.padStart(2,'0')}-${day.padStart(2,'0')}T${hour.padStart(2,'0')}:${minute.padStart(2,'0')}`;
  }
}
