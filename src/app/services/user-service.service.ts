import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserServiceService {
  private api = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || '';
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.api, { headers: this.getAuthHeaders() });
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.api}/${id}`, { headers: this.getAuthHeaders() });
  }

  create(u: User): Observable<User> {
    return this.http.post<User>(this.api, u, { headers: this.getAuthHeaders() });
  }

  update(id: number, u: User): Observable<User> {
    return this.http.put<User>(`${this.api}/${id}`, u, { headers: this.getAuthHeaders() });
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.api}/${id}`, {
      headers: this.getAuthHeaders(),
      responseType: 'text'
    });
  }
}
