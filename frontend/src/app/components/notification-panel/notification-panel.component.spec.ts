import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NotificationPanelComponent } from './notification-panel.component';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { of } from 'rxjs';
import { NotificationType, NotificationDto } from '../../dtos/notification';

const sampleNotifications: NotificationDto[] = [
  { id: 1, message: 'Task 1 Assigned', type: NotificationType.TASK_ASSIGNED, isRead: false, createdAt: '2026-06-08T12:00:00', referenceId: 10 },
  { id: 2, message: 'Expense 1 Created', type: NotificationType.EXPENSE_CREATED, isRead: true, createdAt: '2026-06-08T10:00:00', referenceId: 20 }
];

function configureNotificationPanel(notifications: NotificationDto[]) {
  const mockNotificationService = jasmine.createSpyObj('NotificationService', ['getNotifications', 'markAsRead', 'markAllAsRead']);
  const mockAuthService = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
  mockNotificationService.getNotifications.and.returnValue(of(notifications));
  mockAuthService.isLoggedIn.and.returnValue(true);

  TestBed.configureTestingModule({
    imports: [RouterTestingModule.withRoutes([])],
    declarations: [NotificationPanelComponent],
    providers: [
      { provide: NotificationService, useValue: mockNotificationService },
      { provide: AuthService, useValue: mockAuthService }
    ]
  });

  return { mockNotificationService, mockAuthService };
}

describe('NotificationPanelComponent', () => {
  let component: NotificationPanelComponent;
  let fixture: ComponentFixture<NotificationPanelComponent>;
  let mockNotificationService: jasmine.SpyObj<NotificationService>;
  let router: Router;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    const mocks = configureNotificationPanel(sampleNotifications.map(n => ({ ...n })));
    mockNotificationService = mocks.mockNotificationService;
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(NotificationPanelComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load notifications if logged in', () => {
    expect(mockNotificationService.getNotifications).toHaveBeenCalled();
    expect(component.notifications.length).toBe(2);
    expect(component.unreadCount).toBe(1);
  });

  it('should toggle dropdown', () => {
    const event = new Event('click');
    expect(component.isOpen).toBeFalse();
    component.toggleDropdown(event);
    expect(component.isOpen).toBeTrue();
  });

  it('should mark notification as read', () => {
    mockNotificationService.markAsRead.and.returnValue(of(undefined));
    const event = new Event('click');
    const unreadNotif = component.notifications.find(n => !n.isRead)!;
    component.markAsRead(unreadNotif, event);
    expect(mockNotificationService.markAsRead).toHaveBeenCalledWith(unreadNotif.id);
    expect(unreadNotif.isRead).toBeTrue();
    expect(component.unreadCount).toBe(0);
  });

  it('should mark all as read', () => {
    mockNotificationService.markAllAsRead.and.returnValue(of(undefined));
    const event = new Event('click');
    component.markAllAsRead(event);
    expect(mockNotificationService.markAllAsRead).toHaveBeenCalled();
    expect(component.notifications.every(n => n.isRead)).toBeTrue();
    expect(component.unreadCount).toBe(0);
  });

  it('should navigate to related page when opening a notification', () => {
    mockNotificationService.markAsRead.and.returnValue(of(undefined));
    const navigateSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    const unreadNotif = component.notifications.find(n => !n.isRead)!;

    component.openNotification(unreadNotif, new Event('click'));

    expect(mockNotificationService.markAsRead).toHaveBeenCalledWith(unreadNotif.id);
    expect(navigateSpy).toHaveBeenCalledWith(['/tasks']);
    expect(component.isOpen).toBeFalse();
  });

  it('should resolve routes for supported notification types', () => {
    expect(component.getNotificationRoute({
      id: 1, message: 'x', type: NotificationType.TASK_OVERDUE, isRead: false, createdAt: ''
    })).toEqual(['/tasks']);
    expect(component.getNotificationRoute({
      id: 2, message: 'x', type: NotificationType.EXPENSE_CREATED, isRead: false, createdAt: ''
    })).toEqual(['/expenses']);
    expect(component.getNotificationRoute({
      id: 3, message: 'x', type: NotificationType.PAYMENT_RECEIVED, isRead: false, createdAt: ''
    })).toEqual(['/payment-status']);
    expect(component.getNotificationRoute({
      id: 4, message: 'x', type: NotificationType.CLEANING_REMINDER, isRead: false, createdAt: ''
    })).toEqual(['/cleaning-schedules']);
  });
});

describe('NotificationPanelComponent empty state', () => {
  let component: NotificationPanelComponent;
  let fixture: ComponentFixture<NotificationPanelComponent>;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    configureNotificationPanel([]);
    await TestBed.compileComponents();

    fixture = TestBed.createComponent(NotificationPanelComponent);
    component = fixture.componentInstance;
    component.isOpen = true;
    fixture.detectChanges();
  });

  it('should display empty state when no notifications exist', () => {
    expect(component.notifications.length).toBe(0);
    expect(component.unreadCount).toBe(0);
    expect(fixture.nativeElement.querySelector('[data-cy="notification-empty-state"]')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('No notifications yet');
  });
});
