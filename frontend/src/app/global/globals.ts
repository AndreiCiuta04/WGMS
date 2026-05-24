import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Globals {
  readonly backendUri: string = this.findBackendUrl();

  private findBackendUrl(): string {
    const isLocalAngularDevServer =
      (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
      && window.location.port.startsWith('42');

    if (isLocalAngularDevServer) { // local `ng serve`, backend at localhost:8080
      return 'http://localhost:8080/api/v1';
    } else {
      // assume deployed somewhere and backend is available at same host/port as frontend
      return window.location.protocol + '//' + window.location.host + window.location.pathname + 'api/v1';
    }
  }
}


