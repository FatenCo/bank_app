import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  transactionVolume: {[key: string]: number} = {};
  reconciliationStatusCount: {[key: string]: number} = {};
  reconciliationPerformance: {[key: string]: number} = {};
  pendingTransactions: string[] = [];
  unmatchedReconciliations: any[] = [];
  reconciliationStatusSummary: string = '';
  isLoading: boolean = false;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;

    this.dashboardService.getTransactionVolume().subscribe({
      next: (data) => {
        this.transactionVolume = data || {};
        console.log('Transaction volume loaded:', this.transactionVolume);
      },
      error: (error) => {
        console.error('Error loading transaction volume:', error);
        this.transactionVolume = {};
      }
    });

    this.dashboardService.getReconciliationStatusCount().subscribe({
      next: (data) => {
        this.reconciliationStatusCount = data || {};
        console.log('Reconciliation status loaded:', this.reconciliationStatusCount);
      },
      error: (error) => {
        console.error('Error loading reconciliation status:', error);
        this.reconciliationStatusCount = {};
      }
    });

    this.dashboardService.getReconciliationPerformance().subscribe({
      next: (data) => {
        this.reconciliationPerformance = data || {};
        console.log('Reconciliation performance loaded:', this.reconciliationPerformance);
      },
      error: (error) => {
        console.error('Error loading reconciliation performance:', error);
        this.reconciliationPerformance = {};
      }
    });

    this.dashboardService.getPendingTransactions().subscribe({
      next: (data) => {
        this.pendingTransactions = data || [];
        console.log('Pending transactions loaded:', this.pendingTransactions);
      },
      error: (error) => {
        console.error('Error loading pending transactions:', error);
        this.pendingTransactions = [];
      }
    });

    this.dashboardService.getUnmatchedReconciliations().subscribe({
      next: (data) => {
        this.unmatchedReconciliations = data || [];
        console.log('Unmatched reconciliations loaded:', this.unmatchedReconciliations);
      },
      error: (error) => {
        console.error('Error loading unmatched reconciliations:', error);
        this.unmatchedReconciliations = [];
      }
    });

    this.dashboardService.getReconciliationStatusSummary().subscribe({
      next: (data) => {
        this.reconciliationStatusSummary = data || 'Aucune donnée disponible';
        console.log('Reconciliation status summary loaded:', this.reconciliationStatusSummary);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading reconciliation status summary:', error);
        this.reconciliationStatusSummary = 'Erreur lors du chargement';
        this.isLoading = false;
      }
    });
  }

  // Méthodes utilitaires pour l'affichage
  getObjectKeys(obj: any): string[] {
    return Object.keys(obj);
  }

  formatStatusKey(key: string): string {
    if (key === 'true') return 'Lettré';
    if (key === 'false') return 'Non lettré';
    return key;
  }

  getObjectEntries(obj: any): Array<{key: string, value: any}> {
    return Object.entries(obj).map(([key, value]) => ({key, value}));
  }
}