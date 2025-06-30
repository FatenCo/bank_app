import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatementStatusComponent } from './statement-status.component';

describe('StatementStatusComponent', () => {
  let component: StatementStatusComponent;
  let fixture: ComponentFixture<StatementStatusComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatementStatusComponent]
    });
    fixture = TestBed.createComponent(StatementStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
