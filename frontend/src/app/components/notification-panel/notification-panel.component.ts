import { Component, OnInit, OnDestroy, HostListener, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { NotificationDto, NotificationType } from '../../dtos/notification';
import { AuthService } from '../../services/auth.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-notification-panel',
  templateUrl: './notification-panel.component.html',
  styleUrls: ['./notification-panel.component.scss'],
  standalone: false
})
export class NotificationPanelComponent implements OnInit, OnDestroy {

  notifications: NotificationDto[] = [];
  unreadCount = 0;
  isOpen = false;
  private refreshSub: Subscription | null = null;

  constructor(
    private notificationService: NotificationService,
    public authService: AuthService,
    private elementRef: ElementRef,
    private router: Router
  ) { }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadNotifications();
      // Poll every 30 seconds
      this.refreshSub = interval(30000).subscribe(() => {
        if (this.authService.isLoggedIn()) {
          this.loadNotifications();
        }
      });
    }
  }

  ngOnDestroy(): void {
    if (this.refreshSub) {
      this.refreshSub.unsubscribe();
    }
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data;
        this.unreadCount = data.filter(n => !n.isRead).length;
      },
      error: (err) => {
        console.error('Error fetching notifications', err);
      }
    });
  }

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.loadNotifications();
    }
  }

  @HostListener('document:click', ['$event'])
  clickout(event: Event): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.isOpen = false;
    }
  }

  markAsRead(notification: NotificationDto, event: Event): void {
    event.stopPropagation();
    if (notification.isRead) {
      return;
    }
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.isRead = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      },
      error: (err) => {
        console.error('Error marking notification as read', err);
      }
    });
  }

  openNotification(notification: NotificationDto, event: Event): void {
    event.stopPropagation();
    if (!notification.isRead) {
      this.markAsRead(notification, event);
    }
    const route = this.getNotificationRoute(notification);
    if (route) {
      this.isOpen = false;
      this.router.navigate(route);
    }
  }

  getNotificationRoute(notification: NotificationDto): string[] | null {
    switch (notification.type) {
      case NotificationType.TASK_ASSIGNED:
      case NotificationType.TASK_OVERDUE:
        return ['/tasks'];
      case NotificationType.EXPENSE_CREATED:
        return ['/expenses'];
      case NotificationType.PAYMENT_RECEIVED:
        return ['/payment-status'];
      case NotificationType.CLEANING_REMINDER:
        return ['/cleaning-schedules'];
      default:
        return null;
    }
  }

  markAllAsRead(event: Event): void {
    event.stopPropagation();
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
        this.unreadCount = 0;
      },
      error: (err) => {
        console.error('Error marking all notifications as read', err);
      }
    });
  }

  getIconClass(type: NotificationType): string {
    switch (type) {
      case NotificationType.TASK_ASSIGNED:
        return 'bg-primary-subtle text-primary';
      case NotificationType.TASK_OVERDUE:
        return 'bg-danger-subtle text-danger';
      case NotificationType.EXPENSE_CREATED:
        return 'bg-warning-subtle text-warning';
      case NotificationType.PAYMENT_RECEIVED:
        return 'bg-success-subtle text-success';
      case NotificationType.CLEANING_REMINDER:
        return 'bg-info-subtle text-info';
      default:
        return 'bg-secondary-subtle text-secondary';
    }
  }

  getIconName(type: NotificationType): string {
    switch (type) {
      case NotificationType.TASK_ASSIGNED:
        return 'bi bi-person-check-fill';
      case NotificationType.TASK_OVERDUE:
        return 'bi bi-exclamation-triangle-fill';
      case NotificationType.EXPENSE_CREATED:
        return 'bi bi-receipt-cutoff';
      case NotificationType.PAYMENT_RECEIVED:
        return 'bi bi-cash-coin';
      case NotificationType.CLEANING_REMINDER:
        return 'bi bi-stars';
      default:
        return 'bi bi-info-circle-fill';
    }
  }

  formatRelativeTime(createdAtString: string): string {
    if (!createdAtString) { return ''; }
    const created = new Date(createdAtString);
    const now = new Date();
    const diffMs = now.getTime() - created.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) {
      return 'Just now';
    }
    if (diffMins < 60) {
      return `${diffMins}m ago`;
    }
    if (diffHours < 24) {
      return `${diffHours}h ago`;
    }
    return `${diffDays}d ago`;
  }
}
