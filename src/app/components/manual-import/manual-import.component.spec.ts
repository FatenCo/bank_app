import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManualImportComponent } from './manual-import.component';

describe('ManualImportComponent', () => {
  let component: ManualImportComponent;
  let fixture: ComponentFixture<ManualImportComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ManualImportComponent]
    });
    fixture = TestBed.createComponent(ManualImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
