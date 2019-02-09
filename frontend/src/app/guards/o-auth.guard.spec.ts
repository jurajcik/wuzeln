import {inject, TestBed} from '@angular/core/testing';

import {OAuthGuard} from './o-auth.guard';

describe('OAuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OAuthGuard]
    });
  });

  it('should ...', inject([OAuthGuard], (guard: OAuthGuard) => {
    expect(guard).toBeTruthy();
  }));
});
