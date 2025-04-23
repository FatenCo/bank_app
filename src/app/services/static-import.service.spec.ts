import { TestBed } from '@angular/core/testing';

import { StaticImportService } from './static-import.service';

describe('StaticImportService', () => {
  let service: StaticImportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StaticImportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
