import { Component } from '@angular/core';
import { Router }    from '@angular/router';
import { UserServiceService } from 'src/app/services/user-service.service';  // chemin exact
import { User }      from 'src/app/models/user.model';

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.css']
})
export class UserCreateComponent {
  user: User = { username: '', password: '', role: 'USER' };
  error = '';

  constructor(
    private srv: UserServiceService,
    private router: Router
  ) {}

  save() {
    this.srv.create(this.user).subscribe({
      next: () => this.router.navigate(['/dashboard-admin']),
      error: () => this.error = 'Échec de la création'
    });
  }
}
