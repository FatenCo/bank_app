import { TestBed } from '@angular/core/testing';

import { DirectoryConfigService } from './directory-config.service';

describe('DirectoryConfigService', () => {
  let service: DirectoryConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DirectoryConfigService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
