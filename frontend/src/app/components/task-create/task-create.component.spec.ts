import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';

import { TaskCreateComponent } from './task-create.component';
import { TaskService } from '../../services/task.service';
import { HouseholdService } from '../../services/household.service';
import { Household } from '../../dtos/household';
import { Roommate } from '../../dtos/roommate';
import { FairChoreRecommendationDto, TaskDetailDto, TaskStatus } from '../../dtos/task';

describe('TaskCreateComponent', () => {
  let component: TaskCreateComponent;
  let fixture: ComponentFixture<TaskCreateComponent>;
  let taskService: jasmine.SpyObj<TaskService>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    taskService = jasmine.createSpyObj<TaskService>('TaskService', [
      'create',
      'getFairBalancingRecommendation',
      'sendOverdueReminder'
    ]);
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold', 'getMembers']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    householdService.getMyHousehold.and.returnValue(of(household()));
    householdService.getMembers.and.returnValue(of([roommate()]));
    taskService.create.and.returnValue(of(task()));
    taskService.getFairBalancingRecommendation.and.returnValue(of(recommendation()));
    taskService.sendOverdueReminder.and.returnValue(of({
      targetRoommateId: 2,
      targetDisplayName: 'Suggested Roommate',
      overdueTasks: 5,
      message: 'Reminder sent to Suggested Roommate.'
    }));

    await TestBed.configureTestingModule({
      declarations: [TaskCreateComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: TaskService, useValue: taskService },
        { provide: HouseholdService, useValue: householdService },
        { provide: Router, useValue: router }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskCreateComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load household members as assignee options on init', () => {
    fixture.detectChanges();

    expect(householdService.getMyHousehold).toHaveBeenCalled();
    expect(householdService.getMembers).toHaveBeenCalledOnceWith(1);
    expect(component.roommates).toEqual([roommate()]);
  });

  it('should display fair chore suggestion on init', () => {
    fixture.detectChanges();

    expect(taskService.getFairBalancingRecommendation).toHaveBeenCalled();
    expect(component.recommendation?.recommendedEmail).toBe('suggested@example.com');
  });

  it('should render fair chore suggestion content and stats', () => {
    fixture.detectChanges();

    const card = (fixture.nativeElement as HTMLElement).querySelector('[data-cy="fair-chore-suggestion"]');
    const recommendationText = (fixture.nativeElement as HTMLElement).querySelector('[data-cy="fair-chore-recommendation"]');
    const reason = (fixture.nativeElement as HTMLElement).querySelector('[data-cy="fair-chore-reason"]');
    const stats = (fixture.nativeElement as HTMLElement).querySelector('[data-cy="fair-chore-stats"]');

    expect(card?.textContent).toContain('Fair chore suggestion');
    expect(recommendationText?.textContent).toContain('Suggested Roommate');
    expect(recommendationText?.textContent).toContain('suggested@example.com');
    expect(reason?.textContent).toContain('Open and overdue tasks are included');
    expect(stats?.textContent).toContain('Suggested Roommate');
    expect(stats?.textContent).toContain('Suggested');
    expect(stats?.textContent).not.toContain('NO_OVERDUE_AVAILABLE');
    expect(stats?.textContent).not.toContain('Bucket');
  });

  it('should handle fair chore suggestion loading errors gracefully', () => {
    taskService.getFairBalancingRecommendation.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.recommendationError).toBe('Could not load a fair chore suggestion.');
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-cy="fair-chore-suggestion-error"]')?.textContent)
      .toContain('Could not load a fair chore suggestion.');
  });

  it('should set loading state while fair chore suggestion is pending', () => {
    const recommendationSubject = new Subject<FairChoreRecommendationDto>();
    taskService.getFairBalancingRecommendation.and.returnValue(recommendationSubject.asObservable());

    component.loadFairBalancingRecommendation();

    expect(component.recommendationLoading).toBeTrue();

    recommendationSubject.next(recommendation());
    recommendationSubject.complete();

    expect(component.recommendationLoading).toBeFalse();
  });

  it('should show empty state and disable use suggestion when no recommendation is returned', () => {
    taskService.getFairBalancingRecommendation.and.returnValue(of(undefined as unknown as FairChoreRecommendationDto));

    fixture.detectChanges();

    const emptyState = (fixture.nativeElement as HTMLElement).querySelector('[data-cy="fair-chore-suggestion-empty"]');
    const useButton = (fixture.nativeElement as HTMLElement)
      .querySelector('[data-cy="fair-chore-use-suggestion"]') as HTMLButtonElement | null;

    expect(emptyState?.textContent).toContain('No suggestion available.');
    expect(useButton?.disabled).toBeTrue();
  });

  it('should fill assignee when suggestion is used and still allow manual override', () => {
    fixture.detectChanges();

    component.useSuggestion();
    expect(component.taskForm.get('assignedToEmail')?.value).toBe('suggested@example.com');

    component.taskForm.patchValue({ assignedToEmail: 'roommate@example.com' });
    expect(component.taskForm.get('assignedToEmail')?.value).toBe('roommate@example.com');
  });

  it('should show overdue reminder action to main tenant for roommate with at least five overdue tasks', () => {
    taskService.getFairBalancingRecommendation.and.returnValue(of(recommendation({
      stats: [roommateStat({ overdueTasks: 5 })]
    })));

    fixture.detectChanges();

    const reminderButton = (fixture.nativeElement as HTMLElement)
      .querySelector('[data-cy="send-overdue-reminder"]') as HTMLButtonElement | null;

    expect(component.isMainTenant).toBeTrue();
    expect(reminderButton).not.toBeNull();
    expect(reminderButton?.textContent).toContain('Send reminder');
  });

  it('should not show overdue reminder action when overdue task count is below threshold', () => {
    taskService.getFairBalancingRecommendation.and.returnValue(of(recommendation({
      stats: [roommateStat({ overdueTasks: 4 })]
    })));

    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[data-cy="send-overdue-reminder"]')).toBeNull();
  });

  it('should not show overdue reminder action to normal roommates', () => {
    householdService.getMyHousehold.and.returnValue(of(household({ currentUserRole: 'ROOMMATE' })));
    taskService.getFairBalancingRecommendation.and.returnValue(of(recommendation({
      stats: [roommateStat({ overdueTasks: 5 })]
    })));

    fixture.detectChanges();

    expect(component.isMainTenant).toBeFalse();
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-cy="send-overdue-reminder"]')).toBeNull();
  });

  it('should call backend and show success when overdue reminder is sent', () => {
    taskService.getFairBalancingRecommendation.and.returnValue(of(recommendation({
      stats: [roommateStat({ overdueTasks: 5 })]
    })));

    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('[data-cy="send-overdue-reminder"]')
      ?.click();
    fixture.detectChanges();

    expect(taskService.sendOverdueReminder).toHaveBeenCalledOnceWith(2);
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-cy="overdue-reminder-success"]')?.textContent)
      .toContain('Reminder sent to Suggested Roommate.');
    expect(component.wasReminderSent(2)).toBeTrue();
  });

  it('should show clear error when overdue reminder is rejected', () => {
    taskService.getFairBalancingRecommendation.and.returnValue(of(recommendation({
      stats: [roommateStat({ overdueTasks: 5 })]
    })));
    taskService.sendOverdueReminder.and.returnValue(throwError(() => ({ status: 400 })));

    fixture.detectChanges();
    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('[data-cy="send-overdue-reminder"]')
      ?.click();
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('[data-cy="overdue-reminder-error"]')?.textContent)
      .toContain('no longer has enough overdue tasks');
    expect(component.wasReminderSent(2)).toBeFalse();
  });

  it('should block service call when title is empty', () => {
    fixture.detectChanges();
    component.taskForm.patchValue(validTaskForm({ title: '' }));

    component.onSubmit();

    expect(component.submitted).toBeTrue();
    expect(component.taskForm.get('title')?.errors?.required).toBeTruthy();
    expect(component.taskForm.get('title')?.touched).toBeTrue();
    expect(taskService.create).not.toHaveBeenCalled();
  });

  it('should require assignee and due date', () => {
    fixture.detectChanges();
    component.taskForm.patchValue(validTaskForm({ assignedToEmail: '', dueDate: '' }));

    component.onSubmit();

    expect(component.taskForm.get('assignedToEmail')?.errors?.required).toBeTruthy();
    expect(component.taskForm.get('dueDate')?.errors?.required).toBeTruthy();
    expect(taskService.create).not.toHaveBeenCalled();
  });

  it('should call TaskService with title, description, dueDate, and assignedToEmail for valid form', () => {
    fixture.detectChanges();
    component.taskForm.patchValue(validTaskForm());

    component.onSubmit();

    expect(taskService.create).toHaveBeenCalledOnceWith({
      title: 'Clean kitchen',
      description: 'Wipe counters and floor',
      dueDate: '2026-06-05',
      assignedToEmail: 'roommate@example.com'
    });
  });

  it('should navigate to task list after successful create', () => {
    fixture.detectChanges();
    component.taskForm.patchValue(validTaskForm());

    component.onSubmit();

    expect(router.navigate).toHaveBeenCalledOnceWith(['/tasks']);
  });

  it('should show readable error when household cannot be loaded', () => {
    householdService.getMyHousehold.and.returnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Could not load your household.');
    expect(householdService.getMembers).not.toHaveBeenCalled();
  });

  it('should show no-household error when current user has no household', () => {
    householdService.getMyHousehold.and.returnValue(of(household({ id: 0 })));

    fixture.detectChanges();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('You need to create or join a household before creating tasks.');
    expect(householdService.getMembers).not.toHaveBeenCalled();
  });

  it('should show readable error when member loading fails', () => {
    householdService.getMembers.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Could not load household members.');
  });

  it('should show backend validation message when task creation fails', () => {
    fixture.detectChanges();
    taskService.create.and.returnValue(
      throwError(() => ({ error: { 'Validation errors': ['title must not be blank'] } }))
    );
    component.taskForm.patchValue(validTaskForm());

    component.onSubmit();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('must not be blank');
  });
});

function validTaskForm(overrides: Partial<{
  title: string;
  description: string;
  dueDate: string;
  assignedToEmail: string;
}> = {}) {
  return {
    title: 'Clean kitchen',
    description: 'Wipe counters and floor',
    dueDate: '2026-06-05',
    assignedToEmail: 'roommate@example.com',
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
    title: 'Clean kitchen',
    description: 'Wipe counters and floor',
    dueDate: '2026-06-05',
    status: TaskStatus.OPEN,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function recommendation(overrides: Partial<FairChoreRecommendationDto> = {}): FairChoreRecommendationDto {
  return {
    recommendedRoommateId: 2,
    recommendedUserId: 2,
    recommendedDisplayName: 'Suggested Roommate',
    recommendedEmail: 'suggested@example.com',
    historyDays: 30,
    algorithm: 'HISTORY_BUCKET_SCORE_SEEDED_TIE',
    reason: 'Lower scores mean less recent chore burden.',
    stats: [roommateStat()],
    ...overrides
  };
}

function roommateStat(overrides: Partial<FairChoreRecommendationDto['stats'][number]> = {}): FairChoreRecommendationDto['stats'][number] {
  return {
    roommateId: 2,
    userId: 2,
    displayName: 'Suggested Roommate',
    email: 'suggested@example.com',
    completedTasks: 0,
    openTasks: 0,
    overdueTasks: 0,
    fairnessBucket: 'NO_OVERDUE_AVAILABLE',
    fairnessScore: 0,
    candidate: true,
    recommended: true,
    ...overrides
  };
}
