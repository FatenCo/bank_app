// src/app/services/account-entry.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountEntry } from '../models/account-entry.model';

@Injectable({ providedIn: 'root' })
export class AccountEntryService {
  private baseUrl = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) {}

  list(): Observable<AccountEntry[]> {
    return this.http.get<AccountEntry[]>(`${this.baseUrl}/entries`);
  }

  get(id: string): Observable<AccountEntry> {
    return this.http.get<AccountEntry>(`${this.baseUrl}/entries/${id}`);
  }

  update(id: string, entry: AccountEntry): Observable<AccountEntry> {
    return this.http.put<AccountEntry>(`${this.baseUrl}/entries/${id}`, entry);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/entries/${id}`);
  }
}
