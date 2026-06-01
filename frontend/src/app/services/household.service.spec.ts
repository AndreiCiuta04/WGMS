import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { HouseholdService } from './household.service';
import { Globals } from '../global/globals';
import { Household } from '../dtos/household';
import { Roommate } from '../dtos/roommate';

describe('HouseholdService', () => {
  const backendUri = 'http://backend.test/api/v1';
  let service: HouseholdService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Globals, useValue: { backendUri } }
      ]
    });

    service = TestBed.inject(HouseholdService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should send POST to create household with request body', () => {
    const requestBody = { name: 'Dream Loft Vienna', address: 'Main street 1' };

    service.createHousehold(requestBody).subscribe(response => {
      expect(response).toEqual(household());
    });

    const request = httpTestingController.expectOne(`${backendUri}/households`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(requestBody);
    request.flush(household());
  });

  it('should send GET to current household URL', () => {
    service.getMyHousehold().subscribe(response => {
      expect(response).toEqual(household());
    });

    const request = httpTestingController.expectOne(`${backendUri}/households/me`);
    expect(request.request.method).toBe('GET');
    request.flush(household());
  });

  it('should send GET to household members URL', () => {
    service.getMembers(7).subscribe(response => {
      expect(response).toEqual([member()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/households/7/members`);
    expect(request.request.method).toBe('GET');
    request.flush([member()]);
  });
});

function household(overrides: Partial<Household> = {}): Household {
  return {
    id: 1,
    name: 'Dream Loft Vienna',
    address: 'Main street 1',
    createdAt: '2026-06-01T00:00:00',
    currentUserRole: 'MAIN_TENANT',
    ...overrides
  };
}

function member(overrides: Partial<Roommate> = {}): Roommate {
  return {
    id: 1,
    userId: 1,
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'friend@example.com',
    role: 'ROOMMATE',
    joinedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
