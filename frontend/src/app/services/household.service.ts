import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Household, HouseholdCreateRequest } from '../dtos/household';
import { Roommate } from '../dtos/roommate';
import { Globals } from '../global/globals';

@Injectable({ providedIn: 'root' })
export class HouseholdService {
  private baseUri: string = this.globals.backendUri + '/households';

  constructor(private http: HttpClient, private globals: Globals) {}

  createHousehold(request: HouseholdCreateRequest): Observable<Household> {
    return this.http.post<Household>(this.baseUri, request);
  }

  getMyHousehold(): Observable<Household> {
    return this.http.get<Household>(this.baseUri + '/me');
  }

  getMembers(householdId: number): Observable<Roommate[]> {
    return this.http.get<Roommate[]>(`${this.baseUri}/${householdId}/members`);
  }
}
