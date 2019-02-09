import {Injectable} from '@angular/core';
import {from, Observable, of, ReplaySubject, throwError} from "rxjs";
import {catchError, flatMap, map} from "rxjs/operators";
import {StorageKey, StorageService} from "./storage.service";
import {JwtHelperService} from '@auth0/angular-jwt';
import {ConfigurationService} from "./configuration.service";
import {GoogleAuthService, NgGapiClientConfig} from "ng-gapi";
import {throwCustomError} from "../handlers/custom-error-handler";
import {AdministrativeService} from "./http/administrative.service";
import {UserAccountDto} from "./http/httpModel";
import GoogleUser = gapi.auth2.GoogleUser;


@Injectable({
  providedIn: 'root'
})
export class OAuthService {

  jwtHelperService = new JwtHelperService();
  currentUserAccountSubject = new ReplaySubject<UserAccount>();
  currentUserAccount: UserAccount;


  constructor(
    private googleAuth: GoogleAuthService,
    private storageService: StorageService,
    private administrativeService: AdministrativeService
  ) {
    // this.currentUserAccount = {
    //   familyName: undefined,
    //   givenName: undefined,
    //   username: undefined,
    //   admin: false,
    //   registered: false,
    //   googleAccountId: undefined
    // }
  }

  isAuthenticated(): Observable<boolean> {
    return this.refreshTokensIfNeeded().pipe(
      map((tokens) => true),
      catchError(err => of(false))
    );
  }

  getUserAccount(): Observable<UserAccount> {

    return this.refreshTokensIfNeeded().pipe(
      flatMap(tokens => {

        if (this.currentUserAccount) {
          return of(this.currentUserAccount)

        } else {
          return this.reloadUserAccount(tokens.idToken)
        }
      })
    )
  }

  private reloadUserAccount(idToken: string) {
    return this.administrativeService.getUserAccount().pipe(
      map(userAccountDto => {

        const userAccount = userAccountDto as UserAccount;
        const user = this.jwtHelperService.decodeToken(idToken);

        userAccount.familyName = user.family_name;
        userAccount.givenName = user.given_name;

        this.currentUserAccountSubject.next(userAccount);
        this.currentUserAccount = userAccount;

        return this.currentUserAccount
      })
    )
  }

  onUserChange(): ReplaySubject<UserAccountDto> {
    return this.currentUserAccountSubject;
  }

  getAccessToken(): Observable<string> {
    return this.refreshTokensIfNeeded().pipe(
      map((tokens) => tokens.accessToken)
    );
  }

  private refreshTokensIfNeeded(): Observable<Tokens> {
    if (this.tokensNotPresentOrExpired()) {
      return this.fetchAndStoreTokens();
    } else {
      return of({
        idToken: this.storageService.get(StorageKey.OAUTH_ID_TOKEN),
        accessToken: this.storageService.get(StorageKey.OAUTH_ACCESS_TOKEN)
      });
    }
  }


  private tokensNotPresentOrExpired(): boolean {
    const idToken = this.storageService.get(StorageKey.OAUTH_ID_TOKEN);

    if (idToken) {
      return this.jwtHelperService.isTokenExpired(idToken, 120);
    }
    return true;
  }


  private fetchAndStoreTokens(): Observable<Tokens> {
    console.log("fetching oauth token");

    return this.googleAuth.getAuth().pipe(
      flatMap((auth) => {

        // request user reload
        this.currentUserAccount = undefined;

        return from(auth.signIn()).pipe(
          flatMap(res => {
            const user = res as GoogleUser;
            console.log('social login result: ' + user.isSignedIn());

            if (!user.isSignedIn()) {
              throw throwError('failed to log in: ' + user)
            }

            const idToken = user.getAuthResponse().id_token;
            const accessToken = user.getAuthResponse().access_token;

            this.storageService.save(StorageKey.OAUTH_ACCESS_TOKEN, accessToken);
            this.storageService.save(StorageKey.OAUTH_ID_TOKEN, idToken);

            return of({
              idToken: idToken,
              accessToken: accessToken
            })
          })
        )
      }),
      catchError(error => {
        return throwCustomError('Failed to authenticate the user', error);
      })
    )

  }

}

class Tokens {
  idToken: string;
  accessToken: string;
}

export interface UserAccount extends UserAccountDto {
  familyName: string;
  givenName: string;
}

export function getAuthServiceConfigs(configService: ConfigurationService): NgGapiClientConfig {
  return {
    client_id: configService.getConfig().oauthClientId,
    discoveryDocs: ["https://analyticsreporting.googleapis.com/$discovery/rest?version=v4"],
    scope: 'openid'
  };
}

