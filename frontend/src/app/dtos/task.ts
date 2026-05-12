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
