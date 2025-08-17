import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProcessDefinition } from '../models/process-definition.model';
import { ProcessExecution } from '../models/process-execution.model';

@Injectable({
  providedIn: 'root'
})
export class ProcessService {
  private API = 'http://localhost:8080/api/processes';

  constructor(private http: HttpClient) {}

  // Liste des définitions de processus
  list(): Observable<ProcessDefinition[]> {
    return this.http.get<ProcessDefinition[]>(this.API);
  }

  // Obtenir une définition de processus par son ID
  get(id: string): Observable<ProcessDefinition> {
    return this.http.get<ProcessDefinition>(`${this.API}/${id}`);
  }

  // Créer une nouvelle définition de processus
  create(pd: ProcessDefinition): Observable<ProcessDefinition> {
    return this.http.post<ProcessDefinition>(this.API, pd);
  }

  // Mettre à jour une définition de processus existante
  update(id: string, pd: ProcessDefinition): Observable<ProcessDefinition> {
    return this.http.put<ProcessDefinition>(`${this.API}/${id}`, pd);
  }

  // Supprimer une définition de processus
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }

  // Lancer un processus immédiatement
  runNow(id: string): Observable<ProcessExecution> {
    return this.http.post<ProcessExecution>(`${this.API}/${id}/run`, {});
  }

  // Arrêter un processus
  stop(id: string): Observable<void> {
    return this.http.post<void>(`${this.API}/${id}/stop`, {});
  }

  // Lister les exécutions d'un processus donné
 listExecutions(definitionId: string): Observable<ProcessExecution[]> {
    return this.http.get<ProcessExecution[]>(
      `${this.API}/${definitionId}/executions`,
      { responseType: 'json' } // Force le type JSON
    );
  }
}
