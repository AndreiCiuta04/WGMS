import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { DashboardService } from '../../services/dashboard.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';
import { CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';
import { ShoppingItemDto, ShoppingItemStatus } from '../../dtos/shopping-item';
import { UserProfile } from '../../dtos/user-profile';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: false
})
export class HomeComponent implements OnInit {

  // Dashboard counters
  openTaskCount = 0;
  myTaskCount = 0;
  activeScheduleCount = 0;
  housemateCount = 0;

  // Active items lists
  myPendingTasks: TaskDetailDto[] = [];
  householdSchedules: CleaningScheduleDetailDto[] = [];
  wgShoppingList: ShoppingItemDto[] = [];

  // Financial status
  netBalance = 0;
  totalOwedToMe = 0;
  totalIOwe = 0;
  whoOwesMe: { name: string, amount: number }[] = [];
  whoIOwe: { name: string, amount: number }[] = [];

  dashboardLoading = true;
  currentUserEmail: string | null = null;
  hasNoHousehold = false;
  profile: UserProfile | null = null;

  constructor(
    public authService: AuthService,
    private taskService: TaskService,
    private shoppingItemService: ShoppingItemService,
    private dashboardService: DashboardService
  ) { }

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.authService.getCurrentUserProfile().subscribe({
        next: profile => this.profile = profile,
        error: () => this.profile = null
      });
      this.loadDashboard();
    }
  }

  getUserFirstName(): string {
    if (this.profile?.firstName) {
      const name = this.profile.firstName;
      return name.charAt(0).toUpperCase() + name.slice(1);
    }
    const email = this.authService.getUserEmail();
    if (email) {
      const name = email.split('@')[0];
      return name.charAt(0).toUpperCase() + name.slice(1);
    }
    return 'there';
  }

  private loadDashboard() {
    this.dashboardLoading = true;
    this.currentUserEmail = this.authService.getUserEmail();
    this.hasNoHousehold = false;

    this.dashboardService.getDashboardData().subscribe({
      next: (data) => {
        this.openTaskCount = data.openTaskCount;
        this.myTaskCount = data.myTaskCount;
        this.activeScheduleCount = data.activeScheduleCount;
        this.housemateCount = data.housemateCount;
        this.myPendingTasks = data.myPendingTasks || [];
        this.householdSchedules = data.householdSchedules || [];
        this.wgShoppingList = data.wgShoppingList || [];
        this.netBalance = data.netBalance || 0;
        this.whoOwesMe = (data.whoOwesMe || []).map(b => ({ name: b.fromRoommateName, amount: b.amount }));
        this.whoIOwe = (data.whoIowe || []).map(b => ({ name: b.toRoommateName, amount: b.amount }));
        this.dashboardLoading = false;
        this.hasNoHousehold = false;
      },
      error: (err) => {
        console.error('Error loading dashboard:', err);
        this.dashboardLoading = false;
        if (err.status === 403) {
          this.hasNoHousehold = true;
        }
      }
    });
  }

  isOverdue(dueDateStr: string): boolean {
    if (!dueDateStr) return false;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const dueDate = new Date(dueDateStr);
    dueDate.setHours(0, 0, 0, 0);
    return dueDate < today;
  }

  completeTask(id: number) {
    this.taskService.updateStatus(id, { status: TaskStatus.DONE }).subscribe({
      next: () => {
        this.myPendingTasks = this.myPendingTasks.filter(t => t.id !== id);
        this.myTaskCount = this.myPendingTasks.length;
        this.openTaskCount = Math.max(0, this.openTaskCount - 1);
      },
      error: (err) => {
        console.error('Error completing task:', err);
      }
    });
  }

  buyShoppingItem(id: number) {
    this.shoppingItemService.updateStatus(id, { status: ShoppingItemStatus.BOUGHT }).subscribe({
      next: () => {
        this.wgShoppingList = this.wgShoppingList.filter(item => item.id !== id);
      },
      error: (err) => {
        console.error('Error buying shopping item:', err);
      }
    });
  }

  formatFrequency(frequency: string): string {
    switch (frequency) {
      case 'DAILY': return 'Daily';
      case 'WEEKLY': return 'Weekly';
      case 'BIWEEKLY': return 'Biweekly';
      case 'MONTHLY': return 'Monthly';
      default: return frequency;
    }
  }
}
