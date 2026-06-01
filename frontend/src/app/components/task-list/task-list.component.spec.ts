import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';

import { TaskListComponent } from './task-list.component';
import { TaskService } from '../../services/task.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';

describe('TaskListComponent', () => {
  let component: TaskListComponent;
  let fixture: ComponentFixture<TaskListComponent>;
  let taskService: jasmine.SpyObj<TaskService>;
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
    routeStub = { snapshot: { queryParamMap: convertToParamMap({}) } };

    await TestBed.configureTestingModule({
      declarations: [TaskListComponent],
      providers: [
        { provide: TaskService, useValue: taskService },
        { provide: ActivatedRoute, useValue: routeStub }
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
    expect(component.displayedTasks).toBe(component.allTasks);
    expect(component.loading).toBeFalse();
  });

  it('should load my tasks as displayed tasks when query param tab is mine', () => {
    routeStub.snapshot.queryParamMap = convertToParamMap({ tab: 'mine' });

    fixture.detectChanges();

    expect(component.currentTab).toBe('MINE');
    expect(component.displayedTasks).toBe(component.myTasks);
    expect(component.displayedTasks.map(t => t.title)).toEqual(['My task']);
  });

  it('should switch between All Tasks and My Tasks', () => {
    fixture.detectChanges();

    component.switchTab('MINE');

    expect(component.currentTab).toBe('MINE');
    expect(component.displayedTasks).toBe(component.myTasks);

    component.switchTab('ALL');

    expect(component.currentTab).toBe('ALL');
    expect(component.displayedTasks).toBe(component.allTasks);
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

  it('should delete task after confirmation and refresh the list', () => {
    fixture.detectChanges();
    spyOn(window, 'confirm').and.returnValue(true);
    taskService.findAll.calls.reset();
    taskService.findAssignedToMe.calls.reset();

    component.deleteTask(task({ id: 9 }));

    expect(taskService.delete).toHaveBeenCalledOnceWith(9);
    expect(taskService.findAll).toHaveBeenCalledTimes(1);
    expect(taskService.findAssignedToMe).toHaveBeenCalledTimes(1);
  });

  it('should not delete task when confirmation is cancelled', () => {
    fixture.detectChanges();
    spyOn(window, 'confirm').and.returnValue(false);

    component.deleteTask(task({ id: 9 }));

    expect(taskService.delete).not.toHaveBeenCalled();
  });

  it('should show readable error when deleting task fails', () => {
    fixture.detectChanges();
    spyOn(window, 'confirm').and.returnValue(true);
    taskService.delete.and.returnValue(throwError(() => ({ status: 500 })));

    component.deleteTask(task({ id: 9 }));

    expect(component.errorMessage).toBe('Could not delete the task.');
  });

  it('should show readable error when loading tasks fails', () => {
    taskService.findAll.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Could not load tasks. Please try again.');
    expect(component.loading).toBeFalse();
  });
});

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
