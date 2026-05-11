import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { HouseholdService } from '../../services/household.service';
import { Roommate } from '../../dtos/roommate';
import { TaskCreateDto } from '../../dtos/task';

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
  }

  loadRoommates() {
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => this.roommates = members,
            error: (err) => console.error('Error fetching roommates', err)
          });
        }
      },
      error: (err) => console.error('Error fetching household', err)
    });
  }

  onSubmit() {
    this.submitted = true;
    if (this.taskForm.invalid) {
      return;
    }

    const taskDto: TaskCreateDto = this.taskForm.value;
    
    this.taskService.create(taskDto).subscribe({
      next: () => {
        this.router.navigate(['/tasks']);
      },
      error: (err) => {
        console.error('Error creating task', err);
        this.error = true;
        
        let errorMsg = err.error?.message || 'An error occurred while creating the task. Please try again.';
        
        let errorsArray: string[] = [];
        if (err.error?.['Validation errors']) {
          errorsArray = err.error['Validation errors'];
        } else if (typeof err.error === 'string' && err.error.includes('Validation errors=')) {
          // Fallback if backend wasn't restarted and still sends the string map representation
          const match = err.error.match(/Validation errors=\[(.*?)\]/);
          if (match && match[1]) {
            errorsArray = match[1].split(', ');
          }
        }
        
        if (errorsArray.length > 0) {
          // Temporarily strip the camelCase field name if the backend hasn't been restarted yet
          errorMsg = errorsArray.map((e: string) => {
            const parts = e.split(' ');
            // If the first word looks like a camelCase property name, remove it
            if (parts.length > 1 && (parts[0] === 'dueDate' || parts[0] === 'title' || parts[0] === 'description' || parts[0] === 'assignedToEmail')) {
              return parts.slice(1).join(' ');
            }
            return e;
          }).join(', ');
        }
        
        this.errorMessage = errorMsg;
      }
    });
  }
}
