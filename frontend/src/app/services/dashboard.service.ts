import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { DashboardDto } from '../dtos/dashboard';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private baseUri: string = this.globals.backendUri + '/dashboard';

  constructor(private http: HttpClient, private globals: Globals) { }

  getDashboardData(): Observable<DashboardDto> {
    return this.http.get<DashboardDto>(this.baseUri);
  }
}
