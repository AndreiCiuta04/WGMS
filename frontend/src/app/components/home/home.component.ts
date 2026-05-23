import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { ShoppingItemService } from '../../services/shopping-item.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';
import { CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';
import { ShoppingItemDto, ShoppingItemStatus } from '../../dtos/shopping-item';

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

  dashboardLoading = true;
  currentUserEmail: string | null = null;

  constructor(
    public authService: AuthService,
    private taskService: TaskService,
    private cleaningScheduleService: CleaningScheduleService,
    private householdService: HouseholdService,
    private shoppingItemService: ShoppingItemService
  ) { }

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.loadDashboard();
    }
  }

  getUserFirstName(): string {
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
    let completed = 0;
    const checkDone = () => { completed++; if (completed >= 5) { this.dashboardLoading = false; } };

    // 1. All household tasks (to calculate general stats)
    this.taskService.findAll().subscribe({
      next: (tasks) => {
        this.openTaskCount = tasks.filter(t => t.status !== TaskStatus.DONE && t.status !== TaskStatus.CANCELLED).length;
        checkDone();
      },
      error: () => checkDone()
    });

    // 2. Tasks assigned to current user
    this.taskService.findAssignedToMe().subscribe({
      next: (tasks) => {
        this.myPendingTasks = tasks.filter(t => t.status !== TaskStatus.DONE && t.status !== TaskStatus.CANCELLED)
          .sort((a, b) => a.dueDate.localeCompare(b.dueDate));
        this.myTaskCount = this.myPendingTasks.length;
        checkDone();
      },
      error: () => checkDone()
    });

    // 3. Active schedules
    this.cleaningScheduleService.findActive().subscribe({
      next: (schedules) => {
        this.activeScheduleCount = schedules.length;
        this.householdSchedules = schedules
          .sort((a, b) => a.nextDueDate.localeCompare(b.nextDueDate))
          .slice(0, 5);
        checkDone();
      },
      error: () => checkDone()
    });

    // 4. Housemates
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => { this.housemateCount = members.length; checkDone(); },
            error: () => checkDone()
          });
        } else {
          checkDone();
        }
      },
      error: () => checkDone()
    });

    // 5. Shopping items
    this.shoppingItemService.findAll().subscribe({
      next: (items) => {
        this.wgShoppingList = items.filter(item => item.status === ShoppingItemStatus.NEEDED)
          .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
          .slice(0, 5);
        checkDone();
      },
      error: () => checkDone()
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
