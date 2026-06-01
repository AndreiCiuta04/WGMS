import { TestBed } from '@angular/core/testing';
import { DashboardService } from './dashboard.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Globals } from '../global/globals';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [
        DashboardService,
        Globals,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET /dashboard and return data', () => {
    const dummyData = {
      openTaskCount: 2,
      myTaskCount: 1,
      activeScheduleCount: 0,
      housemateCount: 1,
      myPendingTasks: [],
      householdSchedules: [],
      wgShoppingList: [],
      netBalance: 0,
      whoOwesMe: [],
      whoIowe: []
    };

    service.getDashboardData().subscribe(data => {
      expect(data).toEqual(dummyData);
      expect(data.openTaskCount).toBe(2);
    });

    const req = httpMock.expectOne(`${globals.backendUri}/dashboard`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyData);
  });
});
