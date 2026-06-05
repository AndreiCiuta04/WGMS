import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { AuthRequest } from '../dtos/auth-request';
import { UserRegistration } from '../dtos/user-registration';
import { Globals } from '../global/globals';

describe('AuthService', () => {
  const backendUri = 'http://backend.test/api/v1';
  let service: AuthService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Globals, useValue: { backendUri } }
      ]
    });

    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should post login request as text and store token without Bearer prefix', () => {
    const authRequest = new AuthRequest('anna@example.com', 'Password123!');

    service.loginUser(authRequest).subscribe(response => {
      expect(response).toBe('Bearer login-token');
    });

    const request = httpTestingController.expectOne(`${backendUri}/authentication`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(authRequest);
    expect(request.request.responseType).toBe('text');

    request.flush('Bearer login-token');

    expect(localStorage.getItem('authToken')).toBe('login-token');
  });

  it('should post registration request and store token without Bearer prefix', () => {
    const userRegistration: UserRegistration = {
      email: 'anna@example.com',
      password: 'Password123!',
      firstName: 'Anna',
      lastName: 'Berger'
    };

    service.registerUser(userRegistration).subscribe(response => {
      expect(response).toBe('Bearer register-token');
    });

    const request = httpTestingController.expectOne(`${backendUri}/users`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(userRegistration);
    expect(request.request.responseType).toBe('text');

    request.flush('Bearer register-token');

    expect(localStorage.getItem('authToken')).toBe('register-token');
  });

  it('should get the current user profile', () => {
    const profile = {
      id: 5,
      email: 'anna@example.com',
      firstName: 'Anna',
      lastName: 'Berger',
      role: 'USER',
      householdId: 7,
      householdName: 'WG Test',
      roommateId: 9
    };

    service.getCurrentUserProfile().subscribe(response => {
      expect(response).toEqual(profile);
    });

    const request = httpTestingController.expectOne(`${backendUri}/users/me`);
    expect(request.request.method).toBe('GET');

    request.flush(profile);
  });

  it('should patch the current user profile', () => {
    const update = { email: 'anna.updated@example.com', firstName: 'Anna', lastName: 'Updated' };
    const profile = {
      id: 5,
      email: 'anna@example.com',
      firstName: 'Anna',
      lastName: 'Updated',
      role: 'USER',
      householdId: null,
      householdName: null,
      roommateId: null
    };

    service.updateCurrentUserProfile(update).subscribe(response => {
      expect(response).toEqual(profile);
    });

    const request = httpTestingController.expectOne(`${backendUri}/users/me`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(update);

    request.flush(profile);
  });

  it('should delete the current household membership', () => {
    const profile = {
      id: 5,
      email: 'anna@example.com',
      firstName: 'Anna',
      lastName: 'Berger',
      role: 'USER',
      householdId: null,
      householdName: null,
      roommateId: null
    };

    service.leaveCurrentHousehold().subscribe(response => {
      expect(response).toEqual(profile);
    });

    const request = httpTestingController.expectOne(`${backendUri}/users/me/household`);
    expect(request.request.method).toBe('DELETE');

    request.flush(profile);
  });

  it('should delete the current user profile for anonymization', () => {
    service.anonymizeCurrentUser().subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpTestingController.expectOne(`${backendUri}/users/me`);
    expect(request.request.method).toBe('DELETE');

    request.flush(null);
  });

  it('should remove auth token on logout', () => {
    localStorage.setItem('authToken', 'stored-token');

    service.logoutUser();

    expect(localStorage.getItem('authToken')).toBeNull();
  });

  it('should return stored token', () => {
    localStorage.setItem('authToken', 'stored-token');

    expect(service.getToken()).toBe('stored-token');
  });

  it('should return false from isLoggedIn when no token exists', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should return true from isLoggedIn for a non-expired JWT', () => {
    localStorage.setItem('authToken', jwt({ exp: futureTimestamp(), sub: 'anna@example.com', rol: ['ROLE_USER'] }));

    expect(service.isLoggedIn()).toBeTrue();
  });

  it('should return false from isLoggedIn for an expired JWT', () => {
    localStorage.setItem('authToken', jwt({ exp: pastTimestamp(), sub: 'anna@example.com', rol: ['ROLE_USER'] }));

    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should decode and return the JWT subject as user email', () => {
    localStorage.setItem('authToken', jwt({ exp: futureTimestamp(), sub: 'anna@example.com', rol: ['ROLE_USER'] }));

    expect(service.getUserEmail()).toBe('anna@example.com');
  });

  it('should return USER role when JWT contains ROLE_USER', () => {
    localStorage.setItem('authToken', jwt({ exp: futureTimestamp(), sub: 'anna@example.com', rol: ['ROLE_USER'] }));

    expect(service.getUserRole()).toBe('USER');
  });

  it('should return ADMIN role when JWT contains ROLE_ADMIN', () => {
    localStorage.setItem('authToken', jwt({ exp: futureTimestamp(), sub: 'admin@example.com', rol: ['ROLE_ADMIN', 'ROLE_USER'] }));

    expect(service.getUserRole()).toBe('ADMIN');
  });

  it('should return UNDEFINED role when no token exists', () => {
    expect(service.getUserRole()).toBe('UNDEFINED');
  });
});

function jwt(payload: Record<string, unknown>): string {
  return [
    base64UrlEncode({ alg: 'none', typ: 'JWT' }),
    base64UrlEncode(payload),
    'signature'
  ].join('.');
}

function base64UrlEncode(value: Record<string, unknown>): string {
  return btoa(JSON.stringify(value))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

function futureTimestamp(): number {
  return Math.floor(Date.now() / 1000) + 3600;
}

function pastTimestamp(): number {
  return Math.floor(Date.now() / 1000) - 3600;
}
