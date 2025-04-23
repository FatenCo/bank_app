import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserServiceService } from 'src/app/services/user-service.service';
import { User } from 'src/app/models/user.model';

@Component({
selector: 'app-user-edit',
templateUrl: './user-edit.component.html',
styleUrls: ['./user-edit.component.css']
})
export class UserEditComponent implements OnInit {
user: User = { username: '', role: '' };
error: string = '';
success: string = '';

constructor(
private route: ActivatedRoute,
private router: Router,
private userService: UserServiceService
) {}

ngOnInit() {
const id = Number(this.route.snapshot.paramMap.get('id'));
this.userService.getById(id).subscribe(
data => this.user = data,
() => this.error = 'Utilisateur introuvable'
);
}

save() {
    if (this.user.id) {
      this.userService.update(this.user.id, this.user).subscribe(
        () => {
          this.success = 'Utilisateur mis à jour avec succès.';
          setTimeout(() => this.router.navigate(['/dashboard-admin']), 1500);
        },
        () => this.error = 'Erreur lors de la mise à jour'
      );
    }
  }
  
  cancel() {
    this.router.navigate(['/dashboard-admin']);
  }
  
}