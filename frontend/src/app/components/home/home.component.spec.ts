import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HomeComponent } from './home.component';
import { ShoppingQuantityPipe } from '../../pipes/shopping-quantity.pipe';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { DashboardService } from '../../services/dashboard.service';
import { DashboardDto } from '../../dtos/dashboard';
import { NotificationService } from '../../services/notification.service';
import { TaskStatus } from '../../dtos/task';
import { ShoppingItemStatus } from '../../dtos/shopping-item';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let dashboardServiceSpy: jasmine.SpyObj<DashboardService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let shoppingItemServiceSpy: jasmine.SpyObj<ShoppingItemService>;

  const dummyDashboardData: DashboardDto = {
    openTaskCount: 3,
    myTaskCount: 1,
    activeScheduleCount: 2,
    housemateCount: 4,
    myPendingTasks: [
      {
        id: 1,
        title: 'Chore A',
        description: 'Clean',
        dueDate: '2026-12-31',
        status: 'OPEN' as any,
        createdByEmail: 'a@test.com',
        assignedToEmail: 'me@test.com',
        createdAt: '2026-05-23T20:00:00Z',
        updatedAt: '2026-05-23T20:00:00Z',
        householdId: 10
      }
    ],
    householdSchedules: [],
    wgShoppingList: [],
    netBalance: 15.5,
    whoOwesMe: [],
    whoIowe: []
  };

  beforeEach(waitForAsync(() => {
    dashboardServiceSpy = jasmine.createSpyObj('DashboardService', ['getDashboardData']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getUserEmail', 'getCurrentUserProfile']);
    notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['getNotifications', 'markAsRead']);
    taskServiceSpy = jasmine.createSpyObj('TaskService', ['updateStatus']);
    shoppingItemServiceSpy = jasmine.createSpyObj('ShoppingItemService', ['updateStatus']);

    TestBed.configureTestingModule({
      declarations: [HomeComponent, ShoppingQuantityPipe],
      imports: [RouterTestingModule],
      providers: [
        { provide: DashboardService, useValue: dashboardServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: ShoppingItemService, useValue: shoppingItemServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.getUserEmail.and.returnValue('me@test.com');
    authServiceSpy.getCurrentUserProfile.and.returnValue(of(null as any));
    dashboardServiceSpy.getDashboardData.and.returnValue(of(dummyDashboardData));
    notificationServiceSpy.getNotifications.and.returnValue(of([]));
    taskServiceSpy.updateStatus.and.returnValue(of({} as any));
    shoppingItemServiceSpy.updateStatus.and.returnValue(of({} as any));

    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should call DashboardService on init when logged in', () => {
    fixture.detectChanges();
    expect(dashboardServiceSpy.getDashboardData).toHaveBeenCalled();
    expect(component.myTaskCount).toBe(1);
    expect(component.openTaskCount).toBe(3);
    expect(component.netBalance).toBe(15.5);
    expect(component.dashboardLoading).toBeFalse();
  });

  it('should show loading state and then handle error', () => {
    dashboardServiceSpy.getDashboardData.and.returnValue(throwError(() => new Error('API Error')));
    fixture.detectChanges();
    expect(component.dashboardLoading).toBeFalse();
    expect(component.myPendingTasks.length).toBe(0);
  });

  it('should render the overdue warning banner when a task is past its due date', () => {
    const pastTask = {
      id: 2,
      title: 'Overdue Task',
      description: 'Clean oven',
      dueDate: '2020-01-01',
      status: 'OPEN' as any,
      createdByEmail: 'a@test.com',
      assignedToEmail: 'me@test.com',
      createdAt: '2020-01-01T20:00:00Z',
      updatedAt: '2020-01-01T20:00:00Z',
      householdId: 10
    };
    const overdueData = {
      ...dummyDashboardData,
      myPendingTasks: [pastTask]
    };
    dashboardServiceSpy.getDashboardData.and.returnValue(of(overdueData));

    fixture.detectChanges();

    const bannerElement = fixture.nativeElement.querySelector('.warning-banner');
    expect(bannerElement).toBeTruthy();
    expect(bannerElement.textContent).toContain('Task "Overdue Task" is overdue');
  });

  it('should not render the overdue warning banner when all tasks are in the future', () => {
    fixture.detectChanges();
    const bannerElement = fixture.nativeElement.querySelector('.warning-banner');
    expect(bannerElement).toBeNull();
  });

  it('should render recent reminders if any unread are present', () => {
    const dummyReminders = [
      { id: 101, message: 'Clean tasks overdue', type: 'TASK_OVERDUE' as any, isRead: false, createdAt: '2026-06-07T12:00:00' }
    ];
    notificationServiceSpy.getNotifications.and.returnValue(of(dummyReminders));
    fixture.detectChanges();

    const remindersCard = fixture.nativeElement.querySelector('.reminders-card');
    expect(remindersCard).toBeTruthy();
    expect(remindersCard.textContent).toContain('Clean tasks overdue');
  });

  it('should mark reminder as read when button is clicked', () => {
    const dummyReminders = [
      { id: 101, message: 'Clean tasks overdue', type: 'TASK_OVERDUE' as any, isRead: false, createdAt: '2026-06-07T12:00:00' }
    ];
    notificationServiceSpy.getNotifications.and.returnValue(of(dummyReminders));
    notificationServiceSpy.markAsRead.and.returnValue(of(undefined));
    fixture.detectChanges();

    const markBtn = fixture.nativeElement.querySelector('.reminders-card button');
    expect(markBtn).toBeTruthy();
    markBtn.click();

    expect(notificationServiceSpy.markAsRead).toHaveBeenCalledWith(101);
  });

  it('should use profile first name, email fallback, and generic fallback for greeting', () => {
    component.profile = { firstName: 'anna' } as any;
    expect(component.getUserFirstName()).toBe('Anna');

    component.profile = null;
    authServiceSpy.getUserEmail.and.returnValue('brian@example.com');
    expect(component.getUserFirstName()).toBe('Brian');

    authServiceSpy.getUserEmail.and.returnValue(null);
    expect(component.getUserFirstName()).toBe('there');
  });

  it('should not load dashboard when logged out', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);
    fixture.detectChanges();
    expect(dashboardServiceSpy.getDashboardData).not.toHaveBeenCalled();
  });

  it('should keep profile null when profile loading fails', () => {
    authServiceSpy.getCurrentUserProfile.and.returnValue(throwError(() => new Error('profile failed')));
    fixture.detectChanges();
    expect(component.profile).toBeNull();
  });

  it('should set no-household state on dashboard 403', () => {
    dashboardServiceSpy.getDashboardData.and.returnValue(throwError(() => ({ status: 403 })));
    fixture.detectChanges();
    expect(component.dashboardLoading).toBeFalse();
    expect(component.hasNoHousehold).toBeTrue();
  });

  it('should fall back to empty dashboard lists and mapped balance summaries', () => {
    dashboardServiceSpy.getDashboardData.and.returnValue(of({
      openTaskCount: 1,
      myTaskCount: 2,
      activeScheduleCount: 3,
      housemateCount: 4,
      netBalance: 0,
      whoOwesMe: [{ fromRoommateName: 'Mia', amount: 12.5 } as any],
      whoIowe: [{ toRoommateName: 'Noah', amount: 7.25 } as any]
    } as any));

    fixture.detectChanges();

    expect(component.myPendingTasks).toEqual([]);
    expect(component.householdSchedules).toEqual([]);
    expect(component.wgShoppingList).toEqual([]);
    expect(component.whoOwesMe).toEqual([{ name: 'Mia', amount: 12.5 }]);
    expect(component.whoIOwe).toEqual([{ name: 'Noah', amount: 7.25 }]);
  });

  it('should tolerate notification loading and mark-read failures', () => {
    spyOn(console, 'error');
    notificationServiceSpy.getNotifications.and.returnValue(throwError(() => ({ status: 500 })));
    fixture.detectChanges();
    expect(console.error).toHaveBeenCalledWith('Error fetching dashboard notifications:', { status: 500 });

    notificationServiceSpy.markAsRead.and.returnValue(throwError(() => ({ status: 500 })));
    component.unreadReminders = [{ id: 7, message: 'Reminder', type: 'TASK_ASSIGNED' as any, isRead: false, createdAt: '2026-06-07T12:00:00' }];
    component.markReminderAsRead(7);
    expect(component.unreadReminders.length).toBe(1);
  });

  it('should complete tasks and handle task completion errors', () => {
    component.myPendingTasks = [
      { id: 1, title: 'A' } as any,
      { id: 2, title: 'B' } as any
    ];
    component.myTaskCount = 2;
    component.openTaskCount = 1;

    component.completeTask(1);

    expect(taskServiceSpy.updateStatus).toHaveBeenCalledWith(1, { status: TaskStatus.DONE });
    expect(component.myPendingTasks.map(t => t.id)).toEqual([2]);
    expect(component.myTaskCount).toBe(1);
    expect(component.openTaskCount).toBe(0);

    spyOn(console, 'error');
    taskServiceSpy.updateStatus.and.returnValue(throwError(() => ({ status: 500 })));
    component.completeTask(2);
    expect(console.error).toHaveBeenCalledWith('Error completing task:', { status: 500 });
  });

  it('should mark shopping items bought and handle shopping errors', () => {
    component.wgShoppingList = [
      { id: 1, name: 'Milk' } as any,
      { id: 2, name: 'Bread' } as any
    ];

    component.buyShoppingItem(1);

    expect(shoppingItemServiceSpy.updateStatus).toHaveBeenCalledWith(1, { status: ShoppingItemStatus.BOUGHT });
    expect(component.wgShoppingList.map(item => item.id)).toEqual([2]);

    spyOn(console, 'error');
    shoppingItemServiceSpy.updateStatus.and.returnValue(throwError(() => ({ status: 500 })));
    component.buyShoppingItem(2);
    expect(console.error).toHaveBeenCalledWith('Error buying shopping item:', { status: 500 });
  });

  it('should format reminder metadata, dates, overdue state, and frequencies', () => {
    expect(component.getReminderIcon('TASK_ASSIGNED')).toBe('bi bi-person-check-fill');
    expect(component.getReminderIcon('TASK_OVERDUE')).toBe('bi bi-exclamation-triangle-fill');
    expect(component.getReminderIcon('OVERDUE_TASK_REMINDER')).toBe('bi bi-exclamation-triangle-fill');
    expect(component.getReminderIcon('EXPENSE_CREATED')).toBe('bi bi-receipt-cutoff');
    expect(component.getReminderIcon('PAYMENT_RECEIVED')).toBe('bi bi-cash-coin');
    expect(component.getReminderIcon('CLEANING_REMINDER')).toBe('bi bi-stars');
    expect(component.getReminderIcon('UNKNOWN')).toBe('bi bi-bell-fill');

    expect(component.getReminderClass('TASK_ASSIGNED')).toContain('primary');
    expect(component.getReminderClass('TASK_OVERDUE')).toContain('danger');
    expect(component.getReminderClass('OVERDUE_TASK_REMINDER')).toContain('danger');
    expect(component.getReminderClass('EXPENSE_CREATED')).toContain('warning');
    expect(component.getReminderClass('PAYMENT_RECEIVED')).toContain('success');
    expect(component.getReminderClass('CLEANING_REMINDER')).toContain('info');
    expect(component.getReminderClass('UNKNOWN')).toContain('secondary');

    const now = new Date();
    expect(component.formatRelativeTime('')).toBe('');
    expect(component.formatRelativeTime(now.toISOString())).toBe('Just now');
    expect(component.formatRelativeTime(new Date(now.getTime() - 15 * 60000).toISOString())).toBe('15m ago');
    expect(component.formatRelativeTime(new Date(now.getTime() - 3 * 3600000).toISOString())).toBe('3h ago');
    expect(component.formatRelativeTime(new Date(now.getTime() - 2 * 24 * 3600000).toISOString())).toBe('2d ago');

    expect(component.isOverdue('')).toBeFalse();
    expect(component.isOverdue('2020-01-01')).toBeTrue();
    expect(component.isOverdue('2999-01-01')).toBeFalse();

    expect(component.formatFrequency('DAILY')).toBe('Daily');
    expect(component.formatFrequency('WEEKLY')).toBe('Weekly');
    expect(component.formatFrequency('BIWEEKLY')).toBe('Biweekly');
    expect(component.formatFrequency('MONTHLY')).toBe('Monthly');
    expect(component.formatFrequency('YEARLY')).toBe('YEARLY');
  });
});
