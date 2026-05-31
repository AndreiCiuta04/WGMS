import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { PaymentCreateDto, PaymentDto, PaymentOverviewDto } from '../dtos/payment';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private baseUri: string = this.globals.backendUri + '/payments';

  constructor(private http: HttpClient, private globals: Globals) { }

  getOverview(): Observable<PaymentOverviewDto> {
    return this.http.get<PaymentOverviewDto>(`${this.baseUri}/overview`);
  }

  create(payment: PaymentCreateDto): Observable<PaymentDto> {
    return this.http.post<PaymentDto>(this.baseUri, payment);
  }
}
