export interface HistoryDto {
  tasks: TaskHistoryDto[];
  payments: PaymentHistoryDto[];
}

export interface TaskHistoryDto {
  id: number;
  householdId: number;
  cleaningScheduleId?: number | null;
  title: string;
  description?: string | null;
  dueDate: string;
  status: string;
  createdByEmail: string;
  assignedToEmail: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentHistoryDto {
  id: number;
  title: string;
  amount: number;
  status: string;
  payerEmail?: string | null;
  receiverEmail?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}
