import {Injectable} from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import {AuthService} from '../services/auth.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private globals: Globals) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authUri = this.globals.backendUri + '/authentication';
    const registerUri = this.globals.backendUri + '/users';

    // Do not intercept authentication and registration requests
    if (req.url === authUri || req.url === registerUri) {
      return next.handle(req);
    }

    const token = this.authService.getToken();
    if (token) {
      const authReq = req.clone({
        headers: req.headers.set('Authorization', 'Bearer ' + token)
      });
      return next.handle(authReq);
    }

    return next.handle(req);
  }
}
