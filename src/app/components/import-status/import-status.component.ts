import { Component } from '@angular/core';
import { AccountImportService } from '../../services/account-import.service';
import { ImportJob } from '../../models/import-job.model';

type FilterType = 'id' | 'file' | 'date';

@Component({
  selector: 'app-import-status',
  templateUrl: './import-status.component.html',
  styleUrls: ['./import-status.component.css']
})
export class ImportStatusComponent {
  filterType: FilterType = 'id';
  filterValue = '';
  results: ImportJob[] = [];  // on ne garde plus `status`

  constructor(private service: AccountImportService) {}

  search(): void {
    this.results = [];
    if (!this.filterValue) return;

    switch (this.filterType) {
      case 'id':
        this.service.getStatus(this.filterValue)
          .subscribe(job => this.results = [job], _ => this.results = []);
        break;
      case 'file':
        this.service.searchByFileName(this.filterValue)
          .subscribe(list => this.results = list);
        break;
      case 'date':
        this.service.searchByDate(this.filterValue)
          .subscribe(list => this.results = list);
        break;
    }
  }
}
