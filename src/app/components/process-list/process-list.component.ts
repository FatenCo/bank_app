import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProcessService } from '../../services/process.service';
import { ProcessDefinition, ProcessType } from '../../models/process-definition.model';

@Component({
  selector: 'app-process-list',
  templateUrl: './process-list.component.html',
  styleUrls: ['./process-list.component.css']
})
export class ProcessListComponent implements OnInit {
  processes: ProcessDefinition[] = [];
  loading = true;
  errorMessage: string | null = null;
  searchQuery = '';

  ProcessType = ProcessType;

  constructor(
    private processService: ProcessService, 
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.errorMessage = null;
    
    this.processService.list().subscribe({
      next: (list) => {
        this.processes = list;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement des processus';
        this.loading = false;
        console.error('Erreur de chargement', err);
      }
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
    if (confirm('Êtes-vous sûr de vouloir supprimer ce processus ?')) {
      this.processService.delete(id).subscribe({
        next: () => this.loadAll(),
        error: (err) => {
          this.errorMessage = 'Échec de la suppression';
          console.error('Erreur de suppression', err);
        }
      });
    }
  }

  start(p: ProcessDefinition): void {
    if (!p.id) return;
    
    this.processService.runNow(p.id).subscribe({
      next: () => {
        if (p.type === ProcessType.RECONCILIATION) {
          alert('Réconciliation lancée avec succès');
          this.router.navigate(['/reconciliations']);
        } else {
          alert('Processus exécuté avec succès');
        }
        this.loadAll();
      },
      error: (err) => {
        alert(`Erreur lors du lancement: ${err.error?.message || err.message}`);
      }
    });
  }

  stop(p: ProcessDefinition): void {
    if (!p.id) return;
    
    this.processService.stop(p.id).subscribe({
      next: () => {
        alert('Processus arrêté avec succès');
        this.loadAll();
      },
      error: (err) => {
        alert(`Erreur lors de l'arrêt: ${err.error?.message || err.message}`);
      }
    });
  }

  getStatusClass(enabled: boolean): string {
    return enabled ? 'status-active' : 'status-inactive';
  }

  getStatusText(enabled: boolean): string {
    return enabled ? 'Activé' : 'Désactivé';
  }

  getModeText(mode: string): string {
    return mode === 'SCHEDULED' ? 'Planifié' : 'Manuel';
  }

  getTypeText(type: ProcessType): string {
    switch(type) {
      case ProcessType.ACCOUNT_TREATMENT: return 'Traitement Comptes';
      case ProcessType.STMT_TREATMENT: return 'Traitement Relevés';
      case ProcessType.RECONCILIATION: return 'Réconciliation';
      default: return type;
    }
  }

  get filteredProcesses() {
    if (!this.searchQuery) return this.processes;
    return this.processes.filter(p => 
      p.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      p.description?.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      this.getTypeText(p.type).toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }
}