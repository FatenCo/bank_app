import { Component } from '@angular/core';
import { HttpEventType } from '@angular/common/http';
import { StatementImportService } from '../../services/statement-import.service';
import { ImportJob } from '../../models/import-job.model';

@Component({
  selector: 'app-statement-import-file',
  templateUrl: './statement-import-file.component.html',
  styleUrls: ['./statement-import-file.component.css']
})
export class StatementImportFileComponent {
  selectedFile?: File;
  progress = 0;
  job?: ImportJob;

  constructor(private service: StatementImportService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files ? input.files[0] : undefined;
  }

  upload(): void {
    if (!this.selectedFile) return;
    this.service.uploadFile(this.selectedFile).subscribe(evt => {
      if (evt.type === HttpEventType.UploadProgress && evt.total) {
        this.progress = Math.round(100 * evt.loaded / evt.total);
      } else if (evt.type === HttpEventType.Response) {
        this.job = evt.body;
      }
    });
  }
}
