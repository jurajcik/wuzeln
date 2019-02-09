import {Injectable, NgZone} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable, of} from 'rxjs';
import {OAuthGuard} from "./o-auth.guard";
import {OAuthService} from "../services/oauth.service";
import {flatMap, map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AdminGuard extends OAuthGuard implements CanActivate {

  constructor(
    protected oAuthService: OAuthService,
    protected router: Router,
    protected zone: NgZone
) {
    super(oAuthService, router, zone);
  }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return super.canActivate(next, state).pipe(
      flatMap(isAuthenticated => {
        if (isAuthenticated) {

          return this.oAuthService.getUserAccount().pipe(
            map(userAccount => {
              if (userAccount.admin) {
                return true;
              } else {
                this.zone.run(() => {
                  this.router.navigate(['/']);
                });
              }
            })
          )
        } else {
          return of(false);
        }
      })
    )
  }
}
