import { Component } from '@angular/core';
import { AuthService } from 'src/app/services/auth-service.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  username: string = '';
  newPassword: string = '';
  message: string = '';
  messageColor: string = 'black';

  constructor(private authService: AuthService, private router: Router) {}

  changePassword() {
    this.authService.changePassword(this.username, this.newPassword).subscribe(
      response => {
        this.message = '✅ Mot de passe changé avec succès. Redirection vers la connexion...';
        this.messageColor = 'green';
  
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error => {
        console.error(error); // <-- pour voir dans la console ce qu'il renvoie
        // Si le backend renvoie un message textuel
        if (error.error && error.error.message) {
          this.message = '❌ Erreur : ' + error.error.message;
        } 
        // Sinon, si c'est du texte brut ou un string
        else if (typeof error.error === 'string') {
          this.message = '❌ Erreur : ' + error.error;
        } 
        // Sinon fallback générique
        else {
          this.message = '❌ Une erreur est survenue.';
        }
  
        this.messageColor = 'red';
      }
    );
  }
  
  
}
