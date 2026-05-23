import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { TaskService } from '../../services/task.service';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';
import { CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
    standalone: false
})
export class HomeComponent implements OnInit {

  // Dashboard state
  openTaskCount = 0;
  myTaskCount = 0;
  activeScheduleCount = 0;
  housemateCount = 0;

  todayTasks: TaskDetailDto[] = [];
  upcomingSchedules: CleaningScheduleDetailDto[] = [];

  dashboardLoading = true;

  constructor(
    public authService: AuthService,
    private taskService: TaskService,
    private cleaningScheduleService: CleaningScheduleService,
    private householdService: HouseholdService
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
    let completed = 0;
    const checkDone = () => { completed++; if (completed >= 4) { this.dashboardLoading = false; } };

    // Open tasks
    this.taskService.findAll().subscribe({
      next: (tasks) => {
        this.openTaskCount = tasks.filter(t => t.status !== TaskStatus.DONE && t.status !== TaskStatus.CANCELLED).length;
        checkDone();
      },
      error: () => checkDone()
    });

    // My tasks
    this.taskService.findAssignedToMe().subscribe({
      next: (tasks) => {
        const openTasks = tasks.filter(t => t.status !== TaskStatus.DONE && t.status !== TaskStatus.CANCELLED);
        this.myTaskCount = openTasks.length;
        this.todayTasks = openTasks.filter(t => this.isToday(t.dueDate)).slice(0, 5);
        checkDone();
      },
      error: () => checkDone()
    });

    // Active schedules
    this.cleaningScheduleService.findActive().subscribe({
      next: (schedules) => {
        this.activeScheduleCount = schedules.length;
        this.upcomingSchedules = schedules
          .sort((a, b) => a.nextDueDate.localeCompare(b.nextDueDate))
          .slice(0, 5);
        checkDone();
      },
      error: () => checkDone()
    });

    // Housemates
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
  }

  private isToday(dateStr: string): boolean {
    const today = new Date();
    const todayStr = today.getFullYear() + '-' +
      String(today.getMonth() + 1).padStart(2, '0') + '-' +
      String(today.getDate()).padStart(2, '0');
    return dateStr === todayStr;
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
