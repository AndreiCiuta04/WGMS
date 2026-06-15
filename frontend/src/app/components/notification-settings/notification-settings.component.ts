import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../services/notification.service';
import { NotificationSettingDto } from '../../dtos/notification';

@Component({
  selector: 'app-notification-settings',
  templateUrl: './notification-settings.component.html',
  styleUrls: ['./notification-settings.component.scss'],
  standalone: false
})
export class NotificationSettingsComponent implements OnInit {

  settings: NotificationSettingDto = {
    notifyTaskAssigned: true,
    notifyTaskOverdue: true,
    notifyExpenseCreated: true,
    notifyPaymentReceived: true,
    notifyCleaningReminder: true,
    channelInApp: true,
    channelEmail: true,
    channelPush: false
  };

  loading = true;
  success = false;
  error = '';

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.loadSettings();
  }

  loadSettings(): void {
    this.loading = true;
    this.error = '';
    this.notificationService.getSettings().subscribe({
      next: (data) => {
        this.settings = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching notification settings', err);
        this.error = 'Failed to load notification settings.';
        this.loading = false;
      }
    });
  }

  saveSettings(): void {
    this.error = '';
    this.success = false;
    this.notificationService.updateSettings(this.settings).subscribe({
      next: (data) => {
        this.settings = data;
        this.success = true;
        setTimeout(() => this.success = false, 3000);
      },
      error: (err) => {
        console.error('Error updating notification settings', err);
        this.error = 'Failed to save notification settings.';
      }
    });
  }
}
