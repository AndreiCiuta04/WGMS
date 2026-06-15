import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { CleaningScheduleService } from './cleaning-schedule.service';
import { Globals } from '../global/globals';
import {
  CleaningFrequency,
  CleaningScheduleCreateDto,
  CleaningScheduleDetailDto,
  CleaningScheduleUpdateDto
} from '../dtos/cleaning-schedule';
import { TaskDetailDto, TaskStatus } from '../dtos/task';

describe('CleaningScheduleService', () => {
  const backendUri = 'http://backend.test/api/v1';
  let service: CleaningScheduleService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Globals, useValue: { backendUri } }
      ]
    });

    service = TestBed.inject(CleaningScheduleService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should send GET to find all cleaning schedules', () => {
    service.findAll().subscribe(response => {
      expect(response).toEqual([schedule()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules`);
    expect(request.request.method).toBe('GET');
    request.flush([schedule()]);
  });

  it('should send GET to find active cleaning schedules', () => {
    service.findActive().subscribe(response => {
      expect(response).toEqual([schedule()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules/active`);
    expect(request.request.method).toBe('GET');
    request.flush([schedule()]);
  });

  it('should send GET to find one cleaning schedule', () => {
    service.findOne(7).subscribe(response => {
      expect(response).toEqual(schedule({ id: 7 }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules/7`);
    expect(request.request.method).toBe('GET');
    request.flush(schedule({ id: 7 }));
  });

  it('should send POST to create cleaning schedule with body', () => {
    const body: CleaningScheduleCreateDto = {
      title: 'Kitchen rotation',
      description: 'Clean kitchen',
      frequency: CleaningFrequency.WEEKLY,
      startDate: '2026-06-05',
      endDate: '2026-07-05',
      assignedToEmail: 'roommate@example.com',
      fairBalancingEnabled: false
    };

    service.create(body).subscribe(response => {
      expect(response).toEqual(schedule());
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush(schedule());
  });

  it('should send PUT to update cleaning schedule with body', () => {
    const body: CleaningScheduleUpdateDto = {
      title: 'Updated rotation',
      description: 'Updated cleaning',
      frequency: CleaningFrequency.MONTHLY,
      startDate: '2026-06-06',
      endDate: '2026-08-06',
      assignedToEmail: 'roommate@example.com',
      fairBalancingEnabled: true
    };

    service.update(7, body).subscribe(response => {
      expect(response).toEqual(schedule({ id: 7, title: 'Updated rotation' }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules/7`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush(schedule({ id: 7, title: 'Updated rotation' }));
  });

  it('should send PATCH to update active flag with body', () => {
    const body = { active: false };

    service.updateActive(7, body).subscribe(response => {
      expect(response).toEqual(schedule({ id: 7, active: false }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules/7/active`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(body);
    request.flush(schedule({ id: 7, active: false }));
  });

  it('should send POST to generate task with empty body', () => {
    service.generateTask(7).subscribe(response => {
      expect(response).toEqual(task());
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules/7/generate-task`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush(task());
  });

  it('should send DELETE to delete cleaning schedule', () => {
    service.delete(7).subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpTestingController.expectOne(`${backendUri}/cleaning-schedules/7`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});

function schedule(overrides: Partial<CleaningScheduleDetailDto> = {}): CleaningScheduleDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Kitchen rotation',
    description: 'Clean kitchen',
    frequency: CleaningFrequency.WEEKLY,
    startDate: '2026-06-05',
    endDate: '2026-07-05',
    nextDueDate: '2026-06-05',
    active: true,
    fairBalancingEnabled: false,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function task(overrides: Partial<TaskDetailDto> = {}): TaskDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Generated cleaning task',
    description: 'Clean kitchen',
    dueDate: '2026-06-05',
    status: TaskStatus.OPEN,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
