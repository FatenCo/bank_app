import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportStatusComponent } from './import-status.component';

describe('ImportStatusComponent', () => {
  let component: ImportStatusComponent;
  let fixture: ComponentFixture<ImportStatusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ImportStatusComponent]
    });
    fixture = TestBed.createComponent(ImportStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
