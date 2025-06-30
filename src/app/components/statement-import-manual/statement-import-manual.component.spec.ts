import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatementImportManualComponent } from './statement-import-manual.component';

describe('StatementImportManualComponent', () => {
  let component: StatementImportManualComponent;
  let fixture: ComponentFixture<StatementImportManualComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatementImportManualComponent]
    });
    fixture = TestBed.createComponent(StatementImportManualComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
