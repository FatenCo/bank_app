import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class StaticImportService {
  private readonly apiUrl = 'http://localhost:8080/api/static/accounts';

  constructor(private http: HttpClient) {}

  /** Retourne les headers avec le Bearer token */
  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') ?? '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  /** Enregistrement manuel (POST JSON) */
  createManual(account: Account): Observable<Account> {
    return this.http.post<Account>(
      this.apiUrl,
      account,
      { headers: this.getAuthHeaders() }
    );
  }

  /** Import depuis un fichier Excel/CSV (multipart/form-data) */
  importFile(file: File): Observable<string[]> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    // NE PAS forcer Content-Type : Angular s’occupe de boundary
    return this.http.post<string[]>(
      `${this.apiUrl}/upload`,
      formData,
      { headers: this.getAuthHeaders() }
    );
  }
}
