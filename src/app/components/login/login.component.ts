import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth-service.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user = { username: '', password: '' };
  errorMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.authService.login(this.user).subscribe(
      response => {
        localStorage.setItem('token', response.token);  // Save JWT in localStorage
        this.router.navigate(['/home']);  // Redirect to home page
      },
      error => {
        this.errorMessage = error.error;
      }
    );
  }
}
