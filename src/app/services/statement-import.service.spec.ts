import { TestBed } from '@angular/core/testing';

import { StatementImportService } from './statement-import.service';

describe('StatementImportService', () => {
  let service: StatementImportService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StatementImportService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
