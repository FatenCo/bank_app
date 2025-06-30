import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StatementImportService } from '../../services/statement-import.service';
import { StatementEntry } from '../../models/statement-entry.model';

@Component({
  selector: 'app-statement-detail',
  templateUrl: './statement-detail.component.html',
  styleUrls: ['./statement-detail.component.css']
})
export class StatementDetailComponent implements OnInit {
  entry?: StatementEntry;

  constructor(
    private svc: StatementImportService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.svc.get(id).subscribe(e => this.entry = e);
  }

  back(): void {
    this.router.navigate(['/stmts']);
  }
}
