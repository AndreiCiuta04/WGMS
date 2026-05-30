import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TaskService } from '../../services/task.service';
import { HouseholdService } from '../../services/household.service';
import { Roommate } from '../../dtos/roommate';
import { TaskUpdateDto } from '../../dtos/task';

@Component({
  selector: 'app-task-edit',
  templateUrl: './task-edit.component.html',
  styleUrls: ['./task-edit.component.scss'],
  standalone: false
})
export class TaskEditComponent implements OnInit {

  taskId!: number;
  taskForm: FormGroup;
  roommates: Roommate[] = [];
  submitted = false;
  error = false;
  errorMessage = '';
  loading = true;

  constructor(
    private formBuilder: FormBuilder,
    private taskService: TaskService,
    private householdService: HouseholdService,
    private route: ActivatedRoute,
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
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.taskId = Number(idParam);
      this.loadRoommatesAndTask();
    } else {
      this.router.navigate(['/tasks']);
    }
  }

  loadRoommatesAndTask() {
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => {
              this.roommates = members;
              this.loadTask();
            },
            error: (err) => {
              console.error('Error fetching roommates', err);
              this.error = true;
              this.errorMessage = 'Failed to load household members.';
              this.loading = false;
            }
          });
        }
      },
      error: (err) => {
        console.error('Error fetching household', err);
        this.error = true;
        this.errorMessage = 'Failed to load household.';
        this.loading = false;
      }
    });
  }

  loadTask() {
    this.taskService.findOne(this.taskId).subscribe({
      next: (task) => {
        this.taskForm.patchValue({
          title: task.title,
          description: task.description || '',
          dueDate: task.dueDate,
          assignedToEmail: task.assignedToEmail
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching task', err);
        this.error = true;
        this.errorMessage = 'Failed to load the task details.';
        this.loading = false;
      }
    });
  }

  onSubmit() {
    this.submitted = true;
    if (this.taskForm.invalid) {
      return;
    }

    const taskDto: TaskUpdateDto = this.taskForm.value;
    
    this.taskService.update(this.taskId, taskDto).subscribe({
      next: () => {
        this.router.navigate(['/tasks']);
      },
      error: (err) => {
        console.error('Error updating task', err);
        this.error = true;
        
        let errorMsg = err.error?.message || 'An error occurred while updating the task. Please try again.';
        
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
          errorMsg = errorsArray.map((e: string) => {
            const parts = e.split(' ');
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
