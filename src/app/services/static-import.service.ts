import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account } from '../models/account.model';
import { ImportResult } from '../models/import-result.model';

@Injectable({ providedIn: 'root' })
export class StaticImportService {
  private readonly apiUrl = 'http://localhost:8080/api/static/accounts';

  constructor(private http: HttpClient) {}

  private authHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  importManual(acct: Account): Observable<ImportResult> {
    return this.http.post<ImportResult>(
      this.apiUrl,
      acct,
      { headers: this.authHeaders() }
    );
  }

  uploadFile(file: File): Observable<ImportResult> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    return this.http.post<ImportResult>(
      `${this.apiUrl}/upload`,
      fd,
      { headers: this.authHeaders() }
    );
  }
}
