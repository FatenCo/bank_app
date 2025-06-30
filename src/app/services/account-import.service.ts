import { Injectable } from '@angular/core';
import { HttpClient, HttpEventType } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountEntry } from '../models/account-entry.model';
import { ImportJob } from '../models/import-job.model';

@Injectable({ providedIn: 'root' })
export class AccountImportService {
  private baseUrl = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) {}

  importManual(entries: AccountEntry[]): Observable<ImportJob> {
    return this.http.post<ImportJob>(`${this.baseUrl}/manual`, entries);
  }

  uploadFile(file: File): Observable<any> {
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http.post<ImportJob>(`${this.baseUrl}/upload`, form, {
      reportProgress: true,
      observe: 'events'
    });
  }

 getStatus(id: string): Observable<ImportJob> {
    return this.http.get<ImportJob>(`${this.baseUrl}/status/${id}`);
  }

  searchByFileName(name: string): Observable<ImportJob[]> {
    return this.http.get<ImportJob[]>(`${this.baseUrl}/search/file/${encodeURIComponent(name)}`);
  }

  searchByDate(date: string): Observable<ImportJob[]> {
    return this.http.get<ImportJob[]>(`${this.baseUrl}/search/date/${date}`);
  }

}