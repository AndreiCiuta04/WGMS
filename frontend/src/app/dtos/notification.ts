export enum NotificationType {
  TASK_ASSIGNED = 'TASK_ASSIGNED',
  TASK_OVERDUE = 'TASK_OVERDUE',
  EXPENSE_CREATED = 'EXPENSE_CREATED',
  PAYMENT_RECEIVED = 'PAYMENT_RECEIVED',
  CLEANING_REMINDER = 'CLEANING_REMINDER'
}

export interface NotificationDto {
  id: number;
  message: string;
  type: NotificationType;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationSettingDto {
  notifyTaskAssigned: boolean;
  notifyTaskOverdue: boolean;
  notifyExpenseCreated: boolean;
  notifyPaymentReceived: boolean;
  notifyCleaningReminder: boolean;
  channelInApp: boolean;
  channelEmail: boolean;
  channelPush: boolean;
}

export interface UserVerifyDto {
  email: string;
  code: string;
}
