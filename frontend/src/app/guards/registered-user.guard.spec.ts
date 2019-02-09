import {inject, TestBed} from '@angular/core/testing';

import {RegisteredUserGuard} from './registered-user.guard';

describe('RegisteredUserGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RegisteredUserGuard]
    });
  });

  it('should ...', inject([RegisteredUserGuard], (guard: RegisteredUserGuard) => {
    expect(guard).toBeTruthy();
  }));
});
