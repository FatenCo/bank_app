import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionListComponent } from './execution-list.component';

describe('ExecutionListComponent', () => {
  let component: ExecutionListComponent;
  let fixture: ComponentFixture<ExecutionListComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ExecutionListComponent]
    });
    fixture = TestBed.createComponent(ExecutionListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
