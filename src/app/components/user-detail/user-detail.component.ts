import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserServiceService } from 'src/app/services/user-service.service';
import { User } from 'src/app/models/user.model';

@Component({
selector: 'app-user-detail',
templateUrl: './user-detail.component.html',
styleUrls: ['./user-detail.component.css']
})
export class UserDetailComponent implements OnInit {
user: User | null = null;
error: string = '';

constructor(
private route: ActivatedRoute,
private router: Router,
private userService: UserServiceService
) {}

ngOnInit() {
const id = Number(this.route.snapshot.paramMap.get('id'));
this.userService.getById(id).subscribe(
data => this.user = data,
() => this.error = 'Utilisateur non trouvé'
);
}

backToList() {
this.router.navigate(['/admin/users']);
}
}