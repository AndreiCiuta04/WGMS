import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationSettingsComponent } from './notification-settings.component';
import { NotificationService } from '../../services/notification.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Globals } from '../../global/globals';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

describe('NotificationSettingsComponent', () => {
  let component: NotificationSettingsComponent;
  let fixture: ComponentFixture<NotificationSettingsComponent>;
  let mockNotificationService: any;

  beforeEach(async () => {
    mockNotificationService = jasmine.createSpyObj('NotificationService', ['getSettings', 'updateSettings']);
    mockNotificationService.getSettings.and.returnValue(of({
      notifyTaskAssigned: true,
      notifyTaskOverdue: true,
      notifyExpenseCreated: true,
      notifyPaymentReceived: true,
      notifyCleaningReminder: true,
      channelInApp: true,
      channelEmail: true,
      channelPush: false
    }));

    await TestBed.configureTestingModule({
      declarations: [NotificationSettingsComponent],
      imports: [FormsModule],
      providers: [
        { provide: NotificationService, useValue: mockNotificationService },
        Globals,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(NotificationSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
