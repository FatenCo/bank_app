import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Reconciliation } from '../models/reconciliation.model';

export interface DifferenceCheckResult {
  accountAmount: number;
  statementAmount: number;
  difference: number;
  tolerance: number;
  isExactMatch: boolean;
  isWithinTolerance: boolean;
  requiresConfirmation: boolean;
}

export interface ReconciliationRequest {
  accountId: string;
  statementId: string;
  forceReconciliation?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ReconciliationService {
  private API = 'http://localhost:8080/api/reconciliations';

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private handleError(message: string, error: any): Observable<never> {
    console.error(error);
    this.snackBar.open(message, 'Fermer', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
    return throwError(error);
  }

  getMatchedReconciliations(): Observable<Reconciliation[]> {
    return this.http.get<Reconciliation[]>(`${this.API}/matched`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors du chargement des réconciliations appariées', error))
    );
  }

  getUnmatchedReconciliations(): Observable<Reconciliation[]> {
    return this.http.get<Reconciliation[]>(`${this.API}/unmatched`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors du chargement des réconciliations non appariées', error))
    );
  }

  getUnmatchedAccounts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/unmatched/accounts`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors du chargement des comptes non appariés', error))
    );
  }

  getUnmatchedStatements(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/unmatched/statements`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(error => this.handleError('Erreur lors du chargement des relevés non appariés', error))
    );
  }

  checkReconciliationDifference(accountId: string, statementId: string): Observable<DifferenceCheckResult> {
    return this.http.post<DifferenceCheckResult>(
      `${this.API}/check-difference`,
      { accountId, statementId },
      { headers: this.getHeaders() }
    ).pipe(
      catchError(error => this.handleError('Erreur lors de la vérification de la différence', error))
    );
  }

  manualReconcileWithTolerance(
    accountId: string, 
    statementId: string, 
    forceReconciliation: boolean = false
  ): Observable<Reconciliation> {
    const params: ReconciliationRequest = {
      accountId,
      statementId,
      forceReconciliation
    };

    return this.http.post<Reconciliation>(
      `${this.API}/manual-tolerance`,
      params,
      { headers: this.getHeaders() }
    ).pipe(
      catchError(error => {
        if (error.status === 400) {
          return throwError({
            requiresConfirmation: true,
            message: error.error?.message || 'Différence trop importante. Confirmez-vous la réconciliation ?'
          });
        }
        return this.handleError('Erreur lors de la réconciliation manuelle', error);
      })
    );
  }

  unmatchReconciliation(id: string): Observable<void> {
    return this.http.post<void>(
      `${this.API}/unmatch/${id}`, 
      {},
      { headers: this.getHeaders() }
    ).pipe(
      catchError(error => this.handleError('Erreur lors de la déconnexion de la réconciliation', error))
    );
  }
}