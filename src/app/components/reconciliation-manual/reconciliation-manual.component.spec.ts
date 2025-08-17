import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReconciliationManualComponent } from './reconciliation-manual.component';

describe('ReconciliationManualComponent', () => {
  let component: ReconciliationManualComponent;
  let fixture: ComponentFixture<ReconciliationManualComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ReconciliationManualComponent]
    });
    fixture = TestBed.createComponent(ReconciliationManualComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
