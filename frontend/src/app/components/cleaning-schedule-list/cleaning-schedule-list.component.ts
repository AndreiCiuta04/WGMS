import { Component, OnInit } from '@angular/core';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { CleaningScheduleDetailDto } from '../../dtos/cleaning-schedule';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { Roommate } from '../../dtos/roommate';

@Component({
  selector: 'app-cleaning-schedule-list',
  templateUrl: './cleaning-schedule-list.component.html',
  styleUrls: ['./cleaning-schedule-list.component.scss'],
  standalone: false
})
export class CleaningScheduleListComponent implements OnInit {

  allSchedules: CleaningScheduleDetailDto[] = [];

  dueNowSchedules: CleaningScheduleDetailDto[] = [];
  upcomingSchedules: CleaningScheduleDetailDto[] = [];

  roommates: Roommate[] = [];
  roommateMap: Map<string, string> = new Map(); // email -> first name

  manageExpanded = false;
  successMessage = '';

  constructor(
    private cleaningScheduleService: CleaningScheduleService,
    private householdService: HouseholdService,
    public authService: AuthService
  ) { }

  ngOnInit(): void {
    this.loadRoommates();
    this.loadSchedules();
  }

  loadSchedules() {
    this.cleaningScheduleService.findAll().subscribe({
      next: (schedules) => {
        this.allSchedules = schedules;
        this.categorizeSchedules();
      },
      error: (err) => console.error('Error fetching cleaning schedules', err)
    });
  }

  loadRoommates() {
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => {
              this.roommates = members;
              this.roommateMap = new Map(
                members.map(m => [m.email, m.firstName])
              );
            },
            error: (err) => console.error('Error fetching roommates', err)
          });
        }
      },
      error: (err) => console.error('Error fetching household', err)
    });
  }

  private categorizeSchedules() {
    const todayStr = this.getTodayStr();

    this.dueNowSchedules = this.allSchedules
      .filter(s => s.active && s.nextDueDate <= todayStr)
      .sort((a, b) => a.nextDueDate.localeCompare(b.nextDueDate));

    this.upcomingSchedules = this.allSchedules
      .filter(s => s.active && s.nextDueDate > todayStr)
      .sort((a, b) => a.nextDueDate.localeCompare(b.nextDueDate));
  }

  generateTask(schedule: CleaningScheduleDetailDto) {
    this.cleaningScheduleService.generateTask(schedule.id).subscribe({
      next: () => {
        this.successMessage = `Task created for "${schedule.title}"!`;
        this.loadSchedules();
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => console.error('Error generating task', err)
    });
  }

  toggleActive(schedule: CleaningScheduleDetailDto) {
    this.cleaningScheduleService.updateActive(schedule.id, { active: !schedule.active }).subscribe({
      next: () => this.loadSchedules(),
      error: (err) => console.error('Error toggling active status', err)
    });
  }

  deleteSchedule(schedule: CleaningScheduleDetailDto) {
    if (confirm(`Delete "${schedule.title}"? This cannot be undone.`)) {
      this.cleaningScheduleService.delete(schedule.id).subscribe({
        next: () => this.loadSchedules(),
        error: (err) => console.error('Error deleting cleaning schedule', err)
      });
    }
  }

  toggleManageExpanded() {
    this.manageExpanded = !this.manageExpanded;
  }

  getAssigneeName(email: string): string {
    const name = this.roommateMap.get(email);
    return name || email.split('@')[0];
  }

  getDueLabel(schedule: CleaningScheduleDetailDto): string {
    const todayStr = this.getTodayStr();
    if (schedule.nextDueDate === todayStr) {
      return 'Due today';
    }
    const today = new Date();
    const due = new Date(schedule.nextDueDate + 'T00:00:00');
    const diffMs = due.getTime() - today.getTime();
    const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
      return `Overdue by ${Math.abs(diffDays)} day${Math.abs(diffDays) !== 1 ? 's' : ''}`;
    }
    if (diffDays === 1) {
      return 'Due tomorrow';
    }
    return `Due in ${diffDays} days`;
  }

  isOverdue(schedule: CleaningScheduleDetailDto): boolean {
    return schedule.nextDueDate < this.getTodayStr();
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

  private getTodayStr(): string {
    const today = new Date();
    return today.getFullYear() + '-' +
      String(today.getMonth() + 1).padStart(2, '0') + '-' +
      String(today.getDate()).padStart(2, '0');
  }
}
