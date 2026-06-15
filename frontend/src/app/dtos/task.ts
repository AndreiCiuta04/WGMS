export enum TaskStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  DONE = 'DONE',
  CANCELLED = 'CANCELLED'
}

export interface TaskCreateDto {
  title: string;
  description?: string;
  dueDate: string; // ISO format date YYYY-MM-DD
  assignedToEmail: string;
}

export interface TaskDetailDto {
  id: number;
  householdId: number;
  title: string;
  description?: string;
  dueDate: string;
  status: TaskStatus;
  createdByEmail: string;
  assignedToEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskStatusUpdateDto {
  status: TaskStatus;
}

export interface TaskUpdateDto {
  title: string;
  description?: string;
  dueDate: string;
  assignedToEmail: string;
}

export interface FairChoreRoommateStatsDto {
  roommateId: number;
  userId: number;
  displayName: string;
  email: string;
  completedTasks: number;
  openTasks: number;
  overdueTasks: number;
  fairnessBucket: string;
  fairnessScore: number;
  candidate: boolean;
  recommended: boolean;
}

export interface FairChoreRecommendationDto {
  recommendedRoommateId: number;
  recommendedUserId: number;
  recommendedDisplayName: string;
  recommendedEmail: string;
  historyDays: number;
  algorithm: string;
  reason: string;
  stats: FairChoreRoommateStatsDto[];
}

export interface FairChoreOverdueReminderDto {
  targetRoommateId: number;
  targetDisplayName: string;
  overdueTasks: number;
  message: string;
}
