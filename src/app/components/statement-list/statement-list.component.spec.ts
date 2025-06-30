import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatementListComponent } from './statement-list.component';

describe('StatementListComponent', () => {
  let component: StatementListComponent;
  let fixture: ComponentFixture<StatementListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StatementListComponent]
    });
    fixture = TestBed.createComponent(StatementListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
