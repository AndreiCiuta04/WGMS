import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NotificationPanelComponent } from './notification-panel.component';
import { NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';
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
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    const mocks = configureNotificationPanel(sampleNotifications.map(n => ({ ...n })));
    mockNotificationService = mocks.mockNotificationService;
    mockAuthService = mocks.mockAuthService;
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
    const event = jasmine.createSpyObj<Event>('Event', ['stopPropagation']);
    expect(component.isOpen).toBeFalse();
    component.toggleDropdown(event);
    expect(component.isOpen).toBeTrue();
    expect(event.stopPropagation).toHaveBeenCalled();
    expect(mockNotificationService.getNotifications).toHaveBeenCalledTimes(2);
  });

  it('clickout closes the dropdown only when clicking outside the host', () => {
    component.isOpen = true;

    component.clickout({ target: fixture.nativeElement } as unknown as Event);
    expect(component.isOpen).toBeTrue();

    component.clickout({ target: document.createElement('div') } as unknown as Event);
    expect(component.isOpen).toBeFalse();
  });

  it('should mark notification as read', () => {
    mockNotificationService.markAsRead.and.returnValue(of(undefined));
    const event = jasmine.createSpyObj<Event>('Event', ['stopPropagation']);
    const unreadNotif = component.notifications.find(n => !n.isRead)!;
    component.markAsRead(unreadNotif, event);
    expect(mockNotificationService.markAsRead).toHaveBeenCalledWith(unreadNotif.id);
    expect(unreadNotif.isRead).toBeTrue();
    expect(component.unreadCount).toBe(0);
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  it('does not call markAsRead for already-read notifications', () => {
    const event = jasmine.createSpyObj<Event>('Event', ['stopPropagation']);
    const readNotif = component.notifications.find(n => n.isRead)!;

    component.markAsRead(readNotif, event);

    expect(mockNotificationService.markAsRead).not.toHaveBeenCalled();
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  it('keeps unread count unchanged when markAsRead fails', () => {
    mockNotificationService.markAsRead.and.returnValue(throwError(() => ({ status: 500 })));
    const unreadNotif = component.notifications.find(n => !n.isRead)!;

    component.markAsRead(unreadNotif, jasmine.createSpyObj<Event>('Event', ['stopPropagation']));

    expect(unreadNotif.isRead).toBeFalse();
    expect(component.unreadCount).toBe(1);
  });

  it('should mark all as read', () => {
    mockNotificationService.markAllAsRead.and.returnValue(of(undefined));
    const event = jasmine.createSpyObj<Event>('Event', ['stopPropagation']);
    component.markAllAsRead(event);
    expect(mockNotificationService.markAllAsRead).toHaveBeenCalled();
    expect(component.notifications.every(n => n.isRead)).toBeTrue();
    expect(component.unreadCount).toBe(0);
    expect(event.stopPropagation).toHaveBeenCalled();
  });

  it('keeps notifications unchanged when markAllAsRead fails', () => {
    mockNotificationService.markAllAsRead.and.returnValue(throwError(() => ({ status: 500 })));

    component.markAllAsRead(jasmine.createSpyObj<Event>('Event', ['stopPropagation']));

    expect(component.notifications.some(n => !n.isRead)).toBeTrue();
    expect(component.unreadCount).toBe(1);
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

  it('does not navigate for unsupported notification types', () => {
    const navigateSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
    const unknown = {
      id: 99,
      message: 'Unknown',
      type: 'UNKNOWN' as NotificationType,
      isRead: true,
      createdAt: ''
    };

    component.openNotification(unknown, jasmine.createSpyObj<Event>('Event', ['stopPropagation']));

    expect(navigateSpy).not.toHaveBeenCalled();
    expect(component.getNotificationRoute(unknown)).toBeNull();
  });

  it('should resolve routes for supported notification types', () => {
    expect(component.getNotificationRoute({
      id: 1, message: 'x', type: NotificationType.TASK_OVERDUE, isRead: false, createdAt: ''
    })).toEqual(['/tasks']);
    expect(component.getNotificationRoute({
      id: 5, message: 'x', type: NotificationType.OVERDUE_TASK_REMINDER, isRead: false, createdAt: ''
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

  it('returns expected icon classes and names for every notification type and default', () => {
    expect(component.getIconClass(NotificationType.TASK_ASSIGNED)).toContain('primary');
    expect(component.getIconClass(NotificationType.TASK_OVERDUE)).toContain('danger');
    expect(component.getIconClass(NotificationType.OVERDUE_TASK_REMINDER)).toContain('danger');
    expect(component.getIconClass(NotificationType.EXPENSE_CREATED)).toContain('warning');
    expect(component.getIconClass(NotificationType.PAYMENT_RECEIVED)).toContain('success');
    expect(component.getIconClass(NotificationType.CLEANING_REMINDER)).toContain('info');
    expect(component.getIconClass('UNKNOWN' as NotificationType)).toContain('secondary');

    expect(component.getIconName(NotificationType.TASK_ASSIGNED)).toContain('person-check');
    expect(component.getIconName(NotificationType.TASK_OVERDUE)).toContain('exclamation-triangle');
    expect(component.getIconName(NotificationType.OVERDUE_TASK_REMINDER)).toContain('exclamation-triangle');
    expect(component.getIconName(NotificationType.EXPENSE_CREATED)).toContain('receipt');
    expect(component.getIconName(NotificationType.PAYMENT_RECEIVED)).toContain('cash');
    expect(component.getIconName(NotificationType.CLEANING_REMINDER)).toContain('stars');
    expect(component.getIconName('UNKNOWN' as NotificationType)).toContain('info-circle');
  });

  it('formats relative notification times', () => {
    jasmine.clock().install();
    jasmine.clock().mockDate(new Date('2026-06-26T12:00:00'));

    expect(component.formatRelativeTime('')).toBe('');
    expect(component.formatRelativeTime('2026-06-26T11:59:45')).toBe('Just now');
    expect(component.formatRelativeTime('2026-06-26T11:45:00')).toBe('15m ago');
    expect(component.formatRelativeTime('2026-06-26T10:00:00')).toBe('2h ago');
    expect(component.formatRelativeTime('2026-06-24T12:00:00')).toBe('2d ago');

    jasmine.clock().uninstall();
  });

  it('does not load or poll notifications when logged out', () => {
    mockNotificationService.getNotifications.calls.reset();
    mockAuthService.isLoggedIn.and.returnValue(false);

    component.ngOnDestroy();
    component.ngOnInit();

    expect(mockNotificationService.getNotifications).not.toHaveBeenCalled();
  });

  it('handles load notification errors without changing unread count', () => {
    mockNotificationService.getNotifications.and.returnValue(throwError(() => ({ status: 500 })));
    component.unreadCount = 7;

    component.loadNotifications();

    expect(component.unreadCount).toBe(7);
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
