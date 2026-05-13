import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { Roommate } from '../../dtos/roommate';
import { CleaningScheduleCreateDto, CleaningFrequency } from '../../dtos/cleaning-schedule';

@Component({
  selector: 'app-cleaning-schedule-create',
  templateUrl: './cleaning-schedule-create.component.html',
  styleUrls: ['./cleaning-schedule-create.component.scss'],
  standalone: false
})
export class CleaningScheduleCreateComponent implements OnInit {

  scheduleForm: FormGroup;
  roommates: Roommate[] = [];
  frequencies = Object.values(CleaningFrequency);
  submitted = false;
  error = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private cleaningScheduleService: CleaningScheduleService,
    private householdService: HouseholdService,
    private router: Router
  ) {
    this.scheduleForm = this.formBuilder.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(2000)]],
      frequency: ['', [Validators.required]],
      startDate: ['', [Validators.required]],
      endDate: [''],
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

  formatFrequency(frequency: string): string {
    switch (frequency) {
      case 'DAILY': return 'Daily';
      case 'WEEKLY': return 'Weekly';
      case 'BIWEEKLY': return 'Biweekly';
      case 'MONTHLY': return 'Monthly';
      default: return frequency;
    }
  }

  onSubmit() {
    this.submitted = true;
    if (this.scheduleForm.invalid) {
      return;
    }

    const formValue = this.scheduleForm.value;
    const dto: CleaningScheduleCreateDto = {
      title: formValue.title,
      description: formValue.description || undefined,
      frequency: formValue.frequency,
      startDate: formValue.startDate,
      endDate: formValue.endDate || undefined,
      assignedToEmail: formValue.assignedToEmail
    };

    this.cleaningScheduleService.create(dto).subscribe({
      next: () => {
        this.router.navigate(['/cleaning-schedules']);
      },
      error: (err) => {
        console.error('Error creating cleaning schedule', err);
        this.error = true;

        let errorMsg = err.error?.message || 'An error occurred while creating the cleaning schedule. Please try again.';

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
            if (parts.length > 1 && ['title', 'description', 'frequency', 'startDate', 'endDate', 'assignedToEmail'].includes(parts[0])) {
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
