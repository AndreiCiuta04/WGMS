import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Globals } from '../global/globals';
import { PaymentCreateDto, PaymentDto, PaymentOverviewDto } from '../dtos/payment';
import { PaymentService } from './payment.service';

describe('PaymentService', () => {
  let service: PaymentService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  const overview: PaymentOverviewDto = {
    openBalances: [],
    payments: [],
    totalIOwe: 0,
    totalOwedToMe: 0
  };

  const payment: PaymentDto = {
    id: 1,
    fromRoommateId: 10,
    fromRoommateName: 'Max Mustermann',
    fromRoommateEmail: 'max@test.com',
    toRoommateId: 11,
    toRoommateName: 'Anna Musterfrau',
    toRoommateEmail: 'anna@test.com',
    amount: 12.5,
    date: '2026-05-31',
    createdAt: '2026-05-31T10:00:00'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PaymentService, Globals]
    });
    service = TestBed.inject(PaymentService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getOverview() should issue GET to /payments/overview without household id', () => {
    service.getOverview().subscribe(result => {
      expect(result.openBalances.length).toBe(0);
      expect(result.totalIOwe).toBe(0);
    });

    const req = httpMock.expectOne(globals.backendUri + '/payments/overview');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('householdId')).toBeFalse();
    expect(req.request.body).toBeNull();
    req.flush(overview);
  });

  it('create() should issue POST to /payments without household id', () => {
    const dto: PaymentCreateDto = {
      toRoommateId: 11,
      amount: 12.5,
      date: '2026-05-31'
    };

    service.create(dto).subscribe(result => expect(result.id).toBe(1));

    const req = httpMock.expectOne(globals.backendUri + '/payments');
    expect(req.request.method).toBe('POST');
    expect(req.request.params.has('householdId')).toBeFalse();
    expect(req.request.body).toEqual(dto);
    expect(req.request.body.householdId).toBeUndefined();
    req.flush(payment);
  });

  it('getOverview() should surface backend errors', (done) => {
    service.getOverview().subscribe({
      next: () => done.fail('expected error'),
      error: err => {
        expect(err.status).toBe(403);
        done();
      }
    });

    httpMock.expectOne(globals.backendUri + '/payments/overview')
      .flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
  });
});
