import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { ChangePasswordComponent } from './components/change-password/change-password.component';
import { DashboardAdminComponent } from './components/dashboard-admin/dashboard-admin.component';
import { UserDetailComponent } from './components/user-detail/user-detail.component';
import { UserEditComponent } from './components/user-edit/user-edit.component';
import { UserCreateComponent } from './components/user-create/user-create.component';
import { ImportStaticComponent } from './components/import-static/import-static.component';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },  
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'home', component: HomeComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: 'dashboard-admin', component: DashboardAdminComponent },
  { path: 'user-detail/:id', component: UserDetailComponent },
  { path: 'user-edit/:id', component: UserEditComponent },
  { path: 'users-create', component: UserCreateComponent },
  {path:'import-account', component:ImportStaticComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
