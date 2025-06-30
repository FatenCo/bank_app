import { Component } from '@angular/core';
import { HttpEventType } from '@angular/common/http';
import { AccountImportService } from '../../services/account-import.service';
import { ImportJob } from '../../models/import-job.model';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.css']
})
export class FileUploadComponent {
  selectedFile?: File;
  progress = 0;
  job?: ImportJob;

  constructor(private service: AccountImportService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files ? input.files[0] : undefined;
    this.progress = 0;
  }

  upload(): void {
    if (!this.selectedFile) return;
    this.service.uploadFile(this.selectedFile).subscribe(event => {
      if (event.type === HttpEventType.UploadProgress && event.total) {
        this.progress = Math.round(100 * event.loaded / event.total);
      } else if (event.type === HttpEventType.Response) {
        this.job = event.body as ImportJob;
      }
    });
  }
}