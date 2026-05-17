import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { TaskService } from '../../services/task.service';
import { TaskDetailDto, TaskStatus } from '../../dtos/task';

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

  loading = false;
  errorMessage = '';

  TaskStatus = TaskStatus;

  constructor(private taskService: TaskService) { }

  ngOnInit(): void {
    this.loadTasks();
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
        this.displayedTasks = this.currentTab === 'ALL' ? this.allTasks : this.myTasks;
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
    this.displayedTasks = tab === 'ALL' ? this.allTasks : this.myTasks;
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

  deleteTask(task: TaskDetailDto): void {
    this.errorMessage = '';

    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.delete(task.id).subscribe({
        next: () => this.loadTasks(),
        error: err => {
          console.error('Error deleting task', err);
          this.errorMessage = 'Could not delete the task.';
        }
      });
    }
  }
}
