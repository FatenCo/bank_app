import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { DashboardService } from '../../services/dashboard.service';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

// Enregistrer tous les composants Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, AfterViewInit, OnDestroy {
  transactionVolume: {[key: string]: number} = {};
  reconciliationStatusCount: {[key: string]: number} = {};
  reconciliationPerformance: {[key: string]: number} = {};
  pendingTransactions: string[] = [];
  unmatchedReconciliations: any[] = [];
  reconciliationStatusSummary: string = '';
  isLoading: boolean = false;

  // Variables pour les graphiques
  private volumeChart: Chart | null = null;
  private statusChart: Chart | null = null;
  private performanceChart: Chart | null = null;

  // Configuration des couleurs
  private colors = {
    primary: '#1e3a8a',
    secondary: '#f97316',
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    background: '#f8fafc',
    text: '#2c3e50'
  };

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngAfterViewInit(): void {
    // Initialiser les graphiques après le rendu de la vue
    setTimeout(() => {
      this.initializeCharts();
    }, 100);
  }

  loadData(): void {
    this.isLoading = true;

    this.dashboardService.getTransactionVolume().subscribe({
      next: (data) => {
        this.transactionVolume = data || {};
        console.log('Transaction volume loaded:', this.transactionVolume);
        this.updateVolumeChart();
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
        this.updateStatusChart();
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
        this.updatePerformanceChart();
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

  private initializeCharts(): void {
    this.createVolumeChart();
    this.createStatusChart();
    this.createPerformanceChart();
  }

  private createVolumeChart(): void {
    const canvas = document.getElementById('volumeChart') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    this.volumeChart = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: [],
        datasets: [{
          label: 'Volume des transactions',
          data: [],
          backgroundColor: this.colors.primary,
          borderColor: this.colors.secondary,
          borderWidth: 2,
          borderRadius: 8,
          borderSkipped: false,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: '#e2e8f0'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  private createStatusChart(): void {
    const canvas = document.getElementById('statusPieChart') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    this.statusChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: [],
        datasets: [{
          data: [],
          backgroundColor: [this.colors.success, this.colors.warning],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: {
          legend: {
            position: 'bottom' as const,
            labels: {
              padding: 20,
              usePointStyle: true
            }
          }
        }
      }
    });
  }

  private createPerformanceChart(): void {
    const canvas = document.getElementById('performanceChart') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    this.performanceChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: [],
        datasets: [
          {
            label: 'Taux de réconciliation',
            data: [],
            borderColor: this.colors.primary,
            backgroundColor: this.colors.primary + '20',
            fill: true,
            tension: 0.4,
            pointBackgroundColor: this.colors.primary,
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          intersect: false,
          mode: 'index' as const
        },
        plugins: {
          legend: {
            position: 'top' as const,
            labels: {
              usePointStyle: true,
              padding: 20
            }
          }
        },
        scales: {
          y: {
            beginAtZero: false,
            min: 0,
            max: 100,
            grid: {
              color: '#e2e8f0'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        }
      }
    });
  }

  private updateVolumeChart(): void {
    if (!this.volumeChart) return;

    const labels = Object.keys(this.transactionVolume);
    const data = Object.values(this.transactionVolume);

    this.volumeChart.data.labels = labels;
    this.volumeChart.data.datasets[0].data = data;
    this.volumeChart.update();
  }

  private updateStatusChart(): void {
    if (!this.statusChart) return;

    const labels = Object.keys(this.reconciliationStatusCount).map(key => this.formatStatusKey(key));
    const data = Object.values(this.reconciliationStatusCount);

    this.statusChart.data.labels = labels;
    this.statusChart.data.datasets[0].data = data;
    this.statusChart.update();
  }

  private updatePerformanceChart(): void {
    if (!this.performanceChart) return;

    const labels = Object.keys(this.reconciliationPerformance);
    const data = Object.values(this.reconciliationPerformance);

    this.performanceChart.data.labels = labels;
    this.performanceChart.data.datasets[0].data = data;
    this.performanceChart.update();
  }

  // Méthodes utilitaires pour l'affichage (gardées identiques)
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

  // Nettoyage des graphiques à la destruction du composant
  ngOnDestroy(): void {
    if (this.volumeChart) {
      this.volumeChart.destroy();
    }
    if (this.statusChart) {
      this.statusChart.destroy();
    }
    if (this.performanceChart) {
      this.performanceChart.destroy();
    }
  }
}