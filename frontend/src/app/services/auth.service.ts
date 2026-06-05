import {Injectable} from '@angular/core';
import {AuthRequest} from '../dtos/auth-request';
import {Observable} from 'rxjs';
import { HttpClient } from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';
import {UserRegistration} from '../dtos/user-registration';
import {UserProfile} from '../dtos/user-profile';
import {UserProfileUpdate} from '../dtos/user-profile-update';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authBaseUri: string = this.globals.backendUri + '/authentication';

  constructor(private httpClient: HttpClient, private globals: Globals) {
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: AuthRequest): Observable<string> {
    return this.httpClient.post(this.authBaseUri, authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  /**
   * Register a new user.
   *
   * @param userRegistration User registration data.
   */
  registerUser(userRegistration: UserRegistration): Observable<string> {
    return this.httpClient.post(this.authBaseUri.replace('/authentication', '/users'), userRegistration, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => this.setToken(authResponse))
      );
  }

  /**
   * Load safe profile/account data for the currently authenticated user.
   */
  getCurrentUserProfile(): Observable<UserProfile> {
    return this.httpClient.get<UserProfile>(this.globals.backendUri + '/users/me');
  }

  /**
   * Correct editable profile fields for the currently authenticated user.
   */
  updateCurrentUserProfile(update: UserProfileUpdate): Observable<UserProfile> {
    return this.httpClient.patch<UserProfile>(this.globals.backendUri + '/users/me', update);
  }

  /**
   * Anonymize the currently authenticated account.
   */
  anonymizeCurrentUser(): Observable<void> {
    return this.httpClient.delete<void>(this.globals.backendUri + '/users/me');
  }

  /**
   * Leave the currently active household membership.
   */
  leaveCurrentHousehold(): Observable<UserProfile> {
    return this.httpClient.delete<UserProfile>(this.globals.backendUri + '/users/me/household');
  }


  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    const token = this.getToken();
    if (!token) {
      return false;
    }
    const expirationDate = this.getTokenExpirationDate(token);
    return !!expirationDate && (expirationDate.valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    const token = this.getToken();
    if (token != null) {
      const decoded: any = jwtDecode(token);
      const authInfo: string[] = decoded.rol;
      if (authInfo.includes('ROLE_ADMIN')) {
        return 'ADMIN';
      } else if (authInfo.includes('ROLE_USER')) {
        return 'USER';
      }
    }
    return 'UNDEFINED';
  }

  getUserEmail(): string | null {
    const token = this.getToken();
    if (token != null) {
      const decoded: any = jwtDecode(token);
      return decoded.sub;
    }
    return null;
  }

  private setToken(authResponse: string) {
    const token = authResponse.startsWith('Bearer ') ? authResponse.substring('Bearer '.length) : authResponse;
    localStorage.setItem('authToken', token);
  }

  private getTokenExpirationDate(token: string): Date | null {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

}
