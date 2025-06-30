import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DirectoryConfigService } from 'src/app/services/directory-config.service';
import { DirectoryConfig } from 'src/app/models/directory-config.model';

@Component({
  selector: 'app-directory-config',
  templateUrl: './directory-config.component.html',
  styleUrls: ['./directory-config.component.css']
})
export class DirectoryConfigComponent implements OnInit {
  form: FormGroup;
  loading = false;
  message = '';

  constructor(
    private fb: FormBuilder,
    private configService: DirectoryConfigService
  ) {
    this.form = this.fb.group({
      accountsDir: ['', Validators.required],
      stmtsDir:    ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.loading = true;
    this.configService.getConfig().subscribe(
      (cfg: DirectoryConfig) => {
        this.form.patchValue(cfg);
        this.loading = false;
      },
      () => this.loading = false
    );
  }

  save(): void {
    if (this.form.invalid) {
      this.message = 'Veuillez corriger les chemins avant de sauvegarder.';
      return;
    }
    const cfg: DirectoryConfig = this.form.value;
    this.configService.updateConfig(cfg).subscribe(
      () => this.message = 'Configuration mise à jour avec succès.',
      () => this.message = 'Erreur lors de la mise à jour.'
    );
  }
}
