import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin } from 'rxjs';
import { TaskService } from '../../services/task.service';
import { HouseholdService } from '../../services/household.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';

type DueFilter = 'ALL' | 'OVERDUE' | 'TODAY' | 'WEEK';

@Component({
  selector: 'app-task-list',
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.scss'],
  standalone: false
})
export class TaskListComponent implements OnInit {

  allTasks: TaskDetailDto[] = [];
  myTasks: TaskDetailDto[] = [];
  displayedTasks: TaskDetailDto[] = [];

  currentTab: 'ALL' | 'MINE' = 'ALL';

  // Filter state
  searchTerm = '';
  statusFilter: TaskStatus | 'ALL' = 'ALL';
  assigneeFilter = 'ALL';
  dueFilter: DueFilter = 'ALL';
  filtersExpanded = false;

  assigneeOptions: { email: string; name: string }[] = [];
  private roommateMap = new Map<string, string>(); // email -> "First Last"

  loading = false;
  errorMessage = '';

  TaskStatus = TaskStatus;

  constructor(
    private taskService: TaskService,
    private householdService: HouseholdService,
    private route: ActivatedRoute,
    private confirmDialog: ConfirmDialogService
  ) { }

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('tab') === 'mine') {
      this.currentTab = 'MINE';
    }
    this.loadTasks();
    this.loadAssignees();
  }

  loadTasks(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      allTasks: this.taskService.findAll(),
      myTasks: this.taskService.findAssignedToMe()
    }).subscribe({
      next: ({ allTasks, myTasks }) => {
        this.allTasks = allTasks;
        this.myTasks = myTasks;
        this.applyFilters();
        this.loading = false;
      },
      error: err => {
        console.error('Error fetching tasks', err);
        this.errorMessage = 'Could not load tasks. Please try again.';
        this.loading = false;
      }
    });
  }

  switchTab(tab: 'ALL' | 'MINE'): void {
    this.currentTab = tab;
    this.applyFilters();
  }

  /** Recomputes displayedTasks from the active tab's list through all active filters. */
  applyFilters(): void {
    const base = this.currentTab === 'ALL' ? this.allTasks : this.myTasks;
    const term = this.searchTerm.trim().toLowerCase();
    const todayStr = this.toIsoDate(new Date());
    const weekEnd = new Date();
    weekEnd.setDate(weekEnd.getDate() + 7);
    const weekEndStr = this.toIsoDate(weekEnd);

    this.displayedTasks = base.filter(task => {
      if (term) {
        const haystack = `${task.title} ${task.description || ''}`.toLowerCase();
        if (!haystack.includes(term)) {
          return false;
        }
      }
      if (this.statusFilter !== 'ALL' && task.status !== this.statusFilter) {
        return false;
      }
      if (this.assigneeFilter !== 'ALL' && task.assignedToEmail !== this.assigneeFilter) {
        return false;
      }
      if (this.dueFilter === 'OVERDUE') {
        const open = task.status === TaskStatus.OPEN || task.status === TaskStatus.IN_PROGRESS;
        if (!open || task.dueDate >= todayStr) {
          return false;
        }
      } else if (this.dueFilter === 'TODAY') {
        if (task.dueDate !== todayStr) {
          return false;
        }
      } else if (this.dueFilter === 'WEEK') {
        if (task.dueDate < todayStr || task.dueDate > weekEndStr) {
          return false;
        }
      }
      return true;
    });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'ALL';
    this.assigneeFilter = 'ALL';
    this.dueFilter = 'ALL';
    this.applyFilters();
  }

  toggleFilters(): void {
    this.filtersExpanded = !this.filtersExpanded;
  }

  get activeFilterCount(): number {
    let count = 0;
    if (this.statusFilter !== 'ALL') { count++; }
    if (this.assigneeFilter !== 'ALL') { count++; }
    if (this.dueFilter !== 'ALL') { count++; }
    return count;
  }

  get hasActiveFilters(): boolean {
    return this.activeFilterCount > 0 || this.searchTerm.trim().length > 0;
  }

  get baseTaskCount(): number {
    return (this.currentTab === 'ALL' ? this.allTasks : this.myTasks).length;
  }

  statusBadgeClass(status: TaskStatus): string {
    switch (status) {
      case TaskStatus.OPEN:
      case TaskStatus.IN_PROGRESS:
        return 'badge-wgms-active';
      case TaskStatus.CANCELLED:
        return 'badge-wgms-expired';
      case TaskStatus.DONE:
      default:
        return 'badge-wgms-used';
    }
  }

  statusLabel(status: TaskStatus): string {
    switch (status) {
      case TaskStatus.OPEN: return 'Open';
      case TaskStatus.IN_PROGRESS: return 'In Progress';
      case TaskStatus.DONE: return 'Done';
      case TaskStatus.CANCELLED: return 'Cancelled';
      default: return status;
    }
  }

  completeTask(task: TaskDetailDto): void {
    this.errorMessage = '';

    this.taskService.updateStatus(task.id, { status: TaskStatus.DONE }).subscribe({
      next: () => this.loadTasks(),
      error: err => {
        console.error('Error completing task', err);
        this.errorMessage = 'Could not mark the task as done.';
      }
    });
  }

  reopenTask(task: TaskDetailDto): void {
    this.errorMessage = '';

    this.taskService.updateStatus(task.id, { status: TaskStatus.OPEN }).subscribe({
      next: () => this.loadTasks(),
      error: err => {
        console.error('Error reopening task', err);
        this.errorMessage = 'Could not reopen the task.';
      }
    });
  }

  deleteTask(task: TaskDetailDto): void {
    this.errorMessage = '';

    this.confirmDialog.confirm({
      title: 'Delete task?',
      message: `"${task.title}" will be permanently removed.`,
      confirmLabel: 'Delete',
      danger: true
    }).then(confirmed => {
      if (!confirmed) {
        return;
      }
      this.taskService.delete(task.id).subscribe({
        next: () => this.loadTasks(),
        error: err => {
          console.error('Error deleting task', err);
          this.errorMessage = 'Could not delete the task.';
        }
      });
    });
  }

  /** Loads household members for friendly assignee filter labels; falls back to task emails. */
  private loadAssignees(): void {
    this.householdService.getMyHousehold().subscribe({
      next: household => {
        if (household?.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: members => {
              this.roommateMap = new Map(members.map(m => [m.email, `${m.firstName} ${m.lastName}`.trim()]));
              this.assigneeOptions = members.map(m => ({
                email: m.email,
                name: `${m.firstName} ${m.lastName}`.trim() || m.email
              }));
            },
            error: () => this.deriveAssigneesFromTasks()
          });
        } else {
          this.deriveAssigneesFromTasks();
        }
      },
      error: () => this.deriveAssigneesFromTasks()
    });
  }

  private deriveAssigneesFromTasks(): void {
    const emails = new Set<string>();
    [...this.allTasks, ...this.myTasks].forEach(t => emails.add(t.assignedToEmail));
    this.assigneeOptions = Array.from(emails).map(email => ({
      email,
      name: this.roommateMap.get(email) || email
    }));
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
