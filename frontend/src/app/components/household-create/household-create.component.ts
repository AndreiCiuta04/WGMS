import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HouseholdService } from '../../services/household.service';

@Component({
  selector: 'app-household-create',
  templateUrl: './household-create.component.html',
  styleUrls: ['./household-create.component.scss'],
  standalone: false
})
export class HouseholdCreateComponent implements OnInit {

  form: UntypedFormGroup;
  errorMessage: string = '';

  constructor(
    private fb: UntypedFormBuilder,
    private householdService: HouseholdService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(120)]],
      address: ['', [Validators.maxLength(255)]]
    });
  }

  ngOnInit(): void {
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.householdService.createHousehold(this.form.value).subscribe({
      next: () => this.router.navigate(['/']),
      error: err => {
        console.error('Error creating household', err);
        if (typeof err.error === 'object' && err.error.message) {
          this.errorMessage = err.error.message;
        } else {
          this.errorMessage = 'An error occurred while creating the household.';
        }
      }
    });
  }

}
