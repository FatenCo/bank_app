import { TestBed } from '@angular/core/testing';

import { AccountEntryService } from './account-entry.service';

describe('AccountEntryService', () => {
  let service: AccountEntryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AccountEntryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
