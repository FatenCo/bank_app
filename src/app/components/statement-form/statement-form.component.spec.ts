import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatementFormComponent } from './statement-form.component';

describe('StatementFormComponent', () => {
  let component: StatementFormComponent;
  let fixture: ComponentFixture<StatementFormComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatementFormComponent]
    });
    fixture = TestBed.createComponent(StatementFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
