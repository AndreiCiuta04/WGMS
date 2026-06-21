import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AggregatedShoppingItemDto,
  ShoppingItemCreateDto,
  ShoppingItemDto,
  ShoppingItemScanDto,
  ShoppingItemStatusUpdateDto,
  ShoppingItemSuggestionCreateDto,
  ShoppingItemSuggestionDto,
  ShoppingItemUpdateDto
} from '../dtos/shopping-item';

import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class ShoppingItemService {

  private baseUri: string = this.globals.backendUri + '/shopping-items';

  constructor(private http: HttpClient, private globals: Globals) { }

  create(item: ShoppingItemCreateDto): Observable<ShoppingItemDto> {
    return this.http.post<ShoppingItemDto>(this.baseUri, item);
  }

  /**
   * FR-18: creates a shopping item from a scanned QR payload. The raw decoded
   * string is sent untouched; the backend is the sole validator (contract 9.5).
   */
  scan(payload: string): Observable<ShoppingItemDto> {
    const body: ShoppingItemScanDto = { payload };
    return this.http.post<ShoppingItemDto>(`${this.baseUri}/scan`, body);
  }

  findAll(): Observable<ShoppingItemDto[]> {
    return this.http.get<ShoppingItemDto[]>(this.baseUri);
  }

  findTargetedAtMe(): Observable<ShoppingItemDto[]> {
    return this.http.get<ShoppingItemDto[]>(`${this.baseUri}/targeted-at-me`);
  }

  findAggregatedHouseholdShoppingList(): Observable<AggregatedShoppingItemDto[]> {
    return this.http.get<AggregatedShoppingItemDto[]>(`${this.baseUri}/aggregated`);
  }

  findHouseholdShoppingHistory(): Observable<ShoppingItemDto[]> {
    return this.http.get<ShoppingItemDto[]>(`${this.baseUri}/history`);
  }

  findOne(id: number): Observable<ShoppingItemDto> {
    return this.http.get<ShoppingItemDto>(`${this.baseUri}/${id}`);
  }

  update(id: number, item: ShoppingItemUpdateDto): Observable<ShoppingItemDto> {
    return this.http.put<ShoppingItemDto>(`${this.baseUri}/${id}`, item);
  }

  updateStatus(id: number, statusDto: ShoppingItemStatusUpdateDto): Observable<ShoppingItemDto> {
    return this.http.patch<ShoppingItemDto>(`${this.baseUri}/${id}/status`, statusDto);
  }

  createSuggestion(suggestion: ShoppingItemSuggestionCreateDto): Observable<ShoppingItemSuggestionDto> {
    return this.http.post<ShoppingItemSuggestionDto>(`${this.baseUri}/suggestions`, suggestion);
  }

  findSuggestions(): Observable<ShoppingItemSuggestionDto[]> {
    return this.http.get<ShoppingItemSuggestionDto[]>(`${this.baseUri}/suggestions`);
  }

  voteForSuggestion(id: number): Observable<ShoppingItemSuggestionDto> {
    return this.http.post<ShoppingItemSuggestionDto>(`${this.baseUri}/suggestions/${id}/votes`, null);
  }

  removeVoteFromSuggestion(id: number): Observable<ShoppingItemSuggestionDto> {
    return this.http.delete<ShoppingItemSuggestionDto>(`${this.baseUri}/suggestions/${id}/votes`);
  }

  promoteSuggestion(id: number): Observable<ShoppingItemDto> {
    return this.http.post<ShoppingItemDto>(
      `${this.baseUri}/suggestions/${id}/promote`,
      null
    );
  }

  deleteSuggestion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/suggestions/${id}`);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/${id}`);
  }
}
