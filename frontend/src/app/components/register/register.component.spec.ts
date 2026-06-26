import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../services/auth.service';
import { RegisterDraftService } from '../../services/register-draft.service';
import { NotificationService } from '../../services/notification.service';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let registerDraftService: RegisterDraftService;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['registerUser']);
    notificationServiceSpy = jasmine.createSpyObj<NotificationService>('NotificationService', ['verifyUser']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);
    notificationServiceSpy.verifyUser.and.returnValue(of('jwt-token'));

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
      imports: [FormsModule, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
        { provide: Router, useValue: routerSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    registerDraftService = TestBed.inject(RegisterDraftService);
    registerDraftService.clear();
    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem('pendingInviteToken');
    registerDraftService.clear();
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
    expect(component.registerForm.controls.termsAccepted.errors?.required).toBeTruthy();
    expect(authServiceSpy.registerUser).not.toHaveBeenCalled();
  });

  it('should have terms checkbox initially unchecked', () => {
    expect(component.registerForm.controls.termsAccepted.value).toBeFalse();
    expect(component.registerForm.invalid).toBeTrue();
  });

  it('should render separate terms and privacy acknowledgement links', () => {
    const element: HTMLElement = fixture.nativeElement;
    const termsLink = element.querySelector('[data-cy="registration-terms-link"]');
    const privacyLink = element.querySelector('[data-cy="registration-privacy-link"]');

    expect(element.querySelector('[data-cy="registration-terms-checkbox"]')).toBeTruthy();
    expect(element.querySelector('[data-cy="registration-submit"]')).toBeTruthy();
    expect(termsLink?.getAttribute('routerlink')).toBe('/terms');
    expect(privacyLink?.getAttribute('routerlink')).toBe('/privacy');
    expect(termsLink?.getAttribute('routerlink')).not.toBe(privacyLink?.getAttribute('routerlink'));
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
    registerDraftService.save(validRegistration({ email: 'draft@example.com' }));
    authServiceSpy.registerUser.and.returnValue(of('jwt-token'));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(authServiceSpy.registerUser).toHaveBeenCalledOnceWith({
      email: 'anna@example.com',
      password: 'Password123!',
      firstName: 'Anna',
      lastName: 'Berger',
      termsAccepted: true
    });
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    expect(registerDraftService.load()).toBeNull();
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

  it('should switch to verification when backend requires an email code', () => {
    authServiceSpy.registerUser.and.returnValue(throwError(() => ({
      status: 403,
      error: { message: 'VERIFICATION_REQUIRED' }
    })));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.showVerification).toBeTrue();
    expect(component.verificationEmail).toBe('anna@example.com');
    expect(component.error).toBeFalse();
  });

  it('should show a restart hint when the registration endpoint is missing', () => {
    authServiceSpy.registerUser.and.returnValue(throwError(() => ({ status: 404 })));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Registration endpoint not found. Did you restart the backend?');
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

  it('should show raw string and message backend errors', () => {
    authServiceSpy.registerUser.and.returnValue(throwError(() => ({ status: 400, error: 'Raw backend problem.' })));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.errorMessage).toBe('Raw backend problem.');

    authServiceSpy.registerUser.and.returnValue(throwError(() => ({ status: 400, error: { message: 'Message problem.' } })));
    component.registerUser();

    expect(component.errorMessage).toBe('Message problem.');
  });

  it('should show object error and unknown fallback registration messages', () => {
    authServiceSpy.registerUser.and.returnValue(throwError(() => ({ status: 400, error: { error: 'Nested error problem.' } })));
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.errorMessage).toBe('Nested error problem.');

    authServiceSpy.registerUser.and.returnValue(throwError(() => ({ status: 400, error: {} })));
    component.registerUser();

    expect(component.errorMessage).toBe('Unknown error occurred. Please try again.');
  });

  it('should show validation errors returned by the backend', () => {
    authServiceSpy.registerUser.and.returnValue(
      throwError(() => ({ status: 400, error: { 'Validation errors': ['Terms must be accepted.', 'Password is too weak.'] } }))
    );
    component.registerForm.patchValue(validRegistration());

    component.registerUser();

    expect(component.error).toBeTrue();
    expect(component.errorMessage).toBe('Terms must be accepted. Password is too weak.');
  });

  it('should verify registration code and navigate home', () => {
    component.verificationEmail = 'anna@example.com';
    component.verificationCode = '123456';

    component.submitVerificationCode();

    expect(notificationServiceSpy.verifyUser).toHaveBeenCalledOnceWith({
      email: 'anna@example.com',
      code: '123456'
    });
    expect(component.verificationSubmitting).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should verify registration code and continue pending invitation', () => {
    localStorage.setItem('pendingInviteToken', 'invite-after-verify');
    component.verificationEmail = 'anna@example.com';
    component.verificationCode = '123456';

    component.submitVerificationCode();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/invite', 'invite-after-verify']);
  });

  it('should show invalid-code and generic verification errors', () => {
    notificationServiceSpy.verifyUser.and.returnValue(throwError(() => ({ status: 400 })));

    component.submitVerificationCode();

    expect(component.verificationSubmitting).toBeFalse();
    expect(component.verificationError).toBe('Invalid or expired verification code.');

    notificationServiceSpy.verifyUser.and.returnValue(throwError(() => ({ status: 500 })));
    component.submitVerificationCode();

    expect(component.verificationError).toBe('An error occurred during verification. Please try again.');
  });

  it('should clear registration errors and save empty drafts safely', () => {
    component.error = true;

    component.vanishError();
    component.saveDraft();

    expect(component.error).toBeFalse();
    expect(registerDraftService.load()).toEqual({
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      termsAccepted: false
    });
  });

  it('should restore register form state after navigating to terms and back', () => {
    const draft = validRegistration({
      firstName: 'Terms',
      lastName: 'Draft',
      email: 'terms.draft@example.com',
      password: 'Terms123!'
    });
    component.registerForm.patchValue(draft);

    clickDraftLink('[data-cy="registration-terms-link"]');

    const restored = createRestoredComponent();
    expect(restored.registerForm.value).toEqual(draft);
  });

  it('should restore register form state after navigating to privacy and back', () => {
    const draft = validRegistration({
      firstName: 'Privacy',
      lastName: 'Draft',
      email: 'privacy.draft@example.com',
      password: 'Privacy123!'
    });
    component.registerForm.patchValue(draft);

    clickDraftLink('[data-cy="registration-privacy-link"]');

    const restored = createRestoredComponent();
    expect(restored.registerForm.value).toEqual(draft);
  });

  function clickDraftLink(selector: string): void {
    const link = fixture.nativeElement.querySelector(selector) as HTMLAnchorElement;
    link.dispatchEvent(new Event('click', { bubbles: true }));
    fixture.detectChanges();
  }

  function createRestoredComponent(): RegisterComponent {
    const restoredFixture = TestBed.createComponent(RegisterComponent);
    restoredFixture.detectChanges();
    return restoredFixture.componentInstance;
  }
});

function validRegistration(overrides: Partial<{
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  termsAccepted: boolean;
}> = {}) {
  return {
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'anna@example.com',
    password: 'Password123!',
    termsAccepted: true,
    ...overrides
  };
}
