export enum CleaningFrequency {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  BIWEEKLY = 'BIWEEKLY',
  MONTHLY = 'MONTHLY'
}

export interface CleaningScheduleCreateDto {
  title: string;
  description?: string;
  frequency: CleaningFrequency;
  startDate: string; // ISO format date YYYY-MM-DD
  endDate?: string; // ISO format date YYYY-MM-DD
  assignedToEmail: string;
  fairBalancingEnabled: boolean;
}

export interface CleaningScheduleDetailDto {
  id: number;
  householdId: number;
  title: string;
  description?: string;
  frequency: CleaningFrequency;
  startDate: string;
  endDate?: string;
  nextDueDate: string;
  active: boolean;
  fairBalancingEnabled: boolean;
  createdByEmail: string;
  assignedToEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface CleaningScheduleUpdateDto {
  title: string;
  description?: string;
  frequency: CleaningFrequency;
  startDate: string;
  endDate?: string;
  assignedToEmail: string;
  fairBalancingEnabled: boolean;
}

export interface CleaningScheduleActiveUpdateDto {
  active: boolean;
}
