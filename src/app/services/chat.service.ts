import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  message: string;
  isUser: boolean;
  timestamp: Date;
  intent?: string;
  confidence?: number;
}

export interface ChatRequest {
  message: string;
}

export interface ChatResponse {
  response: string;
  intent: string;
  confidence: number;
  timestamp: string;
  success: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  sendMessage(message: string): Observable<ChatResponse> {
    const request: ChatRequest = { message };
    return this.http.post<ChatResponse>(`${this.apiUrl}/chat`, request);
  }

  checkHealth(): Observable<any> {
    return this.http.get(`${this.apiUrl}/chat/health`);
  }

  testChat(): Observable<ChatResponse> {
    return this.http.get<ChatResponse>(`${this.apiUrl}/chat/test`);
  }
}
