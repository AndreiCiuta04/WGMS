import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['loginUser']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
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
});
