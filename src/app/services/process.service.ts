import { Injectable } from '@angular/core';
import { HttpClient }    from '@angular/common/http';
import { Observable }    from 'rxjs';
import { ProcessDefinition } from '../models/process-definition.model';
import { ProcessExecution  } from '../models/process-execution.model';

@Injectable({ providedIn: 'root' })
export class ProcessService {
  private API = 'http://localhost:8080/api/processes';

  constructor(private http: HttpClient) {}

  list(): Observable<ProcessDefinition[]> {
    return this.http.get<ProcessDefinition[]>(this.API);
  }

  get(id: string): Observable<ProcessDefinition> {
    return this.http.get<ProcessDefinition>(`${this.API}/${id}`);
  }

  create(pd: ProcessDefinition): Observable<ProcessDefinition> {
    return this.http.post<ProcessDefinition>(this.API, pd);
  }

  update(id: string, pd: ProcessDefinition): Observable<ProcessDefinition> {
    return this.http.put<ProcessDefinition>(`${this.API}/${id}`, pd);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }

  runNow(id: string): Observable<ProcessExecution> {
    return this.http.post<ProcessExecution>(`${this.API}/${id}/run`, {});
  }

  /** STOP d’une planification */
  stop(id: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${id}/stop`, {});
  }

  listExecutions(definitionId: string): Observable<ProcessExecution[]> {
    return this.http.get<ProcessExecution[]>(`${this.API}/${definitionId}/executions`);
  }
}
