import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserRegistration } from '../../dtos/user-registration';

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

  constructor(private formBuilder: UntypedFormBuilder, private authService: AuthService, private router: Router) {
    this.registerForm = this.formBuilder.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$')]]
    });
  }

  ngOnInit(): void {
  }

  registerUser() {
    this.submitted = true;
    if (this.registerForm.valid) {
      const userRegistration: UserRegistration = {
        email: this.registerForm.controls.email.value,
        password: this.registerForm.controls.password.value,
        firstName: this.registerForm.controls.firstName.value,
        lastName: this.registerForm.controls.lastName.value
      };
      this.authService.registerUser(userRegistration).subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: error => {
          console.log('Could not register due to:');
          console.log(error);
          this.error = true;
          if (error.status === 409) {
            this.errorMessage = 'Email is already registered.';
          } else if (error.status === 404) {
            this.errorMessage = 'Registration endpoint not found. Did you restart the backend?';
          } else if (error.error && typeof error.error === 'object') {
            if (error.error.error) {
              this.errorMessage = error.error.error;
            } else if (error.error.detail) {
              this.errorMessage = error.error.detail;
            } else {
              this.errorMessage = JSON.stringify(error.error);
            }
          } else if (error.error && typeof error.error === 'string') {
            this.errorMessage = error.error;
          } else {
            this.errorMessage = 'Unknown error occurred. Please try again.';
          }
        }
      });
    } else {
      console.log('Invalid input');
    }
  }

  vanishError() {
    this.error = false;
  }

}
