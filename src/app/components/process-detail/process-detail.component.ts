import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { ProcessService } from '../../services/process.service';
import { ProcessDefinition } from '../../models/process-definition.model';
import { ProcessExecution } from '../../models/process-execution.model';

@Component({
  selector: 'app-process-detail',
  templateUrl: './process-detail.component.html',
  styleUrls: ['./process-detail.component.css']
})
export class ProcessDetailComponent implements OnInit {
  pd?: ProcessDefinition;
  executions: ProcessExecution[] = [];
  definitionId!: string;
  isLoading = true;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private svc: ProcessService,
    private location: Location
  ) {}

  ngOnInit() {
    this.definitionId = this.route.snapshot.paramMap.get('id')!;
    
    // Charger le détail du processus
    this.svc.get(this.definitionId).subscribe({
      next: pd => this.pd = pd,
      error: err => {
        console.error('Erreur chargement processus', err);
        this.errorMessage = 'Erreur lors du chargement du processus';
      }
    });

    // Charger l'historique des exécutions
    this.loadExecutions();
  }

  loadExecutions() {
    this.isLoading = true;
    this.errorMessage = null;
    
    this.svc.listExecutions(this.definitionId).subscribe({
      next: executions => {
        this.executions = executions;
        this.isLoading = false;
      },
      error: err => {
        console.error('Erreur chargement executions', err);
        this.errorMessage = 'Erreur lors du chargement de l\'historique';
        this.isLoading = false;
      }
    });
  }

  calculateDuration(start: string, end: string | null): string {
    if (!start) return '-';
    
    const startDate = new Date(start);
    const endDate = end ? new Date(end) : new Date();
    
    const diffMs = endDate.getTime() - startDate.getTime();
    const diffSec = Math.floor(diffMs / 1000);
    const min = Math.floor(diffSec / 60);
    const sec = diffSec % 60;
    
    return end ? `${min} min ${sec} sec` : `En cours (${min} min ${sec} sec)`;
  }

  back(): void {
    this.location.back();
  }
}