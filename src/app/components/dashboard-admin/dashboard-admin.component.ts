import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserServiceService } from 'src/app/services/user-service.service';
import { User } from 'src/app/models/user.model';

@Component({
  selector: 'app-dashboard-admin',
  templateUrl: './dashboard-admin.component.html',
  styleUrls: ['./dashboard-admin.component.css']
})
export class DashboardAdminComponent implements OnInit {
  users: User[] = [];
  totalUsers: number = 0;
  error: string = '';

  constructor(
    private userService: UserServiceService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getAll().subscribe({
      next: users => {
        this.users = users;
        this.totalUsers = users.length;
      },
      error: () => this.error = 'Impossible de charger la liste des utilisateurs'
    });
  }

  view(u: User) {
    this.router.navigate(['/user-detail', u.id]);
  }

  edit(u: User) {
    this.router.navigate(['/user-edit', u.id]);
  }

  delete(u: User) {
    if (!confirm(`Supprimer l'utilisateur ${u.username} ?`)) return;
    this.userService.delete(u.id!).subscribe({
      next: () => this.loadUsers(),
      error: () => this.error = 'Échec de la suppression'
    });
  }
}
