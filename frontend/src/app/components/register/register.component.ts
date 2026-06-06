import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserRegistration } from '../../dtos/user-registration';
import { RegisterDraftService } from '../../services/register-draft.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
  standalone: false
})
export class RegisterComponent implements OnInit {

  registerForm: UntypedFormGroup;
  submitted = false;
  error = false;
  errorMessage = '';

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: AuthService,
    private router: Router,
    private registerDraftService: RegisterDraftService
  ) {
    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$')]],
      termsAccepted: [false, [Validators.requiredTrue]]
    });
  }

  ngOnInit(): void {
    const draft = this.registerDraftService.load();
    if (draft) {
      this.registerForm.patchValue(draft);
    }
  }

  registerUser() {
    this.submitted = true;
    if (this.registerForm.valid) {
      const userRegistration: UserRegistration = {
        email: this.registerForm.controls.email.value,
        password: this.registerForm.controls.password.value,
        firstName: this.registerForm.controls.firstName.value,
        lastName: this.registerForm.controls.lastName.value,
        termsAccepted: this.registerForm.controls.termsAccepted.value
      };
      this.authService.registerUser(userRegistration).subscribe({
        next: () => {
          this.registerDraftService.clear();
          const pendingToken = localStorage.getItem('pendingInviteToken');
          if (pendingToken) {
            this.router.navigate(['/invite', pendingToken]);
          } else {
            this.router.navigate(['/']);
          }
        },
        error: error => {
          console.log('Could not register due to:');
          console.log(error);
          this.error = true;
          if (error.status === 409) {
            this.errorMessage = 'Email is already registered.';
          } else if (error.status === 404) {
            this.errorMessage = 'Registration endpoint not found. Did you restart the backend?';
          } else {
            this.errorMessage = this.extractRegistrationError(error);
          }
        }
      });
    } else {
      console.log('Invalid input');
    }
  }

  saveDraft(): void {
    this.registerDraftService.save({
      firstName: this.registerForm.controls.firstName.value || '',
      lastName: this.registerForm.controls.lastName.value || '',
      email: this.registerForm.controls.email.value || '',
      password: this.registerForm.controls.password.value || '',
      termsAccepted: !!this.registerForm.controls.termsAccepted.value
    });
  }

  vanishError() {
    this.error = false;
  }

  private extractRegistrationError(error: any): string {
    const responseBody = error?.error;

    if (typeof responseBody === 'string' && responseBody.trim().length > 0) {
      return responseBody;
    }

    if (responseBody && typeof responseBody === 'object') {
      const validationErrors = responseBody['Validation errors'];
      if (Array.isArray(validationErrors) && validationErrors.length > 0) {
        return validationErrors.join(' ');
      }

      if (responseBody.error) {
        return responseBody.error;
      }

      if (responseBody.detail) {
        return responseBody.detail;
      }

      if (responseBody.message) {
        return responseBody.message;
      }
    }

    return 'Unknown error occurred. Please try again.';
  }

}
