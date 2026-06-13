import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { InvitationService } from '../../services/invitation.service';
import { AuthService } from '../../services/auth.service';
import { InvitationPreview } from '../../dtos/invitation';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserRegistration } from '../../dtos/user-registration';
import { AuthRequest } from '../../dtos/auth-request';

@Component({
  selector: 'app-invitation-accept',
  templateUrl: './invitation-accept.component.html',
  styleUrls: ['./invitation-accept.component.scss'],
  standalone: false
})
export class InvitationAcceptComponent implements OnInit {

  token: string = '';
  preview: InvitationPreview | null = null;
  errorMessage: string = '';
  successMessage: string = '';
  
  // Registration form
  registerForm: FormGroup;
  loginForm: FormGroup;
  showLogin: boolean = false;
  submitted: boolean = false;
  currentUserEmail: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private invitationService: InvitationService,
    public authService: AuthService,
    private fb: FormBuilder
  ) {
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[!@#$%^&*])/) ]],
      termsAccepted: [false, Validators.requiredTrue]
    });

    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.paramMap.get('token')!;

    this.invitationService.previewInvitation(this.token).subscribe({
      next: preview => {
        this.preview = preview;
        if (this.authService.isLoggedIn()) {
          this.currentUserEmail = this.authService.getUserEmail();
        } else if (preview.invitedEmail) {
          this.registerForm.patchValue({ email: preview.invitedEmail });
        }
      },
      error: err => {
        if (err.status === 410) {
          this.errorMessage = 'This invitation is no longer valid or has already been used.';
        } else {
          this.errorMessage = 'Could not load invitation details.';
        }
      }
    });
  }

  toggleForm() {
    this.showLogin = !this.showLogin;
    this.errorMessage = '';
  }

  isEmailMismatch(): boolean {
    return !!(this.preview?.invitedEmail && 
              this.currentUserEmail && 
              this.preview.invitedEmail.toLowerCase() !== this.currentUserEmail.toLowerCase());
  }

  accept(): void {
    if (!this.authService.isLoggedIn()) {
      localStorage.setItem('pendingInviteToken', this.token);
      this.router.navigate(['/login']);
      return;
    }

    this.invitationService.acceptInvitation(this.token).subscribe({
      next: () => {
        localStorage.removeItem('pendingInviteToken');
        this.successMessage = 'You have successfully joined the household!';
        setTimeout(() => this.router.navigate(['/']), 2000);
      },
      error: err => {
        this.errorMessage = 'Could not accept invitation. ' + (err.error?.message || '');
      }
    });
  }

  registerAndAccept(): void {
    this.submitted = true;
    if (this.registerForm.invalid) return;

    const registration: UserRegistration = this.registerForm.value;
    this.authService.registerUser(registration).subscribe({
      next: () => this.accept(),
      error: err => this.errorMessage = err.error?.message || 'Registration failed.'
    });
  }

  loginAndAccept(): void {
    this.submitted = true;
    if (this.loginForm.invalid) return;

    const login: AuthRequest = new AuthRequest(this.loginForm.value.email, this.loginForm.value.password);
    this.authService.loginUser(login).subscribe({
      next: () => {
        this.currentUserEmail = login.email;
        this.accept();
      },
      error: err => this.errorMessage = 'Login failed. Please check your credentials.'
    });
  }

}
