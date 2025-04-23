import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportStaticComponent } from './import-static.component';

describe('ImportStaticComponent', () => {
  let component: ImportStaticComponent;
  let fixture: ComponentFixture<ImportStaticComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ImportStaticComponent]
    });
    fixture = TestBed.createComponent(ImportStaticComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
