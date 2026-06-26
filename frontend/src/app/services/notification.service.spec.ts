import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { Globals } from '../global/globals';
import { NotificationDto, NotificationType, NotificationSettingDto, UserVerifyDto } from '../dtos/notification';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;
  let globals: Globals;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [],
      providers: [
        NotificationService,
        Globals,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
    globals = TestBed.inject(Globals);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch notifications', () => {
    const dummyNotifications: NotificationDto[] = [
      { id: 1, message: 'Test assigned', type: NotificationType.TASK_ASSIGNED, isRead: false, createdAt: '2026-06-08T12:00:00' }
    ];

    service.getNotifications().subscribe(data => {
      expect(data).toEqual(dummyNotifications);
    });

    const req = httpMock.expectOne(`${globals.backendUri}/notifications`);
    expect(req.request.method).toBe('GET');
    req.flush(dummyNotifications);
  });

  it('should mark notification as read', () => {
    service.markAsRead(1).subscribe();

    const req = httpMock.expectOne(`${globals.backendUri}/notifications/1/read`);
    expect(req.request.method).toBe('PATCH');
    req.flush({});
  });

  it('should mark all as read', () => {
    service.markAllAsRead().subscribe();

    const req = httpMock.expectOne(`${globals.backendUri}/notifications/read-all`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should fetch settings', () => {
    const dummySettings: NotificationSettingDto = {
      notifyTaskAssigned: true,
      notifyTaskOverdue: true,
      notifyExpenseCreated: true,
      notifyPaymentReceived: true,
      notifyCleaningReminder: true,
      channelInApp: true,
      channelEmail: true,
      channelPush: false
    };

    service.getSettings().subscribe(data => {
      expect(data).toEqual(dummySettings);
    });

    const req = httpMock.expectOne(`${globals.backendUri}/notifications/settings`);
    expect(req.request.method).toBe('GET');
    req.flush(dummySettings);
  });

  it('should update settings', () => {
    const settings: NotificationSettingDto = {
      notifyTaskAssigned: false,
      notifyTaskOverdue: true,
      notifyExpenseCreated: false,
      notifyPaymentReceived: true,
      notifyCleaningReminder: false,
      channelInApp: true,
      channelEmail: false,
      channelPush: false
    };

    service.updateSettings(settings).subscribe(data => expect(data).toEqual(settings));

    const req = httpMock.expectOne(`${globals.backendUri}/notifications/settings`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(settings);
    req.flush(settings);
  });

  it('should verify user and store bearer token without prefix', () => {
    spyOn(localStorage, 'setItem');
    const verifyDto: UserVerifyDto = { email: 'anna@example.com', code: '123456' };

    service.verifyUser(verifyDto).subscribe(token => expect(token).toBe('Bearer jwt-token'));

    const req = httpMock.expectOne(`${globals.backendUri}/users/verify`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(verifyDto);
    req.flush('Bearer jwt-token');
    expect(localStorage.setItem).toHaveBeenCalledWith('authToken', 'jwt-token');
  });

  it('should store verification token unchanged when no bearer prefix is returned', () => {
    spyOn(localStorage, 'setItem');

    service.verifyUser({ email: 'anna@example.com', code: '654321' }).subscribe(token => expect(token).toBe('plain-token'));

    const req = httpMock.expectOne(`${globals.backendUri}/users/verify`);
    expect(req.request.method).toBe('POST');
    req.flush('plain-token');
    expect(localStorage.setItem).toHaveBeenCalledWith('authToken', 'plain-token');
  });
});
