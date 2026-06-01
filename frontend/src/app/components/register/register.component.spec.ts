import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['registerUser']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('pendingInviteToken');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should require first name, last name, email, and password', () => {
    component.registerUser();

    expect(component.submitted).toBeTrue();
    expect(component.registerForm.controls.firstName.errors?.required).toBeTruthy();
    expect(component.registerForm.controls.lastName.errors?.required).toBeTruthy();
    expect(component.registerForm.controls.email.errors?.required).toBeTruthy();
    expect(component.registerForm.controls.password.errors?.required).toBeTruthy();
    expect(authServiceSpy.registerUser).not.toHaveBeenCalled();
  });

  it('should reject invalid email format', () => {
    component.registerForm.patchValue({
      firstName: 'Anna',
      lastName: 'Berger',
      email: 'not-an-email',
      password: 'Password123!'
    });

    component.registerUser();

    expect(component.registerForm.controls.email.errors?.email).toBeTruthy();
    expect(authServiceSpy.registerUser).not.toHaveBeenCalled();
  });

  it('should reject weak passwords', () => {
    component.registerForm.patchValue(validRegistration({ password: 'Aa1!' }));

    component.registerUser();

    expect(component.registerForm.controls.password.errors?.minlength).toBeTruthy();
    expect(authServiceSpy.registerUser).not.toHaveBeenCalled();

    component.registerForm.patchValue(validRegistration({ password: 'Password1' }));

    component.registerUser();

    expect(component.registerForm.controls.password.errors?.pattern).toBeTruthy();
    expect(authServiceSpy.registerUser).not.toHaveBeenCalled();
  });

  it('should call AuthService and navigate home after successful registration', () => {
    authServiceSpy.registerUser.and.returnValue(of('jwt-token'));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(authServiceSpy.registerUser).toHaveBeenCalledOnceWith({
      email: 'anna@example.com',
      password: 'Password123!',
      firstName: 'Anna',
      lastName: 'Berger'
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should redirect to pending invitation after successful registration', () => {
    localStorage.setItem('pendingInviteToken', 'invite-token-123');
    authServiceSpy.registerUser.and.returnValue(of('jwt-token'));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/invite', 'invite-token-123']);
  });

  it('should show duplicate email message when registration returns conflict', () => {
    authServiceSpy.registerUser.and.returnValue(throwError(() => ({ status: 409 })));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Email is already registered.');
  });

  it('should show a readable error message for generic backend errors', () => {
    authServiceSpy.registerUser.and.returnValue(
      throwError(() => ({ status: 500, error: { detail: 'Registration service unavailable.' } }))
    );
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Registration service unavailable.');
  });
});

function validRegistration(overrides: Partial<{
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}> = {}) {
  return {
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'anna@example.com',
    password: 'Password123!',
    ...overrides
  };
}
