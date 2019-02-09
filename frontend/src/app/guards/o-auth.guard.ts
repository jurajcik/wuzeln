import {Injectable, NgZone} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {OAuthService} from "../services/oauth.service";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class OAuthGuard implements CanActivate {

  constructor(
    protected oAuthService: OAuthService,
    protected router: Router,
    protected zone: NgZone
  ) {
  }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
    return this.oAuthService.isAuthenticated().pipe(
      map(isAuthenticated => {
        if (isAuthenticated) {
          return true
        } else {
          this.zone.run(() => {
            this.router.navigate(['public']);
          });
          return false;
        }
      })
    );
  }
}
