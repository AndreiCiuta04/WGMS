import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ShoppingItemCreateDto,
  ShoppingItemDto,
  ShoppingItemStatusUpdateDto,
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

  findAll(): Observable<ShoppingItemDto[]> {
    return this.http.get<ShoppingItemDto[]>(this.baseUri);
  }

  findTargetedAtMe(): Observable<ShoppingItemDto[]> {
    return this.http.get<ShoppingItemDto[]>(`${this.baseUri}/targeted-at-me`);
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

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/${id}`);
  }
}
