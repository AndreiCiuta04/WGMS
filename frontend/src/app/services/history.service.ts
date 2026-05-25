import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { HistoryDto } from '../dtos/history';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {

  private baseUri: string = this.globals.backendUri + '/history';

  constructor(private http: HttpClient, private globals: Globals) { }

  getHistory(): Observable<HistoryDto> {
    return this.http.get<HistoryDto>(this.baseUri);
  }
}
