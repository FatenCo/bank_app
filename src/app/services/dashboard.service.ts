import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  // Gestion d'erreur centralisée
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed:`, error);
      return of(result as T);
    };
  }

  // Obtenir le volume des transactions - correction du type de retour
  getTransactionVolume(): Observable<{[key: string]: number}> {
    return this.http.get<{[key: string]: number}>(`${this.apiUrl}/transaction-volume`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError<{[key: string]: number}>('getTransactionVolume', {}))
    );
  }

  // Obtenir le statut des réconciliations - correction du type de retour
  getReconciliationStatusCount(): Observable<{[key: string]: number}> {
    return this.http.get<{[key: string]: number}>(`${this.apiUrl}/reconciliation-status-count`, {
      headers: this.getHeaders()
    }).pipe(
      map(response => {
        // Conversion des clés boolean en string pour l'affichage
        const converted: {[key: string]: number} = {};
        Object.entries(response).forEach(([key, value]) => {
          converted[key] = value;
        });
        return converted;
      }),
      catchError(this.handleError<{[key: string]: number}>('getReconciliationStatusCount', {}))
    );
  }

  // Obtenir les performances des réconciliations - correction du type de retour
  getReconciliationPerformance(): Observable<{[key: string]: number}> {
    return this.http.get<{[key: string]: number}>(`${this.apiUrl}/reconciliation-performance`, {
      headers: this.getHeaders()
    }).pipe(
      map(response => {
        // Conversion des clés boolean en string pour l'affichage
        const converted: {[key: string]: number} = {};
        Object.entries(response).forEach(([key, value]) => {
          converted[key] = value;
        });
        return converted;
      }),
      catchError(this.handleError<{[key: string]: number}>('getReconciliationPerformance', {}))
    );
  }

  // Obtenir les transactions en attente - correction du type
  getPendingTransactions(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/pending-transactions`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError<string[]>('getPendingTransactions', []))
    );
  }

  // Obtenir les réconciliations non appariées
  getUnmatchedReconciliations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/unmatched-reconciliations`, {
      headers: this.getHeaders()
    }).pipe(
      catchError(this.handleError<any[]>('getUnmatchedReconciliations', []))
    );
  }

  // Obtenir un résumé du statut des réconciliations
  getReconciliationStatusSummary(): Observable<string> {
    return this.http.get(`${this.apiUrl}/reconciliation-status-summary`, {
      headers: this.getHeaders(),
      responseType: 'text' // Spécifier que la réponse est du texte brut
    }).pipe(
      catchError(this.handleError<string>('getReconciliationStatusSummary', 'Aucune donnée disponible'))
    );
  }
}
