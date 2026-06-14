import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NotificationDto, NotificationSettingDto, UserVerifyDto } from '../dtos/notification';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private baseUri: string = this.globals.backendUri + '/notifications';

  constructor(private http: HttpClient, private globals: Globals) { }

  getNotifications(): Observable<NotificationDto[]> {
    return this.http.get<NotificationDto[]>(this.baseUri);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUri}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.post<void>(`${this.baseUri}/read-all`, {});
  }

  getSettings(): Observable<NotificationSettingDto> {
    return this.http.get<NotificationSettingDto>(`${this.baseUri}/settings`);
  }

  updateSettings(settings: NotificationSettingDto): Observable<NotificationSettingDto> {
    return this.http.put<NotificationSettingDto>(`${this.baseUri}/settings`, settings);
  }

  verifyUser(verifyDto: UserVerifyDto): Observable<string> {
    const url = this.globals.backendUri + '/users/verify';
    return this.http.post(url, verifyDto, { responseType: 'text' })
      .pipe(
        tap((authResponse: string) => {
          const token = authResponse.startsWith('Bearer ') ? authResponse.substring('Bearer '.length) : authResponse;
          localStorage.setItem('authToken', token);
        })
      );
  }
}
