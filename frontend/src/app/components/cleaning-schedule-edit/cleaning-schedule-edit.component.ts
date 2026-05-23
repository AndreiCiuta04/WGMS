import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CleaningScheduleService } from '../../services/cleaning-schedule.service';
import { HouseholdService } from '../../services/household.service';
import { Roommate } from '../../dtos/roommate';
import { CleaningScheduleUpdateDto, CleaningFrequency } from '../../dtos/cleaning-schedule';

@Component({
  selector: 'app-cleaning-schedule-edit',
  templateUrl: './cleaning-schedule-edit.component.html',
  styleUrls: ['./cleaning-schedule-edit.component.scss'],
  standalone: false
})
export class CleaningScheduleEditComponent implements OnInit {

  scheduleId!: number;
  scheduleForm: FormGroup;
  roommates: Roommate[] = [];
  frequencies = Object.values(CleaningFrequency);
  submitted = false;
  error = false;
  errorMessage = '';
  loading = true;

  constructor(
    private formBuilder: FormBuilder,
    private cleaningScheduleService: CleaningScheduleService,
    private householdService: HouseholdService,
    private route: ActivatedRoute,
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
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.scheduleId = Number(idParam);
      this.loadRoommatesAndSchedule();
    } else {
      this.router.navigate(['/cleaning-schedules']);
    }
  }

  loadRoommatesAndSchedule() {
    this.householdService.getMyHousehold().subscribe({
      next: (household) => {
        if (household && household.id) {
          this.householdService.getMembers(household.id).subscribe({
            next: (members) => {
              this.roommates = members;
              this.loadSchedule();
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

  loadSchedule() {
    this.cleaningScheduleService.findOne(this.scheduleId).subscribe({
      next: (schedule) => {
        this.scheduleForm.patchValue({
          title: schedule.title,
          description: schedule.description || '',
          frequency: schedule.frequency,
          startDate: schedule.startDate,
          endDate: schedule.endDate || '',
          assignedToEmail: schedule.assignedToEmail
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching cleaning schedule', err);
        this.error = true;
        this.errorMessage = 'Failed to load the cleaning schedule details.';
        this.loading = false;
      }
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
    const dto: CleaningScheduleUpdateDto = {
      title: formValue.title,
      description: formValue.description || undefined,
      frequency: formValue.frequency,
      startDate: formValue.startDate,
      endDate: formValue.endDate || undefined,
      assignedToEmail: formValue.assignedToEmail
    };

    this.cleaningScheduleService.update(this.scheduleId, dto).subscribe({
      next: () => {
        this.router.navigate(['/cleaning-schedules']);
      },
      error: (err) => {
        console.error('Error updating cleaning schedule', err);
        this.error = true;

        let errorMsg = err.error?.message || 'An error occurred while updating the cleaning schedule. Please try again.';

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
