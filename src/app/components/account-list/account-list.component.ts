import { Component, OnInit } from '@angular/core';
import { AccountEntryService } from '../../services/account-entry.service';
import { AccountEntry } from '../../models/account-entry.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.css']
})
export class AccountListComponent implements OnInit {
  entries: AccountEntry[] = [];
  filtered: AccountEntry[] = [];
  searchTerm = '';

  constructor(
    private service: AccountEntryService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.service.list().subscribe(list => {
      this.entries = list;
      this.applyFilter();
    });
  }

  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      this.filtered = [...this.entries];
    } else {
      this.filtered = this.entries.filter(e =>
        e.accountNumber.toLowerCase().includes(term) ||
        e.transactionId.toLowerCase().includes(term) ||
        e.entity.toLowerCase().includes(term)
      );
    }
  }

  edit(id?: string): void {
    if (id) this.router.navigate(['/entries/edit', id]);
  }

  delete(id?: string): void {
    if (!id || !confirm('Confirmer la suppression ?')) return;
    this.service.delete(id).subscribe(() => this.load());
  }
}
