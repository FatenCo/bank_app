import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { StatementImportService } from '../../services/statement-import.service';
import { StatementEntry } from '../../models/statement-entry.model';

@Component({
  selector: 'app-statement-list',
  templateUrl: './statement-list.component.html',
  styleUrls: ['./statement-list.component.css']
})
export class StatementListComponent implements OnInit {
  entries: StatementEntry[] = [];
  filtered: StatementEntry[] = [];
  searchTerm = '';

  constructor(
    private svc: StatementImportService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.svc.list().subscribe(list => {
      this.entries = list;
      this.filtered = list;
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.toLowerCase();
    this.filtered = this.entries.filter(e =>
      e.contract.toLowerCase().includes(term) ||
      e.category.toLowerCase().includes(term) ||
      e.accountNumber.toLowerCase().includes(term)
    );
  }

  view(id?: string): void {
    if (id) this.router.navigate(['stmts/view', id]);
  }

  edit(id?: string): void {
    if (id) this.router.navigate(['stmts/edit', id]);
  }

  delete(id?: string): void {
    if (!id || !confirm('Confirmer la suppression ?')) return;
    this.svc.delete(id).subscribe(() => this.load());
  }
}
