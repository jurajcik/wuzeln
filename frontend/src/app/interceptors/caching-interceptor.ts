import {Injectable} from "@angular/core";
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {RequestCacheService} from "../services/requestCache.service";

@Injectable()
export class CachingInterceptor implements HttpInterceptor {


  constructor(
    private cache: RequestCacheService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    var cachedResponse;

    if (req.url === 'services/persons' && req.method === 'GET') {
      cachedResponse = this.cache.get(req.url);
    }

    return cachedResponse ? of(cachedResponse) : this.sendRequest(req, next);
  }

  sendRequest(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const obs = next.handle(req);

    obs.subscribe(stuff => {
      if (event instanceof HttpResponse) {
        this.cache.set(req.url, event);
      }
    });

    return obs;
  }

}
