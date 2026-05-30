import { TaskDetailDto } from './task';
import { CleaningScheduleDetailDto } from './cleaning-schedule';
import { ShoppingItemDto } from './shopping-item';
import { BalanceDto } from './expense';

export interface DashboardDto {
  openTaskCount: number;
  myTaskCount: number;
  activeScheduleCount: number;
  housemateCount: number;
  myPendingTasks: TaskDetailDto[];
  householdSchedules: CleaningScheduleDetailDto[];
  wgShoppingList: ShoppingItemDto[];
  netBalance: number;
  whoOwesMe: BalanceDto[];
  whoIowe: BalanceDto[];
}
