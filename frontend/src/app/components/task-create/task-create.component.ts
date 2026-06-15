import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { HouseholdService } from '../../services/household.service';
import { Roommate } from '../../dtos/roommate';
import { FairChoreRecommendationDto, FairChoreRoommateStatsDto, TaskCreateDto } from '../../dtos/task';

@Component({
  selector: 'app-task-create',
  templateUrl: './task-create.component.html',
  styleUrls: ['./task-create.component.scss'],
  standalone: false
})
export class TaskCreateComponent implements OnInit {

  taskForm: FormGroup;
  roommates: Roommate[] = [];
  submitted = false;
  error = false;
  errorMessage = '';
  recommendation?: FairChoreRecommendationDto;
  recommendationLoading = false;
  recommendationError = '';
  isMainTenant = false;
  overdueReminderSuccess = '';
  overdueReminderError = '';
  sendingReminderRoommateIds = new Set<number>();
  reminderSentRoommateIds = new Set<number>();

  constructor(
    private formBuilder: FormBuilder,
    private taskService: TaskService,
    private householdService: HouseholdService,
    private router: Router
  ) {
    this.taskForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(2000)]],
      dueDate: ['', [Validators.required]],
      assignedToEmail: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.loadRoommates();
    this.loadFairBalancingRecommendation();
  }

  loadRoommates(): void {
    this.error = false;
    this.errorMessage = '';

    this.householdService.getMyHousehold().subscribe({
      next: household => {
        if (!household || !household.id) {
          this.error = true;
          this.errorMessage = 'You need to create or join a household before creating tasks.';
          return;
        }

        this.isMainTenant = household.currentUserRole === 'MAIN_TENANT';

        this.householdService.getMembers(household.id).subscribe({
          next: members => this.roommates = members,
          error: err => {
            console.error('Error fetching roommates', err);
            this.error = true;
            this.errorMessage = 'Could not load household members.';
          }
        });
      },
      error: err => {
        console.error('Error fetching household', err);
        this.error = true;
        this.errorMessage = 'Could not load your household.';
      }
    });
  }

  loadFairBalancingRecommendation(): void {
    this.recommendationLoading = true;
    this.recommendationError = '';

    this.taskService.getFairBalancingRecommendation({
      taskTitle: this.taskForm.get('title')?.value || undefined,
      dueDate: this.taskForm.get('dueDate')?.value || undefined
    }).subscribe({
      next: recommendation => {
        this.recommendation = recommendation;
        this.recommendationLoading = false;
      },
      error: err => {
        console.error('Error fetching fair chore recommendation', err);
        this.recommendationLoading = false;
        this.recommendationError = 'Could not load a fair chore suggestion.';
      }
    });
  }

  useSuggestion(): void {
    if (!this.recommendation?.recommendedEmail) {
      return;
    }

    this.taskForm.patchValue({
      assignedToEmail: this.recommendation.recommendedEmail
    });
  }

  canSendOverdueReminder(stat: FairChoreRoommateStatsDto): boolean {
    return this.isMainTenant && stat.overdueTasks >= 5;
  }

  isSendingReminder(roommateId: number): boolean {
    return this.sendingReminderRoommateIds.has(roommateId);
  }

  wasReminderSent(roommateId: number): boolean {
    return this.reminderSentRoommateIds.has(roommateId);
  }

  sendOverdueReminder(stat: FairChoreRoommateStatsDto): void {
    if (!this.canSendOverdueReminder(stat) || this.isSendingReminder(stat.roommateId)) {
      return;
    }

    this.overdueReminderSuccess = '';
    this.overdueReminderError = '';
    this.sendingReminderRoommateIds.add(stat.roommateId);

    this.taskService.sendOverdueReminder(stat.roommateId).subscribe({
      next: reminder => {
        this.sendingReminderRoommateIds.delete(stat.roommateId);
        this.reminderSentRoommateIds.add(stat.roommateId);
        this.overdueReminderSuccess = reminder.message || `Reminder sent to ${stat.displayName}.`;
      },
      error: err => {
        console.error('Error sending overdue reminder', err);
        this.sendingReminderRoommateIds.delete(stat.roommateId);
        this.overdueReminderError = this.buildReminderErrorMessage(err);
      }
    });
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = false;
    this.errorMessage = '';

    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    const taskDto: TaskCreateDto = this.taskForm.value;

    this.taskService.create(taskDto).subscribe({
      next: () => {
        this.router.navigate(['/tasks']);
      },
      error: err => {
        console.error('Error creating task', err);
        this.error = true;
        this.errorMessage = this.buildErrorMessage(
          err,
          'An error occurred while creating the task. Please try again.'
        );
      }
    });
  }

  private buildErrorMessage(err: any, fallback: string): string {
    let errorMsg = err.error?.message || fallback;

    let errorsArray: string[] = [];
    if (err.error?.['Validation errors']) {
      errorsArray = err.error['Validation errors'];
    } else if (typeof err.error === 'string' && err.error.includes('Validation errors=')) {
      const match = err.error.match(/Validation errors=\[(.*?)\]/);
      if (match && match[1]) {
        errorsArray = match[1].split(', ');
      }
    }

    if (errorsArray.length > 0) {
      errorMsg = errorsArray.map((e: string) => {
        const parts = e.split(' ');
        if (
          parts.length > 1 &&
          ['dueDate', 'title', 'description', 'assignedToEmail'].includes(parts[0])
        ) {
          return parts.slice(1).join(' ');
        }
        return e;
      }).join(', ');
    }

    return errorMsg;
  }

  private buildReminderErrorMessage(err: any): string {
    if (err?.status === 403) {
      return 'Only the Main Tenant can send overdue reminders for this household.';
    }
    if (err?.status === 400) {
      return 'Reminder was not sent because this roommate no longer has enough overdue tasks.';
    }
    if (err?.status === 404) {
      return 'Reminder was not sent because this roommate is no longer available.';
    }

    return this.buildErrorMessage(err, 'Could not send overdue reminder. Please try again.');
  }
}
