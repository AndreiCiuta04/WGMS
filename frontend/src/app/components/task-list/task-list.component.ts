import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  
  TaskStatus = TaskStatus;

  constructor(private taskService: TaskService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('tab') === 'mine') {
      this.currentTab = 'MINE';
    }
    this.loadTasks();
  }

  loadTasks() {
    this.taskService.findAll().subscribe({
      next: (tasks) => {
        this.allTasks = tasks;
        if (this.currentTab === 'ALL') {
          this.displayedTasks = this.allTasks;
        }
      },
      error: (err) => console.error('Error fetching all tasks', err)
    });

    this.taskService.findAssignedToMe().subscribe({
      next: (tasks) => {
        this.myTasks = tasks;
        if (this.currentTab === 'MINE') {
          this.displayedTasks = this.myTasks;
        }
      },
      error: (err) => console.error('Error fetching my tasks', err)
    });
  }

  switchTab(tab: 'ALL' | 'MINE') {
    this.currentTab = tab;
    if (tab === 'ALL') {
      this.displayedTasks = this.allTasks;
    } else {
      this.displayedTasks = this.myTasks;
    }
  }

  completeTask(task: TaskDetailDto) {
    this.taskService.updateStatus(task.id, { status: TaskStatus.DONE }).subscribe({
      next: () => this.loadTasks(),
      error: (err) => console.error('Error completing task', err)
    });
  }

  deleteTask(task: TaskDetailDto) {
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.delete(task.id).subscribe({
        next: () => this.loadTasks(),
        error: (err) => console.error('Error deleting task', err)
      });
    }
  }
}
