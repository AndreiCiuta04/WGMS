import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import {
  BalanceDto,
  BillExpensePreviewDto,
  BillExpensePreviewRequestDto,
  EqualSplitRequestDto,
  ExpenseCreateDto,
  ExpenseDto,
  ExpenseSplitCreateDto,
  ExpenseUpdateDto
} from '../dtos/expense';

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {

  private baseUri: string = this.globals.backendUri + '/expenses';

  constructor(private http: HttpClient, private globals: Globals) { }

  create(expense: ExpenseCreateDto): Observable<ExpenseDto> {
    return this.http.post<ExpenseDto>(this.baseUri, expense);
  }

  findAll(): Observable<ExpenseDto[]> {
    return this.http.get<ExpenseDto[]>(this.baseUri);
  }

  findOne(id: number): Observable<ExpenseDto> {
    return this.http.get<ExpenseDto>(`${this.baseUri}/${id}`);
  }

  update(id: number, expense: ExpenseUpdateDto): Observable<ExpenseDto> {
    return this.http.put<ExpenseDto>(`${this.baseUri}/${id}`, expense);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/${id}`);
  }

  calculateEqualSplits(request: EqualSplitRequestDto): Observable<ExpenseSplitCreateDto[]> {
    return this.http.post<ExpenseSplitCreateDto[]>(`${this.baseUri}/equal-split`, request);
  }

  previewBillExpense(request: BillExpensePreviewRequestDto): Observable<BillExpensePreviewDto> {
    return this.http.post<BillExpensePreviewDto>(`${this.baseUri}/bill-preview`, request);
  }

  calculateBalances(): Observable<BalanceDto[]> {
    return this.http.get<BalanceDto[]>(`${this.baseUri}/balances`);
  }
}
