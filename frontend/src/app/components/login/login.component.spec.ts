import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['loginUser']);
    notificationServiceSpy = jasmine.createSpyObj<NotificationService>('NotificationService', ['verifyUser']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [FormsModule, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('pendingInviteToken');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should require email and password', () => {
    component.loginUser();

    expect(component.submitted).toBeTrue();
    expect(component.loginForm.controls.email.errors?.required).toBeTruthy();
    expect(component.loginForm.controls.password.errors?.required).toBeTruthy();
    expect(authServiceSpy.loginUser).not.toHaveBeenCalled();
  });

  it('should reject invalid email format', () => {
    component.loginForm.patchValue({
      email: 'not-an-email',
      password: 'Password123!'
    });

    component.loginUser();

    expect(component.loginForm.controls.email.errors?.email).toBeTruthy();
    expect(authServiceSpy.loginUser).not.toHaveBeenCalled();
  });

  it('should call AuthService and navigate home after successful login', () => {
    authServiceSpy.loginUser.and.returnValue(of('jwt-token'));

    component.loginForm.patchValue({
      email: 'anna@example.com',
      password: 'Password123!'
    });

    component.loginUser();

    expect(authServiceSpy.loginUser).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should redirect to pending invitation after successful login', () => {
    localStorage.setItem('pendingInviteToken', 'invite-token-123');
    authServiceSpy.loginUser.and.returnValue(of('jwt-token'));

    component.loginForm.patchValue({
      email: 'anna@example.com',
      password: 'Password123!'
    });

    component.loginUser();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/invite', 'invite-token-123']);
  });

  it('should show an error message when login fails', () => {
    authServiceSpy.loginUser.and.returnValue(
      throwError(() => ({ error: 'Invalid credentials' }))
    );

    component.loginForm.patchValue({
      email: 'anna@example.com',
      password: 'Password123!'
    });

    component.loginUser();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Invalid credentials');
  });

  it('should extract object error messages and allow the alert to be dismissed', () => {
    authServiceSpy.loginUser.and.returnValue(
      throwError(() => ({ error: { error: 'Locked account' } }))
    );

    component.loginForm.patchValue({
      email: 'anna@example.com',
      password: 'Password123!'
    });

    component.loginUser();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Locked account');

    component.vanishError();

    expect(component.error).toBeFalse();
  });

  it('should show verification form when login requires email verification', () => {
    authServiceSpy.loginUser.and.returnValue(
      throwError(() => ({ status: 403, error: { message: 'VERIFICATION_REQUIRED' } }))
    );

    component.loginForm.patchValue({
      email: 'verify@example.com',
      password: 'Password123!'
    });

    component.loginUser();

    expect(component.showVerification).toBeTrue();
    expect(component.verificationEmail).toBe('verify@example.com');
    expect(component.error).toBeFalse();
  });

  it('should verify the email code and navigate home', () => {
    notificationServiceSpy.verifyUser.and.returnValue(of('jwt-token'));
    component.verificationEmail = 'verify@example.com';
    component.verificationCode = '123456';

    component.submitVerificationCode();

    expect(notificationServiceSpy.verifyUser).toHaveBeenCalledOnceWith({
      email: 'verify@example.com',
      code: '123456'
    });
    expect(component.verificationSubmitting).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should return to a pending invite after successful verification', () => {
    localStorage.setItem('pendingInviteToken', 'pending-123');
    notificationServiceSpy.verifyUser.and.returnValue(of('jwt-token'));

    component.submitVerificationCode();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/invite', 'pending-123']);
  });

  it('should show readable verification errors', () => {
    notificationServiceSpy.verifyUser.and.returnValue(throwError(() => ({ status: 400 })));

    component.submitVerificationCode();

    expect(component.verificationSubmitting).toBeFalse();
    expect(component.verificationError).toBe('Invalid or expired verification code.');

    notificationServiceSpy.verifyUser.and.returnValue(throwError(() => ({ status: 500 })));

    component.submitVerificationCode();

    expect(component.verificationError).toBe('An error occurred during verification. Please try again.');
  });
});
