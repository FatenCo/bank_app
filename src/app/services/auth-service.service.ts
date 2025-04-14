import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth';  

  constructor(private http: HttpClient) {}

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  login(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user);
  }

  changePassword(username: string, newPassword: string): Observable<any> {
    // Envoi des données dans le corps de la requête pour éviter des erreurs liées à l'URL
    const payload = { username, newPassword };  
    return this.http.post(`${this.apiUrl}/change-password`, payload, { responseType: 'text' });
  }
  
}
