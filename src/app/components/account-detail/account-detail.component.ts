import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountEntryService } from '../../services/account-entry.service';
import { AccountEntry } from '../../models/account-entry.model';

@Component({
  selector: 'app-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.css']
})
export class AccountDetailComponent implements OnInit {
  entry?: AccountEntry;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: AccountEntryService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.service.get(id).subscribe(e => this.entry = e);
  }

  back(): void {
    this.router.navigate(['/entries']);
  }
}
