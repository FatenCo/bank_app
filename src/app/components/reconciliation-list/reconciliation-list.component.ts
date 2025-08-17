import { Component, OnInit } from '@angular/core';
import { ReconciliationService } from '../../services/reconciliation.service';
import { Reconciliation } from '../../models/reconciliation.model';

@Component({
  selector: 'app-reconciliation-list',
  templateUrl: './reconciliation-list.component.html',
  styleUrls: ['./reconciliation-list.component.css']
})
export class ReconciliationListComponent implements OnInit {
  matchedReconciliations: Reconciliation[] = [];
  unmatchedReconciliations: Reconciliation[] = [];
  isLoading = false;
  error: string | null = null;

  constructor(private reconciliationService: ReconciliationService) { }

  ngOnInit(): void {
    this.loadReconciliations();
  }

  loadReconciliations(): void {
    this.isLoading = true;
    this.error = null;
    
    this.reconciliationService.getMatchedReconciliations().subscribe({
      next: (data) => {
        this.matchedReconciliations = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des réconciliations appariées';
        this.isLoading = false;
        console.error(err);
      }
    });

    this.reconciliationService.getUnmatchedReconciliations().subscribe({
      next: (data) => {
        this.unmatchedReconciliations = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des réconciliations non appariées';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  unmatchReconciliation(id: string): void {
    this.reconciliationService.unmatchReconciliation(id).subscribe({
      next: () => {
        this.loadReconciliations();
        alert('Réconciliation défaite avec succès');
      },
      error: (err) => {
        console.error(err);
        alert('Erreur lors de la déconnexion de la réconciliation');
      }
    });
  }
}