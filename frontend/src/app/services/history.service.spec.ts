import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HistoryService } from './history.service';
import { Globals } from '../global/globals';
import { HistoryDto } from '../dtos/history';

describe('HistoryService', () => {
  let service: HistoryService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  const history: HistoryDto = {
    tasks: [],
    payments: []
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HistoryService, Globals]
    });

    service = TestBed.inject(HistoryService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getHistory() should issue GET to /history without a household id', () => {
    service.getHistory().subscribe(result => {
      expect(result.tasks).toEqual([]);
      expect(result.payments).toEqual([]);
    });

    const req = httpMock.expectOne(globals.backendUri + '/history');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).not.toContain('householdId');
    req.flush(history);
  });

  it('getHistory() should surface backend errors', (done) => {
    service.getHistory().subscribe({
      next: () => done.fail('expected error'),
      error: err => {
        expect(err.status).toBe(403);
        done();
      }
    });

    httpMock.expectOne(globals.backendUri + '/history')
      .flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });
  });
});
