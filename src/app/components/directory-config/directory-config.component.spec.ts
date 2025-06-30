import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DirectoryConfigComponent } from './directory-config.component';

describe('DirectoryConfigComponent', () => {
  let component: DirectoryConfigComponent;
  let fixture: ComponentFixture<DirectoryConfigComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DirectoryConfigComponent]
    });
    fixture = TestBed.createComponent(DirectoryConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
