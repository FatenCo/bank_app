import { Component, ElementRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StaticImportService } from 'src/app/services/static-import.service';
import { Account } from 'src/app/models/account.model';

@Component({
  selector: 'app-import-static',
  templateUrl: './import-static.component.html',
  styleUrls: ['./import-static.component.css']
})
export class ImportStaticComponent {
  manualForm: FormGroup;
  selectedFile: File | null = null;
  logs: string[] = [];
  errorMessage = '';
  activeTab: 'manual' | 'file' = 'manual';

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  constructor(
    private fb: FormBuilder,
    private staticImportService: StaticImportService
  ) {
    this.manualForm = this.fb.group({
      accountNo: ['', Validators.required],
      shortName: [''],
      mnemonic: [''],
      accountOfficer: [''],
      product: [''],
      currency: [''],
      customerId: [''],
      maCode: [''],
      accountType: [''],
      coCode: ['']
    });
  }

  /** Change d’onglet */
  selectTab(tab: 'manual' | 'file') {
    this.activeTab = tab;
    this.errorMessage = '';
    this.logs = [];
    this.selectedFile = null;
    if (this.fileInputRef) {
      this.fileInputRef.nativeElement.value = '';
    }
  }

  /** Quand on sélectionne un fichier */
  onFileSelected(evt: Event) {
    this.errorMessage = '';
    this.logs = [];
    const input = evt.target as HTMLInputElement;
    this.selectedFile = (input.files && input.files.length > 0)
      ? input.files[0]
      : null;
  }

  /** Enregistrement manuel */
  submitManual() {
    if (this.manualForm.invalid) return;
    this.errorMessage = '';
    this.staticImportService.createManual(this.manualForm.value).subscribe({
      next: () => {
        alert('Enregistrement manuel réussi');
        this.manualForm.reset();
      },
      error: (err: any) => {
        this.errorMessage = err.error?.[0] || err.message || 'Erreur lors de l’enregistrement';
      }
    });
  }

  /** Import par fichier */
  uploadFile() {
    if (!this.selectedFile) return;
    this.errorMessage = '';
    this.logs = [];
    this.staticImportService.importFile(this.selectedFile).subscribe({
      next: (res: string[]) => this.logs = res,
      error: (err: any) => {
        this.errorMessage = err.error?.[0] || err.message || 'Erreur lors de l’import';
      }
    });
  }
}
