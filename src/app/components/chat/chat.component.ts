// chat.component.ts
import { Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ChatService } from 'src/app/services/chat.service';
import { ChatMessage, SuggestedAction, BankingContextData } from 'src/app/models/chat.model';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  @ViewChild('messageInput') private messageInput!: ElementRef;

  messages: ChatMessage[] = [];
  currentMessage = '';
  isLoading = false;
  isConnected = true;
  contextData: BankingContextData = {};
  
  // Suggestions rapides
  quickSuggestions = [
    'Comment faire une réconciliation manuelle ?',
    'Pourquoi mes comptes ne sont pas lettrés ?',
    'Comment configurer les tolérances de lettrage ?',
    'Comment importer un fichier bancaire ?',
    'Que faire en cas d\'échec d\'import ?'
  ];

  private destroy$ = new Subject<void>();
  private shouldScrollToBottom = false;

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    this.initializeChat();
    this.subscribeToServices();
    this.checkServiceHealth();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  /**
   * Initialise le chat avec un message de bienvenue
   */
  private initializeChat(): void {
    const welcomeMessage: ChatMessage = {
      id: this.generateMessageId(),
      content: `👋 Bonjour ! Je suis votre assistant spécialisé en réconciliation bancaire.
      
Je peux vous aider avec :
• Les processus de lettrage automatique et manuel
• La résolution d'écarts et différences  
• La configuration des règles de tolérance
• L'import de fichiers bancaires
• L'analyse des échecs de réconciliation

Comment puis-je vous aider aujourd'hui ?`,
      isUser: false,
      timestamp: new Date(),
      suggestedActions: [
        {
          type: 'NAVIGATION',
          label: 'Tableau de bord',
          action: '/dashboard'
        },
        {
          type: 'NAVIGATION', 
          label: 'Réconciliations',
          action: '/reconciliation'
        }
      ]
    };

    this.messages = [welcomeMessage];
    this.shouldScrollToBottom = true;
  }

  /**
   * S'abonne aux services
   */
  private subscribeToServices(): void {
    this.chatService.isLoading$
      .pipe(takeUntil(this.destroy$))
      .subscribe(loading => {
        this.isLoading = loading;
      });

    this.chatService.contextData$
      .pipe(takeUntil(this.destroy$))
      .subscribe(context => {
        this.contextData = context;
      });
  }

  /**
   * Vérifie la santé du service - CHANGED TO PUBLIC
   */
  public checkServiceHealth(): void {
    this.chatService.checkHealth()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (health) => {
          this.isConnected = health.status === 'UP';
        },
        error: () => {
          this.isConnected = false;
        }
      });
  }

  /**
   * Envoie un message
   */
  sendMessage(): void {
    const message = this.currentMessage.trim();
    if (!message || this.isLoading) return;

    // Ajoute le message utilisateur
    const userMessage: ChatMessage = {
      id: this.generateMessageId(),
      content: message,
      isUser: true,
      timestamp: new Date()
    };
    
    this.messages.push(userMessage);
    this.currentMessage = '';
    this.shouldScrollToBottom = true;

    // Ajoute un indicateur de chargement
    const loadingMessage: ChatMessage = {
      id: this.generateMessageId(),
      content: 'En cours de réflexion...',
      isUser: false,
      timestamp: new Date(),
      isLoading: true
    };
    
    this.messages.push(loadingMessage);
    this.shouldScrollToBottom = true;

    // Envoie la requête au service
    this.chatService.askQuestion(message)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          // Supprime le message de chargement
          this.messages = this.messages.filter(m => !m.isLoading);
          
          // Ajoute la réponse
          const botMessage: ChatMessage = {
            id: this.generateMessageId(),
            content: response.response,
            isUser: false,
            timestamp: new Date(),
            suggestedActions: response.suggestedActions,
            isError: !response.success
          };
          
          this.messages.push(botMessage);
          this.shouldScrollToBottom = true;
        },
        error: (error) => {
          // Supprime le message de chargement
          this.messages = this.messages.filter(m => !m.isLoading);
          
          // Ajoute un message d'erreur
          const errorMessage: ChatMessage = {
            id: this.generateMessageId(),
            content: 'Désolé, je rencontre un problème technique. Veuillez réessayer.',
            isUser: false,
            timestamp: new Date(),
            isError: true
          };
          
          this.messages.push(errorMessage);
          this.shouldScrollToBottom = true;
        }
      });
  }

  /**
   * Utilise une suggestion rapide
   */
  useSuggestion(suggestion: string): void {
    this.currentMessage = suggestion;
    this.sendMessage();
  }

  /**
   * Exécute une action suggérée
   */
  executeSuggestedAction(action: SuggestedAction): void {
    console.log('Exécution de l\'action:', action);
    
    this.chatService.executeSuggestedAction(action)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (result) => {
          // Affiche un feedback selon le type d'action
          let feedbackMessage = '';
          switch (action.type) {
            case 'NAVIGATION':
              feedbackMessage = `🔄 Navigation vers: ${action.label}`;
              break;
            case 'PROCESS':
              feedbackMessage = `⚡ Processus lancé: ${action.label}`;
              break;
            default:
              feedbackMessage = `✅ Action exécutée: ${action.label}`;
          }
          
          const feedback: ChatMessage = {
            id: this.generateMessageId(),
            content: feedbackMessage,
            isUser: false,
            timestamp: new Date()
          };
          
          this.messages.push(feedback);
          this.shouldScrollToBottom = true;
        },
        error: (error) => {
          const errorFeedback: ChatMessage = {
            id: this.generateMessageId(),
            content: `❌ Erreur lors de l'exécution: ${action.label}`,
            isUser: false,
            timestamp: new Date(),
            isError: true
          };
          
          this.messages.push(errorFeedback);
          this.shouldScrollToBottom = true;
        }
      });
  }

  /**
   * Réinitialise la conversation
   */
  resetConversation(): void {
    this.messages = [];
    this.chatService.resetContext();
    this.initializeChat();
  }

  /**
   * Met à jour le contexte depuis l'interface
   */
  updateContext(field: keyof BankingContextData, value: any): void {
    const update = { [field]: value };
    this.chatService.updateContext(update);
  }

  /**
   * Gestion de la touche Entrée
   */
  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  /**
   * Faire défiler vers le bas
   */
  private scrollToBottom(): void {
    try {
      const container = this.messagesContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    } catch (err) {
      console.error('Erreur lors du scroll:', err);
    }
  }

  /**
   * Génère un ID unique pour les messages
   */
  private generateMessageId(): string {
    return 'msg-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
  }

  /**
   * Formate la date pour l'affichage
   */
  formatTime(date: Date): string {
    return date.toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }

  /**
   * Obtient l'icône pour le type d'action
   */
  getActionIcon(actionType: string): string {
    switch (actionType) {
      case 'NAVIGATION': return '🔗';
      case 'PROCESS': return '⚡';
      case 'RECONCILIATION': return '🔄';
      case 'IMPORT': return '📤';
      case 'CONFIGURATION': return '⚙️';
      default: return '▶️';
    }
  }

  /**
   * Formate le contenu du message (gère les retours à la ligne)
   */
  formatMessageContent(content: string): string {
    return content
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>');
  }

  /**
   * TrackBy function pour optimiser le rendu des messages
   */
  trackByMessageId(index: number, message: ChatMessage): string {
    return message.id;
  }
}