import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { TaskService } from './task.service';
import { Globals } from '../global/globals';
import { TaskCreateDto, TaskDetailDto, TaskStatus, TaskUpdateDto } from '../dtos/task';

describe('TaskService', () => {
  const backendUri = 'http://backend.test/api/v1';
  let service: TaskService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Globals, useValue: { backendUri } }
      ]
    });

    service = TestBed.inject(TaskService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should send GET to find all tasks', () => {
    service.findAll().subscribe(response => {
      expect(response).toEqual([task()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks`);
    expect(request.request.method).toBe('GET');
    request.flush([task()]);
  });

  it('should send GET to find assigned-to-me tasks', () => {
    service.findAssignedToMe().subscribe(response => {
      expect(response).toEqual([task()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks/my`);
    expect(request.request.method).toBe('GET');
    request.flush([task()]);
  });

  it('should send GET to fair balancing recommendation with stable context params', () => {
    service.getFairBalancingRecommendation({
      historyDays: 30,
      taskTitle: 'Clean kitchen',
      dueDate: '2026-06-05'
    }).subscribe(response => {
      expect(response.recommendedEmail).toBe('roommate@example.com');
    });

    const request = httpTestingController.expectOne(req =>
      req.url === `${backendUri}/tasks/fair-balancing/recommendation`
      && req.params.get('historyDays') === '30'
      && req.params.get('taskTitle') === 'Clean kitchen'
      && req.params.get('dueDate') === '2026-06-05'
    );
    expect(request.request.method).toBe('GET');
    request.flush({
      recommendedRoommateId: 1,
      recommendedUserId: 1,
      recommendedDisplayName: 'Anna Berger',
      recommendedEmail: 'roommate@example.com',
      historyDays: 30,
      algorithm: 'HISTORY_BUCKET_SCORE_SEEDED_TIE',
      reason: 'Lower scores mean less recent chore burden.',
      stats: []
    });
  });

  it('should send POST to fair balancing overdue reminder endpoint', () => {
    service.sendOverdueReminder(12).subscribe(response => {
      expect(response).toEqual({
        targetRoommateId: 12,
        targetDisplayName: 'Heavy Worker',
        overdueTasks: 5,
        message: 'Reminder sent to Heavy Worker.'
      });
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks/fair-balancing/overdue-reminders/12`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush({
      targetRoommateId: 12,
      targetDisplayName: 'Heavy Worker',
      overdueTasks: 5,
      message: 'Reminder sent to Heavy Worker.'
    });
  });

  it('should send POST to create task with body', () => {
    const body: TaskCreateDto = {
      title: 'Clean kitchen',
      description: 'Wipe counters',
      dueDate: '2026-06-05',
      assignedToEmail: 'roommate@example.com'
    };

    service.create(body).subscribe(response => {
      expect(response).toEqual(task());
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush(task());
  });

  it('should send GET to find one task', () => {
    service.findOne(7).subscribe(response => {
      expect(response).toEqual(task({ id: 7 }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks/7`);
    expect(request.request.method).toBe('GET');
    request.flush(task({ id: 7 }));
  });

  it('should send PUT to update task with body', () => {
    const body: TaskUpdateDto = {
      title: 'Updated task',
      description: 'Updated description',
      dueDate: '2026-06-06',
      assignedToEmail: 'roommate@example.com'
    };

    service.update(7, body).subscribe(response => {
      expect(response).toEqual(task({ id: 7, title: 'Updated task' }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks/7`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush(task({ id: 7, title: 'Updated task' }));
  });

  it('should send PATCH to update task status with body', () => {
    const body = { status: TaskStatus.DONE };

    service.updateStatus(7, body).subscribe(response => {
      expect(response).toEqual(task({ id: 7, status: TaskStatus.DONE }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks/7/status`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(body);
    request.flush(task({ id: 7, status: TaskStatus.DONE }));
  });

  it('should send DELETE to delete task', () => {
    service.delete(7).subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpTestingController.expectOne(`${backendUri}/tasks/7`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});

function task(overrides: Partial<TaskDetailDto> = {}): TaskDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Clean kitchen',
    description: 'Wipe counters',
    dueDate: '2026-06-05',
    status: TaskStatus.OPEN,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
