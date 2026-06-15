import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';

import { CleaningScheduleListComponent } from './cleaning-schedule-list.component';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { CleaningFrequency, CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';
import { Household } from '../../dtos/household';
import { Roommate } from '../../dtos/roommate';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';

describe('CleaningScheduleListComponent', () => {
  let component: CleaningScheduleListComponent;
  let fixture: ComponentFixture<CleaningScheduleListComponent>;
  let cleaningScheduleService: jasmine.SpyObj<CleaningScheduleService>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let authService: jasmine.SpyObj<AuthService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;

  beforeEach(async () => {
    cleaningScheduleService = jasmine.createSpyObj<CleaningScheduleService>('CleaningScheduleService', [
      'findAll',
      'findActive',
      'updateActive',
      'generateTask',
      'delete'
    ]);
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold', 'getMembers']);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['isLoggedIn']);

    cleaningScheduleService.findAll.and.returnValue(of(schedules()));
    cleaningScheduleService.findActive.and.returnValue(of([]));
    cleaningScheduleService.updateActive.and.returnValue(of(schedule()));
    cleaningScheduleService.generateTask.and.returnValue(of(task()));
    cleaningScheduleService.delete.and.returnValue(of(void 0));
    householdService.getMyHousehold.and.returnValue(of(household()));
    householdService.getMembers.and.returnValue(of([roommate()]));
    authService.isLoggedIn.and.returnValue(true);
    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      declarations: [CleaningScheduleListComponent],
      providers: [
        { provide: CleaningScheduleService, useValue: cleaningScheduleService },
        { provide: HouseholdService, useValue: householdService },
        { provide: AuthService, useValue: authService },
        { provide: ConfirmDialogService, useValue: confirmDialog }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CleaningScheduleListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load cleaning schedules and roommates on init', () => {
    fixture.detectChanges();

    expect(cleaningScheduleService.findAll).toHaveBeenCalled();
    expect(householdService.getMyHousehold).toHaveBeenCalled();
    expect(householdService.getMembers).toHaveBeenCalledOnceWith(1);
    expect(component.allSchedules.length).toBe(4);
    expect(component.roommates).toEqual([roommate()]);
    expect(component.roommateMap.get('roommate@example.com')).toBe('Anna');
  });

  it('should store inactive schedules only in allSchedules and categorize active schedules', () => {
    fixture.detectChanges();

    expect(component.allSchedules.some(s => !s.active)).toBeTrue();
    expect(component.dueNowSchedules.map(s => s.title)).toEqual(['Overdue bathroom', 'Due kitchen']);
    expect(component.upcomingSchedules.map(s => s.title)).toEqual(['Upcoming hallway']);
    expect(component.dueNowSchedules.every(s => s.active)).toBeTrue();
    expect(component.upcomingSchedules.every(s => s.active)).toBeTrue();
  });

  it('should not call findActive because the component loads all schedules', () => {
    fixture.detectChanges();

    expect(cleaningScheduleService.findActive).not.toHaveBeenCalled();
  });

  it('should pause active schedule by calling updateActive with false and refresh', () => {
    fixture.detectChanges();
    cleaningScheduleService.findAll.calls.reset();

    component.toggleActive(schedule({ id: 7, active: true }));

    expect(cleaningScheduleService.updateActive).toHaveBeenCalledOnceWith(7, { active: false });
    expect(cleaningScheduleService.findAll).toHaveBeenCalledTimes(1);
  });

  it('should resume inactive schedule by calling updateActive with true and refresh', () => {
    fixture.detectChanges();
    cleaningScheduleService.findAll.calls.reset();

    component.toggleActive(schedule({ id: 7, active: false }));

    expect(cleaningScheduleService.updateActive).toHaveBeenCalledOnceWith(7, { active: true });
    expect(cleaningScheduleService.findAll).toHaveBeenCalledTimes(1);
  });

  it('should show readable error when active update fails', () => {
    fixture.detectChanges();
    cleaningScheduleService.updateActive.and.returnValue(throwError(() => ({ status: 403 })));

    component.toggleActive(schedule({ title: 'Kitchen rotation' }));

    expect(component.errorMessage).toBe('You do not have permission to perform this action.');
  });

  it('should generate task, show success, refresh, and clear success later', fakeAsync(() => {
    fixture.detectChanges();
    cleaningScheduleService.findAll.calls.reset();

    component.generateTask(schedule({ id: 5, title: 'Kitchen rotation' }));

    expect(cleaningScheduleService.generateTask).toHaveBeenCalledOnceWith(5);
    expect(component.successMessage).toBe('Task created for "Kitchen rotation"!');
    expect(component.errorMessage).toBe('');
    expect(cleaningScheduleService.findAll).toHaveBeenCalledTimes(1);

    tick(4000);

    expect(component.successMessage).toBe('');
  }));

  it('should show readable error when generating task fails', () => {
    fixture.detectChanges();
    cleaningScheduleService.generateTask.and.returnValue(throwError(() => ({ error: { message: 'Task already exists.' } })));

    component.generateTask(schedule({ title: 'Kitchen rotation' }));

    expect(component.successMessage).toBe('');
    expect(component.errorMessage).toBe('Task already exists.');
  });

  it('should delete schedule after confirmation and refresh', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    cleaningScheduleService.findAll.calls.reset();

    component.deleteSchedule(schedule({ id: 8, title: 'Kitchen rotation' }));
    tick();

    expect(cleaningScheduleService.delete).toHaveBeenCalledOnceWith(8);
    expect(cleaningScheduleService.findAll).toHaveBeenCalledTimes(1);
  }));

  it('should not delete schedule when confirmation is cancelled', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(false));

    component.deleteSchedule(schedule({ id: 8, title: 'Kitchen rotation' }));
    tick();

    expect(cleaningScheduleService.delete).not.toHaveBeenCalled();
  }));

  it('should show conflict-specific fallback when delete fails with 409', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    cleaningScheduleService.delete.and.returnValue(throwError(() => ({ status: 409 })));

    component.deleteSchedule(schedule({ title: 'Kitchen rotation' }));
    tick();

    expect(component.errorMessage).toBe(
      'Cannot delete "Kitchen rotation" because it already generated tasks. Pause it instead if you do not want it to create more tasks.'
    );
  }));

  it('should show readable error when loading schedules fails', () => {
    cleaningScheduleService.findAll.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Could not load cleaning schedules. Please try again.');
  });

  it('should map assignee email to first name and fall back to email prefix', () => {
    fixture.detectChanges();

    expect(component.getAssigneeName('roommate@example.com')).toBe('Anna');
    expect(component.getAssigneeName('unknown@example.com')).toBe('unknown');
  });

  it('should toggle management section expansion', () => {
    fixture.detectChanges();

    component.toggleManageExpanded();

    expect(component.manageExpanded).toBeTrue();

    component.toggleManageExpanded();

    expect(component.manageExpanded).toBeFalse();
  });
});

function schedules(): CleaningScheduleDetailDto[] {
  return [
    schedule({ id: 1, title: 'Overdue bathroom', nextDueDate: '2000-01-01', active: true }),
    schedule({ id: 2, title: 'Due kitchen', nextDueDate: today(), active: true }),
    schedule({ id: 3, title: 'Upcoming hallway', nextDueDate: '2999-01-01', active: true }),
    schedule({ id: 4, title: 'Paused laundry', nextDueDate: today(), active: false })
  ];
}

function today(): string {
  const date = new Date();
  return date.getFullYear() + '-' +
    String(date.getMonth() + 1).padStart(2, '0') + '-' +
    String(date.getDate()).padStart(2, '0');
}

function schedule(overrides: Partial<CleaningScheduleDetailDto> = {}): CleaningScheduleDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Kitchen rotation',
    description: 'Clean kitchen surfaces',
    frequency: CleaningFrequency.WEEKLY,
    startDate: '2026-06-05',
    endDate: '2026-07-05',
    nextDueDate: today(),
    active: true,
    fairBalancingEnabled: false,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

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

function roommate(overrides: Partial<Roommate> = {}): Roommate {
  return {
    id: 1,
    userId: 1,
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'roommate@example.com',
    role: 'ROOMMATE',
    joinedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function task(overrides: Partial<TaskDetailDto> = {}): TaskDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Generated cleaning task',
    description: 'Clean kitchen surfaces',
    dueDate: today(),
    status: TaskStatus.OPEN,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
