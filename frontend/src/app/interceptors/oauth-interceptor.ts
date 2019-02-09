import {Injectable, Injector} from "@angular/core";
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {OAuthService} from "../services/oauth.service";
import {flatMap} from "rxjs/operators";
import {Constants} from "../services/http/constants";

@Injectable()
export class OAuthInterceptor implements HttpInterceptor {

  dontAuthPaths : string[] = [
    Constants.PATH_CONFIGURATION
  ];
  oAuthService: OAuthService;

  constructor(
    private injector: Injector) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    const addAuth = !this.dontAuthPaths.find((one => one === req.url));

    if (addAuth) {
      if (!this.oAuthService) {
        this.oAuthService = this.injector.get(OAuthService)
      }

      return this.oAuthService.getAccessToken().pipe(
        flatMap(accessToken => {

          const requestWithAuth = req.clone({
            setHeaders: {
              Authorization: 'Bearer '+accessToken
            }
          });

          return next.handle(requestWithAuth);
        }),
      )

    }else{
      return next.handle(req);
    }

  }

}
