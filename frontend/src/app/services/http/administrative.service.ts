import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

import {AdministrativeApi, UserAccountCreationDto, UserAccountDto, UserAccountUpdateAdminDto} from './httpModel';
import {Observable} from 'rxjs';
import {Constants} from "./constants";

@Injectable({
  providedIn: 'root',
})
export class AdministrativeService implements AdministrativeApi {

  constructor(private http: HttpClient) {
  }

  createUserAccount(arg0: UserAccountCreationDto): Observable<number> {
    return this.http.post<number>(Constants.PATH_ADMINISTRATION + 'users/current', arg0);
  }

  getUserAccount(): Observable<UserAccountDto> {
    return this.http.get<UserAccountDto>(Constants.PATH_ADMINISTRATION + 'users/current');
  }

  getUserAccounts(): Observable<UserAccountDto[]> {
    return this.http.get<UserAccountDto[]>(Constants.PATH_ADMINISTRATION + 'users');
  }

  updateUserAccount(username: string, arg1: UserAccountUpdateAdminDto, queryParams?: { username?: string }): Observable<void> {
    return this.http.put<void>(Constants.PATH_ADMINISTRATION + 'users/' + username , arg1);
  }

  createUserAccountForOtherUser(arg0: UserAccountCreationDto): Observable<number> {
    return this.http.post<number>(Constants.PATH_ADMINISTRATION + 'users', arg0);
  }


}
