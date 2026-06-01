import { ComponentFixture, fakeAsync, TestBed, tick, waitForAsync } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

import { HomeComponent } from './home.component';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';
import { CleaningFrequency, CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';
import { Household } from '../../dtos/household';
import { Roommate } from '../../dtos/roommate';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let taskService: jasmine.SpyObj<TaskService>;
  let cleaningScheduleService: jasmine.SpyObj<CleaningScheduleService>;
  let householdService: jasmine.SpyObj<HouseholdService>;

  beforeEach(waitForAsync(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['isLoggedIn', 'getUserEmail']);
    taskService = jasmine.createSpyObj<TaskService>('TaskService', ['findAll', 'findAssignedToMe']);
    cleaningScheduleService = jasmine.createSpyObj<CleaningScheduleService>('CleaningScheduleService', ['findActive']);
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold', 'getMembers']);

    authService.isLoggedIn.and.returnValue(false);
    authService.getUserEmail.and.returnValue('main@test.com');
    taskService.findAll.and.returnValue(of([]));
    taskService.findAssignedToMe.and.returnValue(of([]));
    cleaningScheduleService.findActive.and.returnValue(of([]));
    householdService.getMyHousehold.and.returnValue(of(household()));
    householdService.getMembers.and.returnValue(of([]));

    TestBed.configureTestingModule({
      declarations: [HomeComponent],
      imports: [RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: TaskService, useValue: taskService },
        { provide: CleaningScheduleService, useValue: cleaningScheduleService },
        { provide: HouseholdService, useValue: householdService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should not load dashboard data when logged out', () => {
    const loadDashboardSpy = spyOn<any>(component, 'loadDashboard').and.callThrough();

    fixture.detectChanges();

    expect(loadDashboardSpy).not.toHaveBeenCalled();
    expect(taskService.findAll).not.toHaveBeenCalled();
    expect(taskService.findAssignedToMe).not.toHaveBeenCalled();
    expect(cleaningScheduleService.findActive).not.toHaveBeenCalled();
    expect(householdService.getMyHousehold).not.toHaveBeenCalled();
    expect(householdService.getMembers).not.toHaveBeenCalled();
  });

  it('should calculate dashboard state from service data when logged in', fakeAsync(() => {
    authService.isLoggedIn.and.returnValue(true);
    taskService.findAll.and.returnValue(of([
      task({ id: 1, status: TaskStatus.OPEN }),
      task({ id: 2, status: TaskStatus.IN_PROGRESS }),
      task({ id: 3, status: TaskStatus.DONE }),
      task({ id: 4, status: TaskStatus.CANCELLED })
    ]));
    taskService.findAssignedToMe.and.returnValue(of([
      task({ id: 10, title: 'Today 1', dueDate: today(), status: TaskStatus.OPEN }),
      task({ id: 11, title: 'Today 2', dueDate: today(), status: TaskStatus.IN_PROGRESS }),
      task({ id: 12, title: 'Today 3', dueDate: today(), status: TaskStatus.OPEN }),
      task({ id: 13, title: 'Today 4', dueDate: today(), status: TaskStatus.OPEN }),
      task({ id: 14, title: 'Today 5', dueDate: today(), status: TaskStatus.OPEN }),
      task({ id: 15, title: 'Today 6', dueDate: today(), status: TaskStatus.OPEN }),
      task({ id: 16, title: 'Tomorrow', dueDate: '2099-01-01', status: TaskStatus.OPEN }),
      task({ id: 17, title: 'Done today', dueDate: today(), status: TaskStatus.DONE }),
      task({ id: 18, title: 'Cancelled today', dueDate: today(), status: TaskStatus.CANCELLED })
    ]));
    cleaningScheduleService.findActive.and.returnValue(of([
      schedule({ id: 1, nextDueDate: '2026-06-05' }),
      schedule({ id: 2, nextDueDate: '2026-06-02' }),
      schedule({ id: 3, nextDueDate: '2026-06-04' }),
      schedule({ id: 4, nextDueDate: '2026-06-01' }),
      schedule({ id: 5, nextDueDate: '2026-06-03' }),
      schedule({ id: 6, nextDueDate: '2026-06-06' })
    ]));
    householdService.getMembers.and.returnValue(of([
      member({ id: 1 }),
      member({ id: 2 }),
      member({ id: 3 })
    ]));

    fixture.detectChanges();
    tick();

    expect(component.openTaskCount).toBe(2);
    expect(component.myTaskCount).toBe(7);
    expect(component.todayTasks.map(t => t.id)).toEqual([10, 11, 12, 13, 14]);
    expect(component.activeScheduleCount).toBe(6);
    expect(component.upcomingSchedules.map(s => s.id)).toEqual([4, 2, 5, 3, 1]);
    expect(component.housemateCount).toBe(3);
    expect(component.dashboardLoading).toBeFalse();
  }));

  it('should finish loading and not crash when dashboard service calls fail', fakeAsync(() => {
    authService.isLoggedIn.and.returnValue(true);
    taskService.findAll.and.returnValue(throwError(() => new Error('tasks failed')));
    taskService.findAssignedToMe.and.returnValue(throwError(() => new Error('my tasks failed')));
    cleaningScheduleService.findActive.and.returnValue(of([]));
    householdService.getMyHousehold.and.returnValue(throwError(() => new Error('household failed')));

    expect(() => fixture.detectChanges()).not.toThrow();
    tick();

    expect(component.dashboardLoading).toBeFalse();
    expect(component.openTaskCount).toBe(0);
    expect(component.myTaskCount).toBe(0);
    expect(component.todayTasks).toEqual([]);
    expect(component.upcomingSchedules).toEqual([]);
  }));

  it('should show a visible dashboard loading message while logged-in data is loading', () => {
    authService.isLoggedIn.and.returnValue(true);
    const allTasks$ = new Subject<TaskDetailDto[]>();
    const myTasks$ = new Subject<TaskDetailDto[]>();
    const schedules$ = new Subject<CleaningScheduleDetailDto[]>();
    const household$ = new Subject<Household>();

    taskService.findAll.and.returnValue(allTasks$.asObservable());
    taskService.findAssignedToMe.and.returnValue(myTasks$.asObservable());
    cleaningScheduleService.findActive.and.returnValue(schedules$.asObservable());
    householdService.getMyHousehold.and.returnValue(household$.asObservable());

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Loading dashboard...');
  });
});

function today(): string {
  const date = new Date();
  return date.getFullYear() + '-' +
    String(date.getMonth() + 1).padStart(2, '0') + '-' +
    String(date.getDate()).padStart(2, '0');
}

function task(overrides: Partial<TaskDetailDto> = {}): TaskDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Task',
    description: 'Description',
    dueDate: today(),
    status: TaskStatus.OPEN,
    createdByEmail: 'main@test.com',
    assignedToEmail: 'roommate@test.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function schedule(overrides: Partial<CleaningScheduleDetailDto> = {}): CleaningScheduleDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Schedule',
    description: 'Description',
    frequency: CleaningFrequency.WEEKLY,
    startDate: '2026-06-01',
    nextDueDate: '2026-06-01',
    active: true,
    createdByEmail: 'main@test.com',
    assignedToEmail: 'roommate@test.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function household(overrides: Partial<Household> = {}): Household {
  return {
    id: 1,
    name: 'Integration WG',
    address: 'Test address',
    createdAt: '2026-06-01T00:00:00',
    currentUserRole: 'MAIN_TENANT',
    ...overrides
  };
}

function member(overrides: Partial<Roommate> = {}): Roommate {
  return {
    id: 1,
    userId: 1,
    firstName: 'User',
    lastName: 'Test',
    email: 'user@test.com',
    role: 'ROOMMATE',
    joinedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
