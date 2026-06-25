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

export type StatisticsGrouping = 'DAY' | 'WEEK' | 'MONTH';

export type StatisticsModule = 'TASKS' | 'CLEANING' | 'EXPENSES' | 'PAYMENTS' | 'SHOPPING';

export interface StatisticsFilter {
  from: string;
  to: string;
  grouping: StatisticsGrouping;
  modules?: StatisticsModule[];
}

export interface StatisticsDataPointDto {
  periodStart: string;
  value: number;
}

export interface StatisticsSeriesDto {
  key: string;
  label: string;
  points: StatisticsDataPointDto[];
}

export interface StatisticsCategoryValueDto {
  key: string;
  label: string;
  value: number;
}

export interface StatisticsMemberValueDto {
  roommateId: number | null;
  displayName: string;
  value: number;
}

export interface StatisticsOverviewDto {
  from: string;
  to: string;
  grouping: StatisticsGrouping;
  completedTaskCount: number;
  taskCompletionRate: number;
  totalExpenseAmount: number;
  recordedPaymentAmount: number;
  purchasedShoppingItemCount: number;
  activeCleaningScheduleCount: number;
  totalActivityCount: number;
  activityByModule: StatisticsCategoryValueDto[];
  activityTrend: StatisticsSeriesDto[];
}

export interface TaskStatisticsDto {
  from: string;
  to: string;
  grouping: StatisticsGrouping;
  totalTaskCount: number;
  completedTaskCount: number;
  cancelledTaskCount: number;
  overdueTaskCount: number;
  completionRate: number;
  totalCleaningScheduleCount: number;
  activeCleaningScheduleCount: number;
  scheduleGeneratedTaskCount: number;
  scheduleGeneratedTaskCompletionRate: number;
  taskStatusDistribution: StatisticsCategoryValueDto[];
  cleaningFrequencyDistribution: StatisticsCategoryValueDto[];
  completionTrend: StatisticsSeriesDto[];
  completedTasksByMember: StatisticsMemberValueDto[];
  completedTasksByWeekday: StatisticsCategoryValueDto[];
}

export interface ExpenseStatisticsDto {
  from: string;
  to: string;
  grouping: StatisticsGrouping;
  expenseCount: number;
  totalExpenseAmount: number;
  averageExpenseAmount: number;
  largestExpenseAmount: number;
  paymentCount: number;
  totalPaymentAmount: number;
  expenseTrend: StatisticsSeriesDto[];
  paymentTrend: StatisticsSeriesDto[];
  expensesPaidByMember: StatisticsMemberValueDto[];
  paymentsSentByMember: StatisticsMemberValueDto[];
  paymentsReceivedByMember: StatisticsMemberValueDto[];
}

export interface ShoppingStatisticsDto {
  from: string;
  to: string;
  grouping: StatisticsGrouping;
  addedItemCount: number;
  purchasedItemCount: number;
  purchaseCompletionRate: number;
  statusDistribution: StatisticsCategoryValueDto[];
  visibilityDistribution: StatisticsCategoryValueDto[];
  itemActivityTrend: StatisticsSeriesDto[];
  mostFrequentlyPurchasedItems: StatisticsCategoryValueDto[];
  itemsAddedByMember: StatisticsMemberValueDto[];
}