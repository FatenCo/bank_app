import { Injectable } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StatementEntry } from '../models/statement-entry.model';
import { ImportJob } from '../models/import-job.model';

@Injectable({ providedIn: 'root' })
export class StatementImportService {
  private base = 'http://localhost:8080/api/stmts';

  constructor(private http: HttpClient) {}

  // Import manuel (liste d'entrées)
  importManual(entries: StatementEntry[]): Observable<ImportJob> {
    return this.http.post<ImportJob>(`${this.base}/manual`, entries);
  }

  // Upload de fichier
  uploadFile(file: File): Observable<any> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<ImportJob>(`${this.base}/upload`, form, {
      reportProgress: true,
      observe: 'events'
    });
  }

  // Statut d'un job
  getStatus(id: string): Observable<ImportJob> {
    return this.http.get<ImportJob>(`${this.base}/status/${id}`);
  }

  // CRUD statements
  list(): Observable<StatementEntry[]> {
    return this.http.get<StatementEntry[]>(`${this.base}/entries`);
  }
  get(id: string): Observable<StatementEntry> {
    return this.http.get<StatementEntry>(`${this.base}/entries/${id}`);
  }
  update(id: string, entry: StatementEntry): Observable<StatementEntry> {
    return this.http.put<StatementEntry>(`${this.base}/entries/${id}`, entry);
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/entries/${id}`);
  }
}