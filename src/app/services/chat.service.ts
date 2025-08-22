// chat.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, map, timeout } from 'rxjs/operators';
import { 
  ChatRequest, 
  ChatResponse, 
  BankingContextData, 
  ChatbotHealth, 
  ChatbotStatus 
} from 'src/app/models/chat.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly API_BASE_URL = 'http://localhost:8080/api/chatbot';
  private readonly REQUEST_TIMEOUT = 25000; 
  
  // State management
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();
  
  private sessionIdSubject = new BehaviorSubject<string>(this.generateSessionId());
  public sessionId$ = this.sessionIdSubject.asObservable();
  
  private contextDataSubject = new BehaviorSubject<BankingContextData>({});
  public contextData$ = this.contextDataSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initializeContext();
  }

  /**
   * Envoie une question au chatbot
   */
  askQuestion(message: string, userId: string = 'anonymous'): Observable<ChatResponse> {
    this.setLoading(true);
    
    const request: ChatRequest = {
      message: message.trim(),
      userId,
      sessionId: this.sessionIdSubject.value,
      contextData: this.contextDataSubject.value
    };

    return this.http.post<ChatResponse>(`${this.API_BASE_URL}/ask`, request)
      .pipe(
        timeout(this.REQUEST_TIMEOUT),
        map(response => {
          // Mise à jour du sessionId si fourni
          if (response.sessionId) {
            this.sessionIdSubject.next(response.sessionId);
          }
          return response;
        }),
        catchError(this.handleError.bind(this)),
        map(response => {
          this.setLoading(false);
          return response;
        })
      );
  }

  /**
   * Vérifie la santé du service chatbot
   */
  checkHealth(): Observable<ChatbotHealth> {
    return this.http.get<ChatbotHealth>(`${this.API_BASE_URL}/health`)
      .pipe(
        timeout(5000),
        catchError((error) => {
          console.error('Erreur health check:', error);
          // Retourne un objet par défaut en cas d'erreur
          return new Observable<ChatbotHealth>(observer => {
            observer.next({
              status: 'DOWN',
              service: 'Banking Chatbot',
              timestamp: Date.now()
            });
            observer.complete();
          });
        })
      );
  }

  /**
   * Récupère le statut du chatbot
   */
  getStatus(): Observable<ChatbotStatus> {
    return this.http.get<ChatbotStatus>(`${this.API_BASE_URL}/status`)
      .pipe(
        timeout(5000),
        catchError((error) => {
          console.error('Erreur status check:', error);
          // Retourne un objet par défaut en cas d'erreur
          return new Observable<ChatbotStatus>(observer => {
            observer.next({
              chatbotService: 'ERROR',
              version: 'unknown',
              uptime: 0
            });
            observer.complete();
          });
        })
      );
  }

  /**
   * Met à jour le contexte bancaire
   */
  updateContext(context: Partial<BankingContextData>): void {
    const currentContext = this.contextDataSubject.value;
    this.contextDataSubject.next({ ...currentContext, ...context });
  }

  /**
   * Réinitialise le contexte
   */
  resetContext(): void {
    this.contextDataSubject.next({});
    this.sessionIdSubject.next(this.generateSessionId());
  }

  /**
   * Met à jour la page courante dans le contexte
   */
  updateCurrentPage(page: string): void {
    this.updateContext({ currentPage: page });
  }

  /**
   * Met à jour le compte sélectionné
   */
  updateSelectedAccount(accountId: string, accountNumber?: string, balance?: number): void {
    this.updateContext({
      selectedAccountId: accountId,
      accountNumber,
      accountBalance: balance
    });
  }

  /**
   * Exécute une action suggérée
   */
  executeSuggestedAction(action: any): Observable<any> {
    // Implémentation selon le type d'action
    switch (action.type) {
      case 'NAVIGATION':
        // Dans une vraie app, on utiliserait le Router
        console.log('Navigation vers:', action.action);
        return new Observable(observer => {
          observer.next({ success: true, navigatedTo: action.action });
          observer.complete();
        });
        
      case 'PROCESS':
        // Appel API pour déclencher un processus
        return this.http.post(action.action, action.data || {})
          .pipe(catchError((error) => {
            console.error('Erreur lors de l\'exécution de l\'action:', error);
            return throwError(() => error);
          }));
        
      default:
        return throwError(() => new Error('Type d\'action non supporté'));
    }
  }

  /**
   * Gestion des erreurs HTTP
   */
  private handleError(error: any): Observable<ChatResponse> {
    this.setLoading(false);
    
    let errorMessage = 'Une erreur inattendue s\'est produite';
    
    // Gestion spécifique des timeouts
    if (error.name === 'TimeoutError' || error.message?.includes('timeout')) {
      errorMessage = 'La demande prend trop de temps. Veuillez réessayer.';
    } 
    // Gestion des erreurs HTTP
    else if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        errorMessage = 'Impossible de contacter le serveur. Vérifiez votre connexion.';
      } else if (error.status >= 400 && error.status < 500) {
        errorMessage = error.error?.response || 'Erreur dans la requête';
      } else if (error.status >= 500) {
        errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
      }
    }
    // Autres types d'erreurs
    else if (error.message) {
      errorMessage = error.message;
    }

    // Retourne une réponse d'erreur formatée
    const errorResponse: ChatResponse = {
      response: errorMessage,
      success: false,
      suggestedActions: [
        {
          type: 'RETRY',
          label: 'Réessayer',
          action: 'retry'
        }
      ]
    };

    return new Observable(observer => {
      observer.next(errorResponse);
      observer.complete();
    });
  }

  /**
   * Génère un ID de session unique
   */
  private generateSessionId(): string {
    return 'session-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
  }

  /**
   * Initialise le contexte avec des valeurs par défaut
   */
  private initializeContext(): void {
    // Récupération du contexte depuis le localStorage si disponible
    const savedContext = localStorage.getItem('banking-chat-context');
    if (savedContext) {
      try {
        const context = JSON.parse(savedContext);
        this.contextDataSubject.next(context);
      } catch (e) {
        console.warn('Contexte sauvegardé invalide, réinitialisation');
      }
    }

    // Sauvegarde automatique du contexte
    this.contextData$.subscribe(context => {
      localStorage.setItem('banking-chat-context', JSON.stringify(context));
    });
  }

  /**
   * Gestion de l'état de chargement
   */
  private setLoading(loading: boolean): void {
    this.isLoadingSubject.next(loading);
  }

  /**
   * Obtient l'état de chargement actuel
   */
  get isLoading(): boolean {
    return this.isLoadingSubject.value;
  }

  /**
   * Obtient le contexte actuel
   */
  get currentContext(): BankingContextData {
    return this.contextDataSubject.value;
  }

  /**
   * Obtient le sessionId actuel
   */
  get currentSessionId(): string {
    return this.sessionIdSubject.value;
  }
}