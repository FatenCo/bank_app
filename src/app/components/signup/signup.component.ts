import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth-service.service';  // Assurez-vous que vous avez ce service

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  user = {
    username: '',
    password: '',
    role: 'USER' // Valeur par défaut du rôle
  };

  errorMessage: string = '';
  successMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    this.authService.register(this.user).subscribe(
      (response) => {
        this.successMessage = 'Registration successful!';
        this.router.navigate(['/login']); // Redirection vers la page de login
      },
      (error) => {
        this.errorMessage = error.error.message || 'Registration failed!';
      }
    );
  }
}
