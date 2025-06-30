import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatementImportFileComponent } from './statement-import-file.component';

describe('StatementImportFileComponent', () => {
  let component: StatementImportFileComponent;
  let fixture: ComponentFixture<StatementImportFileComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatementImportFileComponent]
    });
    fixture = TestBed.createComponent(StatementImportFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
