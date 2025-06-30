import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProcessDefinition } from '../../models/process-definition.model';
import { ProcessService }    from '../../services/process.service';

@Component({
  selector: 'app-process-list',
  templateUrl: './process-list.component.html',
  styleUrls: ['./process-list.component.css']
})
export class ProcessListComponent implements OnInit {
  processes: ProcessDefinition[] = [];
  loading = true;
  searchTerm = '';

  constructor(
    private svc: ProcessService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.svc.list().subscribe(list => {
      this.processes = list;
      this.loading = false;
    });
  }

  create(): void {
    this.router.navigate(['/processes/new']);
  }

  view(id: string): void {
    this.router.navigate(['/processes/view', id]);
  }

  edit(id: string): void {
    this.router.navigate(['/processes/edit', id]);
  }

  delete(id: string): void {
    this.svc.delete(id).subscribe(() => this.loadAll());
  }

  start(p: ProcessDefinition): void {
    if (!p.id) return;
    this.svc.runNow(p.id).subscribe(() => this.loadAll());
  }

  stop(p: ProcessDefinition): void {
    if (!p.id) return;
    this.svc.stop(p.id).subscribe(() => this.loadAll());
  }
}
