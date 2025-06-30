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
import { ManualImportComponent } from './components/manual-import/manual-import.component';
import { FileUploadComponent } from './components/file-upload/file-upload.component';
import { ImportStatusComponent } from './components/import-status/import-status.component';
import { AccountListComponent } from './components/account-list/account-list.component';
import { AccountFormComponent } from './components/account-form/account-form.component';
import { AccountDetailComponent } from './components/account-detail/account-detail.component';
import { StatementListComponent } from './components/statement-list/statement-list.component';
import { StatementDetailComponent } from './components/statement-detail/statement-detail.component';
import { StatementFormComponent } from './components/statement-form/statement-form.component';
import { StatementImportManualComponent } from './components/statement-import-manual/statement-import-manual.component';
import { StatementImportFileComponent } from './components/statement-import-file/statement-import-file.component';
import { StatementStatusComponent } from './components/statement-status/statement-status.component';
import { DirectoryConfigComponent } from './components/directory-config/directory-config.component';
import { ProcessListComponent } from './components/process-list/process-list.component';
import { ProcessFormComponent } from './components/process-form/process-form.component';
import { ProcessDetailComponent } from './components/process-detail/process-detail.component';
import { ExecutionListComponent } from './components/execution-list/execution-list.component';




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
    { path: 'manual', component: ManualImportComponent },
  { path: 'upload', component: FileUploadComponent },
  { path: 'status', component: ImportStatusComponent },
    { path: 'accounts', component: AccountListComponent },
  { path: 'entries/view/:id', component: AccountDetailComponent },
  { path: 'entries/edit/:id', component: AccountFormComponent },
    { path: 'stmts', component: StatementListComponent },
  { path: 'stmts/view/:id', component: StatementDetailComponent },
  { path: 'stmts/edit/:id', component: StatementFormComponent },
  { path: 'stmts/new', component: StatementFormComponent },
  { path: 'stmts/import/manual', component: StatementImportManualComponent },
  { path: 'stmts/import/file', component: StatementImportFileComponent },
  { path: 'stmts/status', component: StatementStatusComponent },
  { path: 'config/directories', component: DirectoryConfigComponent },
  { path: 'processes', component: ProcessListComponent },
  { path: 'processes/new', component: ProcessFormComponent },
  { path: 'processes/edit/:id', component: ProcessFormComponent },
  { path: 'processes/view/:id', component: ProcessDetailComponent },
  { path: 'executions', component: ExecutionListComponent },



];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
