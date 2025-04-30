import { Component, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { StaticImportService } from 'src/app/services/static-import.service';
import { Account } from 'src/app/models/account.model';
import { ImportResult } from 'src/app/models/import-result.model';

@Component({
  selector: 'app-import-static',
  templateUrl: './import-static.component.html',
  styleUrls: ['./import-static.component.css']
})
export class ImportStaticComponent {
  manualForm: FormGroup;
  selectedFile: File|null = null;
  importResult: ImportResult|null = null;
  errorMessage = '';
  successMessage = '';
  activeTab: 'manual'|'file' = 'manual';
  submittedManual = false;

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  constructor(
    private fb: FormBuilder,
    private svc: StaticImportService
  ) {
    // On applique `notZeroValidator` à **tous** les champs
    const controlsConfig: any = {
      accountNo:      ['', [Validators.required, this.notZeroValidator]],
      shortName:      ['', this.notZeroValidator],
      mnemonic:       ['', this.notZeroValidator],
      accountOfficer: ['', this.notZeroValidator],
      product:        ['', this.notZeroValidator],
      currency:       ['', [Validators.required, this.notZeroValidator]],
      customerId:     ['', [Validators.required, this.notZeroValidator]],
      maCode:         ['', this.notZeroValidator],
      accountType:    ['', this.notZeroValidator],
      coCode:         ['', this.notZeroValidator],
      importFileName: ['', [Validators.required, this.notZeroValidator]],
      importDate:     ['', Validators.required]
    };
    this.manualForm = this.fb.group(controlsConfig);
  }

  /** Sélection onglet */
  selectTab(tab: 'manual'|'file') {
    this.activeTab = tab;
    this.clearMessages();
    this.submittedManual = false;
    this.selectedFile = null;
    // avoid optional chaining pro-parser error
    if (this.fileInputRef) this.fileInputRef.nativeElement.value = '';
  }

  /** Validator : interdit strictement la chaîne "0" */
  notZeroValidator(control: AbstractControl) {
    return control.value === '0'
      ? { zeroNotAllowed: true }
      : null;
  }

  /** Soumission du manuel */
  submitManual() {
    this.submittedManual = true;
    if (this.manualForm.invalid) {
      this.errorMessage = '❌ Veuillez corriger les erreurs avant de soumettre.';
      return;
    }
    this.clearMessages();
    const acct: Account = this.manualForm.value;
    this.svc.importManual(acct).subscribe({
      next: res => {
        this.importResult = res;
        if (res.failureCount > 0) {
          this.errorMessage = `⚠️ ${res.failureCount} erreur(s) détectée(s)`;
        } else {
          this.successMessage = '✅ Import manuel réussi !';
        }
      },
      error: err => {
        this.errorMessage = err.error?.logs?.[0]?.message || err.message;
      }
    });
  }

  /** Sélection fichier */
  onFileSelected(evt: Event) {
    this.clearMessages();
    const input = evt.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  /** Upload fichier */
  uploadFile() {
    if (!this.selectedFile) return;
    this.clearMessages();
    this.svc.uploadFile(this.selectedFile).subscribe({
      next: res => {
        this.importResult = res;
        if (res.failureCount > 0) {
          this.errorMessage = `⚠️ ${res.failureCount} échec(s) sur ${res.total}`;
        } else {
          this.successMessage = `✅ ${res.total} lignes importées`;
        }
      },
      error: err => {
        this.errorMessage = err.error?.logs?.[0]?.message || err.message;
      }
    });
  }

  /** Indique si on doit afficher le tableau de logs */
  hasLogs(): boolean {
    return !!(this.importResult && this.importResult.logs.length > 0);
  }

  /** Efface messages & résultat */
  private clearMessages() {
    this.errorMessage = '';
    this.successMessage = '';
    this.importResult = null;
  }
}
