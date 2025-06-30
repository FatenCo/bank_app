// src/app/app.module.ts

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms'; 
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { FooterComponent } from './components/footer/footer.component';
import { HomeComponent } from './components/home/home.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
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





@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    HomeComponent,
    SidebarComponent,
    LoginComponent,
    SignupComponent,
    ChangePasswordComponent,
    DashboardAdminComponent,
    UserDetailComponent,
    UserEditComponent,
    UserCreateComponent,
    ManualImportComponent,
    FileUploadComponent,
    ImportStatusComponent,
    AccountListComponent,
    AccountFormComponent,
    AccountDetailComponent,
    StatementListComponent,
    StatementDetailComponent,
    StatementFormComponent,
    StatementImportManualComponent,
    StatementImportFileComponent,
    StatementStatusComponent,
    DirectoryConfigComponent,
    ProcessListComponent,
    ProcessFormComponent,
    ProcessDetailComponent,
    ExecutionListComponent
        ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
