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

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let dashboardServiceSpy: jasmine.SpyObj<DashboardService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

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

    TestBed.configureTestingModule({
      declarations: [HomeComponent, ShoppingQuantityPipe],
      imports: [RouterTestingModule],
      providers: [
        { provide: DashboardService, useValue: dashboardServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: TaskService, useValue: {} },
        { provide: ShoppingItemService, useValue: {} }
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
});
