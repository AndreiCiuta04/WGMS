import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { TaskListComponent } from './task-list.component';
import { TaskService } from '../../services/task.service';
import { HouseholdService } from '../../services/household.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;
  let taskService: jasmine.SpyObj<TaskService>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;
  let routeStub: { snapshot: { queryParamMap: ReturnType<typeof convertToParamMap> } };

  beforeEach(async () => {
    taskService = jasmine.createSpyObj<TaskService>('TaskService', [
      'findAll',
      'findAssignedToMe',
      'updateStatus',
      'delete'
    ]);
    taskService.findAll.and.returnValue(of([task({ id: 1, title: 'All task' })]));
    taskService.findAssignedToMe.and.returnValue(of([task({ id: 2, title: 'My task', assignedToEmail: 'me@example.com' })]));
    taskService.updateStatus.and.returnValue(of(task({ id: 1, status: TaskStatus.DONE })));
    taskService.delete.and.returnValue(of(void 0));
    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold', 'getMembers']);
    householdService.getMyHousehold.and.returnValue(of({
      id: 1, name: 'WG', createdAt: '2026-01-01T00:00:00', currentUserRole: 'MAIN_TENANT' as const
    }));
    householdService.getMembers.and.returnValue(of([
      { id: 1, userId: 1, firstName: 'Anna', lastName: 'Berger', email: 'roommate@example.com', role: 'ROOMMATE' as const, joinedAt: '2026-01-01' },
      { id: 2, userId: 2, firstName: 'Main', lastName: 'Tenant', email: 'main@example.com', role: 'MAIN_TENANT' as const, joinedAt: '2026-01-01' }
    ]));
    routeStub = { snapshot: { queryParamMap: convertToParamMap({}) } };

    await TestBed.configureTestingModule({
      declarations: [TaskListComponent],
      providers: [
        { provide: TaskService, useValue: taskService },
        { provide: HouseholdService, useValue: householdService },
        { provide: ActivatedRoute, useValue: routeStub },
        { provide: ConfirmDialogService, useValue: confirmDialog }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load all and assigned tasks on init', () => {
    fixture.detectChanges();

    expect(taskService.findAll).toHaveBeenCalled();
    expect(taskService.findAssignedToMe).toHaveBeenCalled();
    expect(component.allTasks.map(t => t.title)).toEqual(['All task']);
    expect(component.myTasks.map(t => t.title)).toEqual(['My task']);
    expect(component.currentTab).toBe('ALL');
    expect(component.displayedTasks).toEqual(component.allTasks);
    expect(component.loading).toBeFalse();
  });

  it('should load my tasks as displayed tasks when query param tab is mine', () => {
    routeStub.snapshot.queryParamMap = convertToParamMap({ tab: 'mine' });

    fixture.detectChanges();

    expect(component.currentTab).toBe('MINE');
    expect(component.displayedTasks).toEqual(component.myTasks);
    expect(component.displayedTasks.map(t => t.title)).toEqual(['My task']);
  });

  it('should switch between All Tasks and My Tasks', () => {
    fixture.detectChanges();

    component.switchTab('MINE');

    expect(component.currentTab).toBe('MINE');
    expect(component.displayedTasks).toEqual(component.myTasks);

    component.switchTab('ALL');

    expect(component.currentTab).toBe('ALL');
    expect(component.displayedTasks).toEqual(component.allTasks);
  });

  it('should mark task as done and refresh the list', () => {
    fixture.detectChanges();
    taskService.findAll.calls.reset();
    taskService.findAssignedToMe.calls.reset();

    component.completeTask(task({ id: 9 }));

    expect(taskService.updateStatus).toHaveBeenCalledOnceWith(9, { status: TaskStatus.DONE });
    expect(taskService.findAll).toHaveBeenCalledTimes(1);
    expect(taskService.findAssignedToMe).toHaveBeenCalledTimes(1);
  });

  it('should show readable error when marking task as done fails', () => {
    fixture.detectChanges();
    taskService.updateStatus.and.returnValue(throwError(() => ({ status: 500 })));

    component.completeTask(task({ id: 9 }));

    expect(component.errorMessage).toBe('Could not mark the task as done.');
  });

  it('should delete task after confirmation and refresh the list', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    taskService.findAll.calls.reset();
    taskService.findAssignedToMe.calls.reset();

    component.deleteTask(task({ id: 9 }));
    tick();

    expect(taskService.delete).toHaveBeenCalledOnceWith(9);
    expect(taskService.findAll).toHaveBeenCalledTimes(1);
    expect(taskService.findAssignedToMe).toHaveBeenCalledTimes(1);
  }));

  it('should not delete task when confirmation is cancelled', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(false));

    component.deleteTask(task({ id: 9 }));
    tick();

    expect(taskService.delete).not.toHaveBeenCalled();
  }));

  it('should show readable error when deleting task fails', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));
    taskService.delete.and.returnValue(throwError(() => ({ status: 500 })));

    component.deleteTask(task({ id: 9 }));
    tick();

    expect(component.errorMessage).toBe('Could not delete the task.');
  }));

  it('should show readable error when loading tasks fails', () => {
    taskService.findAll.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Could not load tasks. Please try again.');
    expect(component.loading).toBeFalse();
  });

  it('filters by search term across title and description', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, title: 'Clean kitchen', description: 'scrub the sink' }),
      task({ id: 2, title: 'Buy milk', description: 'oat milk' }),
      task({ id: 3, title: 'Vacuum', description: 'living room' })
    ]));
    fixture.detectChanges();

    component.searchTerm = 'milk';
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([2]);

    component.searchTerm = 'living';
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([3]);
  });

  it('filters by status', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, status: TaskStatus.OPEN }),
      task({ id: 2, status: TaskStatus.DONE }),
      task({ id: 3, status: TaskStatus.IN_PROGRESS })
    ]));
    fixture.detectChanges();

    component.statusFilter = TaskStatus.DONE;
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([2]);
  });

  it('filters by assignee', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, assignedToEmail: 'a@example.com' }),
      task({ id: 2, assignedToEmail: 'b@example.com' })
    ]));
    fixture.detectChanges();

    component.assigneeFilter = 'b@example.com';
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([2]);
  });

  it('filters by due-date buckets', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, dueDate: isoOffset(-1), status: TaskStatus.OPEN }), // overdue
      task({ id: 2, dueDate: isoOffset(0) }),                           // today + week
      task({ id: 3, dueDate: isoOffset(3) }),                          // week
      task({ id: 4, dueDate: isoOffset(40) })                          // none of the buckets
    ]));
    fixture.detectChanges();

    component.dueFilter = 'OVERDUE';
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([1]);

    component.dueFilter = 'TODAY';
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([2]);

    component.dueFilter = 'WEEK';
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([2, 3]);
  });

  it('combines filters with AND and reports the active count', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, status: TaskStatus.OPEN, assignedToEmail: 'a@example.com' }),
      task({ id: 2, status: TaskStatus.OPEN, assignedToEmail: 'b@example.com' }),
      task({ id: 3, status: TaskStatus.DONE, assignedToEmail: 'a@example.com' })
    ]));
    fixture.detectChanges();

    component.statusFilter = TaskStatus.OPEN;
    component.assigneeFilter = 'a@example.com';
    component.applyFilters();

    expect(component.displayedTasks.map(t => t.id)).toEqual([1]);
    expect(component.activeFilterCount).toBe(2);
    expect(component.hasActiveFilters).toBeTrue();
  });

  it('clearFilters restores the full list', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, status: TaskStatus.OPEN }),
      task({ id: 2, status: TaskStatus.DONE })
    ]));
    fixture.detectChanges();

    component.searchTerm = 'zzz';
    component.statusFilter = TaskStatus.DONE;
    component.applyFilters();
    expect(component.displayedTasks.length).toBe(0);

    component.clearFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([1, 2]);
    expect(component.activeFilterCount).toBe(0);
    expect(component.hasActiveFilters).toBeFalse();
  });

  it('reapplies active filters when switching tabs', () => {
    taskService.findAll.and.returnValue(of([
      task({ id: 1, status: TaskStatus.OPEN }),
      task({ id: 2, status: TaskStatus.DONE })
    ]));
    taskService.findAssignedToMe.and.returnValue(of([
      task({ id: 3, status: TaskStatus.OPEN }),
      task({ id: 4, status: TaskStatus.DONE })
    ]));
    fixture.detectChanges();

    component.statusFilter = TaskStatus.OPEN;
    component.applyFilters();
    expect(component.displayedTasks.map(t => t.id)).toEqual([1]);

    component.switchTab('MINE');
    expect(component.displayedTasks.map(t => t.id)).toEqual([3]);
  });

  it('builds assignee options from household members', () => {
    fixture.detectChanges();

    expect(component.assigneeOptions).toEqual([
      { email: 'roommate@example.com', name: 'Anna Berger' },
      { email: 'main@example.com', name: 'Main Tenant' }
    ]);
  });
});

function isoOffset(days: number): string {
  const d = new Date();
  d.setDate(d.getDate() + days);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function task(overrides: Partial<TaskDetailDto> = {}): TaskDetailDto {
  return {
    id: 1,
    householdId: 1,
    title: 'Task',
    description: 'Description',
    dueDate: '2026-06-05',
    status: TaskStatus.OPEN,
    createdByEmail: 'main@example.com',
    assignedToEmail: 'roommate@example.com',
    createdAt: '2026-06-01T00:00:00',
    updatedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
