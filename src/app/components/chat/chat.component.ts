import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef } from '@angular/core';
import { ChatService, ChatMessage } from 'src/app/services/chat.service'

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  
  messages: ChatMessage[] = [];
  currentMessage: string = '';
  isLoading: boolean = false;
  isConnected: boolean = false;

  // Suggestions rapides
  quickSuggestions = [
    'démarrer process',
    'état des process', 
    'réconciliation manuelle',
    'charger fichiers',
    'arrêter process'
  ];

  constructor(private chatService: ChatService) {}

  ngOnInit() {
    this.checkConnection();
    this.addWelcomeMessage();
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  checkConnection() {
    this.chatService.checkHealth().subscribe({
      next: () => {
        this.isConnected = true;
      },
      error: () => {
        this.isConnected = false;
        this.messages.push({
          message: "⚠️ Connexion au backend impossible. Vérifiez que le service Spring Boot est démarré.",
          isUser: false,
          timestamp: new Date()
        });
      }
    });
  }

  addWelcomeMessage() {
    this.messages.push({
      message: `👋 **Bonjour !** Je suis votre assistant pour la réconciliation bancaire.

🔧 **Je peux vous aider avec :**
• Gérer les process de réconciliation
• Expliquer les procédures 
• Résoudre les problèmes d'import
• Diagnostiquer les écarts

💬 **Tapez votre question ou utilisez les suggestions ci-dessous.**`,
      isUser: false,
      timestamp: new Date()
    });
  }

  sendMessage() {
    if (!this.currentMessage.trim() || this.isLoading) return;

    // Ajouter message utilisateur
    this.messages.push({
      message: this.currentMessage,
      isUser: true,
      timestamp: new Date()
    });

    const userMessage = this.currentMessage;
    this.currentMessage = '';
    this.isLoading = true;

    // Appel API Backend Spring Boot
    this.chatService.sendMessage(userMessage).subscribe({
      next: (response) => {
        this.messages.push({
          message: response.response,
          isUser: false,
          timestamp: new Date(),
          intent: response.intent,
          confidence: response.confidence
        });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur chat:', error);
        this.messages.push({
          message: "❌ Erreur de connexion. Vérifiez que le backend Spring Boot et le microservice Python sont démarrés.",
          isUser: false,
          timestamp: new Date()
        });
        this.isLoading = false;
      }
    });
  }

  sendQuickMessage(suggestion: string) {
    this.currentMessage = suggestion;
    this.sendMessage();
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  formatMessage(message: string): string {
    // Conversion markdown simple pour affichage
    return message
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>')
      .replace(/•/g, '&bull;');
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch(err) {}
  }

  // Test de connexion
  testConnection() {
    this.chatService.testChat().subscribe({
      next: (response) => {
        this.messages.push({
          message: `🧪 **Test réussi !** Intent: ${response.intent}, Confiance: ${(response.confidence * 100).toFixed(1)}%`,
          isUser: false,
          timestamp: new Date()
        });
      },
      error: (error) => {
        this.messages.push({
          message: "❌ Test de connexion échoué.",
          isUser: false,
          timestamp: new Date()
        });
      }
    });
  }
}