import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HistoryService } from './history.service';
import { Globals } from '../global/globals';
import { HistoryDto, StatisticsFilter } from '../dtos/history';

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

  it('statistics calls should include date and grouping filters', () => {
    const filter: StatisticsFilter = {
      from: '2026-01-01',
      to: '2026-01-31',
      grouping: 'DAY',
      modules: ['TASKS', 'EXPENSES']
    } as any;

    service.getTaskStatistics(filter).subscribe();
    let req = httpMock.expectOne(globals.backendUri + '/history/statistics/tasks?from=2026-01-01&to=2026-01-31&grouping=DAY');
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('modules')).toBeFalse();
    req.flush({} as any);

    service.getExpenseStatistics(filter).subscribe();
    req = httpMock.expectOne(globals.backendUri + '/history/statistics/expenses?from=2026-01-01&to=2026-01-31&grouping=DAY');
    expect(req.request.method).toBe('GET');
    req.flush({} as any);

    service.getShoppingStatistics(filter).subscribe();
    req = httpMock.expectOne(globals.backendUri + '/history/statistics/shopping?from=2026-01-01&to=2026-01-31&grouping=DAY');
    expect(req.request.method).toBe('GET');
    req.flush({} as any);
  });

  it('overview statistics should include selected modules when provided', () => {
    const filter: StatisticsFilter = {
      from: '2026-02-01',
      to: '2026-02-28',
      grouping: 'MONTH',
      modules: ['SHOPPING', 'TASKS']
    } as any;

    service.getStatisticsOverview(filter).subscribe();

    const req = httpMock.expectOne(request =>
      request.url === globals.backendUri + '/history/statistics/overview'
      && request.params.get('from') === '2026-02-01'
      && request.params.get('to') === '2026-02-28'
      && request.params.get('grouping') === 'MONTH'
      && request.params.getAll('modules')?.join(',') === 'SHOPPING,TASKS'
    );
    expect(req.request.method).toBe('GET');
    req.flush({} as any);
  });
});
