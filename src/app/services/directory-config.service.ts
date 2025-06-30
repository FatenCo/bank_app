import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DirectoryConfig } from '../models/directory-config.model';

@Injectable({ providedIn: 'root' })
export class DirectoryConfigService {
  private readonly apiUrl = 'http://localhost:8080/api/config/directories';

  constructor(private http: HttpClient) {}

  getConfig(): Observable<DirectoryConfig> {
    return this.http.get<DirectoryConfig>(this.apiUrl);
  }

  updateConfig(cfg: DirectoryConfig): Observable<void> {
    return this.http.put<void>(this.apiUrl, cfg);
  }
}
