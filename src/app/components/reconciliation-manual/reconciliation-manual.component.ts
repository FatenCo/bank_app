import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ReconciliationService } from '../../services/reconciliation.service';
import { ConfirmationDialogComponent } from 'src/app/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-reconciliation-manual',
  templateUrl: './reconciliation-manual.component.html',
  styleUrls: ['./reconciliation-manual.component.css'] // Doit pointer vers le bon fichier
})
export class ReconciliationManualComponent implements OnInit {
  accounts: any[] = [];
  statements: any[] = [];
  selectedStatementMap: { [key: string]: any } = {};
  
  // Propriétés de recherche
  globalSearch: string = '';
  accountSearch: string = '';
  statementSearch: string = '';

  // État de chargement
  isLoadingAccounts: boolean = false;
  isLoadingStatements: boolean = false;
  reconciliationInProgress: { [key: string]: boolean } = {};

  constructor(
    private svc: ReconciliationService,
    private dialog: MatDialog,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoadingAccounts = true;
    this.isLoadingStatements = true;
    
    this.svc.getUnmatchedAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts.filter(acc => acc.total !== 0);
        this.isLoadingAccounts = false;
      },
      error: (err) => {
        console.error('Erreur chargement comptes:', err);
        this.isLoadingAccounts = false;
        this.showErrorMessage('Erreur lors du chargement des comptes');
      }
    });

    this.svc.getUnmatchedStatements().subscribe({
      next: (statements) => {
        this.statements = statements;
        this.isLoadingStatements = false;
      },
      error: (err) => {
        console.error('Erreur chargement relevés:', err);
        this.isLoadingStatements = false;
        this.showErrorMessage('Erreur lors du chargement des relevés');
      }
    });
  }

  get filteredAccounts(): any[] {
    return this.accounts
      .filter(acc => 
        (!this.accountSearch || 
          acc.accountNumber.toLowerCase().includes(this.accountSearch.toLowerCase()) ||
          this.formatNumber(acc.total).includes(this.accountSearch)) &&
        (!this.globalSearch || 
          acc.accountNumber.toLowerCase().includes(this.globalSearch.toLowerCase()) ||
          this.formatNumber(acc.total).includes(this.globalSearch))
      )
      .sort((a, b) => a.accountNumber.localeCompare(b.accountNumber));
  }

  get filteredStatements(): any[] {
    return this.statements
      .filter(stmt => 
        (!this.statementSearch || 
          stmt.accountNumber.toLowerCase().includes(this.statementSearch.toLowerCase()) ||
          this.formatNumber(stmt.amtFcy).includes(this.statementSearch)) &&
        (!this.globalSearch || 
          stmt.accountNumber.toLowerCase().includes(this.globalSearch.toLowerCase()) ||
          this.formatNumber(stmt.amtFcy).includes(this.globalSearch))
      )
      .sort((a, b) => a.accountNumber.localeCompare(b.accountNumber));
  }

  manualReconcile(account: any): void {
    const statement = this.selectedStatementMap[account.id];
    if (!statement) return;
    
    this.reconciliationInProgress[account.id] = true;
    this.attemptReconciliation(account, statement, false);
  }

  private attemptReconciliation(account: any, statement: any, forceReconciliation: boolean): void {
    this.svc.manualReconcileWithTolerance(
      account.id, 
      statement.id, 
      forceReconciliation
    ).subscribe({
      next: (result) => {
        this.showSuccessMessage('Réconciliation effectuée avec succès');
        this.router.navigate(['/reconciliations']);
      },
      error: (error) => {
        this.reconciliationInProgress[account.id] = false;
        if (error.requiresConfirmation) {
          this.handleHighDifferenceConfirmation(account, statement, error.message);
        } else {
          this.showErrorMessage(error.message || 'Erreur lors de la réconciliation');
        }
      }
    });
  }

  private handleHighDifferenceConfirmation(account: any, statement: any, message: string): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      width: '500px',
      data: {
        title: 'Confirmation requise',
        message: `${message}\n\nÊtes-vous sûr de vouloir procéder à la réconciliation ?`,
        confirmText: 'Confirmer',
        cancelText: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.reconciliationInProgress[account.id] = true;
        this.attemptReconciliation(account, statement, true);
      }
    });
  }

  checkDifference(account: any): void {
    const statement = this.selectedStatementMap[account.id];
    if (!statement) return;

    this.svc.checkReconciliationDifference(account.id, statement.id).subscribe({
      next: (result: any) => {
        const diff = result.difference;
        let message = '';

        if (result.isExactMatch) {
          message = 'Les montants sont exactement égaux. Réconciliation recommandée.';
        } else if (result.isWithinTolerance) {
          message = `Différence dans la tolérance: ${diff.toFixed(2)} TND (≤ 100 TND). Réconciliation possible.`;
        } else {
          message = `Attention: Différence importante de ${diff.toFixed(2)} TND (> 100 TND). Confirmation requise.`;
        }

        this.dialog.open(ConfirmationDialogComponent, {
          width: '500px',
          data: {
            title: 'Vérification des différences',
            message: message,
            showConfirm: false,
            cancelText: 'Fermer'
          }
        });
      },
      error: (err) => {
        console.error('Erreur lors de la vérification:', err);
        this.showErrorMessage('Erreur lors de la vérification des différences');
      }
    });
  }

  getStatementsForAccount(accountNumber: string): any[] {
    return this.statements
      .filter(stmt => 
        stmt.accountNumber === accountNumber && 
        !this.isStatementUsed(stmt.id));
  }

  isStatementUsed(statementId: string): boolean {
    return Object.values(this.selectedStatementMap)
      .some(stmt => stmt && stmt.id === statementId);
  }

  parseCurrency(value: any): number {
    if (typeof value === 'number') return value;
    if (typeof value === 'string') {
      const cleaned = value.replace(/[^\d.-]/g, '');
      return parseFloat(cleaned) || 0;
    }
    return 0;
  }

  formatNumber(value: any): string {
    const num = this.parseCurrency(value);
    return num.toFixed(2);
  }

  formatCurrency(value: any): string {
    const num = this.parseCurrency(value);
    return num.toLocaleString('fr-TN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }) + ' TND';
  }

  getDifferenceInfo(account: any, statement: any): {message: string, cssClass: string} | null {
    if (!statement) return null;
    
    const accountAmount = this.parseCurrency(account.total);
    const statementAmount = this.parseCurrency(statement.amtFcy);
    const diff = Math.abs(accountAmount - statementAmount);
    
    if (diff === 0) {
      return {
        message: 'Montants égaux',
        cssClass: 'exact-match'
      };
    } else if (diff <= 100) {
      return {
        message: `Différence: ${diff.toFixed(2)} TND (Tolérance OK)`,
        cssClass: 'within-tolerance'
      };
    } else {
      return {
        message: `Différence: ${diff.toFixed(2)} TND (ATTENTION!)`,
        cssClass: 'outside-tolerance'
      };
    }
  }

  private showSuccessMessage(message: string): void {
    // À remplacer par un snackbar ou toast
    console.log(message);
  }

  private showErrorMessage(message: string): void {
    // À remplacer par un snackbar ou toast
    console.error(message);
  }
}