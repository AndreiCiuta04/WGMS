import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { InvitationAcceptComponent } from './invitation-accept.component';
import { InvitationService } from '../../services/invitation.service';
import { AuthService } from '../../services/auth.service';
import { InvitationAcceptResponse, InvitationPreview } from '../../dtos/invitation';

describe('InvitationAcceptComponent', () => {
  let component: InvitationAcceptComponent;
  let fixture: ComponentFixture<InvitationAcceptComponent>;
  let invitationService: jasmine.SpyObj<InvitationService>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    invitationService = jasmine.createSpyObj<InvitationService>('InvitationService', [
      'previewInvitation',
      'acceptInvitation'
    ]);
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'isLoggedIn',
      'getUserEmail',
      'registerUser',
      'loginUser'
    ]);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    invitationService.previewInvitation.and.returnValue(of(preview()));
    invitationService.acceptInvitation.and.returnValue(of(acceptResponse()));
    authService.isLoggedIn.and.returnValue(false);
    authService.getUserEmail.and.returnValue(null);
    authService.registerUser.and.returnValue(of('jwt-token'));
    authService.loginUser.and.returnValue(of('jwt-token'));

    await TestBed.configureTestingModule({
      declarations: [InvitationAcceptComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: InvitationService, useValue: invitationService },
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => key === 'token' ? 'invite-token-123' : null
              }
            }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(InvitationAcceptComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    localStorage.removeItem('pendingInviteToken');
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should read token from route params', () => {
    fixture.detectChanges();

    expect(component.token).toBe('invite-token-123');
    expect(invitationService.previewInvitation).toHaveBeenCalledOnceWith('invite-token-123');
  });

  it('should load household preview data for a valid token', () => {
    fixture.detectChanges();

    expect(component.preview?.householdName).toBe('Dream Loft Vienna');
    expect(component.registerForm.value.email).toBe('friend@example.com');
  });

  it('should show readable error for expired invitation preview', () => {
    invitationService.previewInvitation.and.returnValue(throwError(() => ({ status: 410 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('This invitation is no longer valid or has already been used.');
  });

  it('should show readable error for invalid invitation preview', () => {
    invitationService.previewInvitation.and.returnValue(throwError(() => ({ status: 404 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Could not load invitation details.');
  });

  it('should accept invitation for logged-in user and navigate home after success', fakeAsync(() => {
    authService.isLoggedIn.and.returnValue(true);
    authService.getUserEmail.and.returnValue('friend@example.com');
    localStorage.setItem('pendingInviteToken', 'invite-token-123');
    fixture.detectChanges();

    component.accept();
    tick(2000);

    expect(invitationService.acceptInvitation).toHaveBeenCalledOnceWith('invite-token-123');
    expect(localStorage.getItem('pendingInviteToken')).toBeNull();
    expect(component.successMessage).toBe('You have successfully joined the household!');
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  }));

  it('should store pending invitation and navigate to login when accepting while logged out', () => {
    fixture.detectChanges();

    component.accept();

    expect(localStorage.getItem('pendingInviteToken')).toBe('invite-token-123');
    expect(invitationService.acceptInvitation).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledOnceWith(['/login']);
  });

  it('should register and join when register form is valid', () => {
    fixture.detectChanges();
    authService.isLoggedIn.and.returnValue(true);
    component.registerForm.patchValue({
      firstName: 'Anna',
      lastName: 'Berger',
      email: 'friend@example.com',
      password: 'Password123!',
      termsAccepted: true
    });

    component.registerAndAccept();

    expect(authService.registerUser).toHaveBeenCalledOnceWith({
      firstName: 'Anna',
      lastName: 'Berger',
      email: 'friend@example.com',
      password: 'Password123!',
      termsAccepted: true
    });
    expect(invitationService.acceptInvitation).toHaveBeenCalledOnceWith('invite-token-123');
  });

  it('should not register and join when register form is invalid', () => {
    fixture.detectChanges();
    component.registerForm.patchValue({
      firstName: '',
      lastName: 'Berger',
      email: 'friend@example.com',
      password: 'Password123!',
      termsAccepted: true
    });

    component.registerAndAccept();

    expect(component.submitted).toBeTrue();
    expect(authService.registerUser).not.toHaveBeenCalled();
    expect(invitationService.acceptInvitation).not.toHaveBeenCalled();
  });

  it('should show readable error when accepting invitation fails', () => {
    authService.isLoggedIn.and.returnValue(true);
    invitationService.acceptInvitation.and.returnValue(
      throwError(() => ({ error: { message: 'Invitation belongs to another user.' } }))
    );
    fixture.detectChanges();

    component.accept();

    expect(component.errorMessage).toBe('Could not accept invitation. Invitation belongs to another user.');
  });
});

function preview(overrides: Partial<InvitationPreview> = {}): InvitationPreview {
  return {
    householdName: 'Dream Loft Vienna',
    invitedEmail: 'friend@example.com',
    expiresAt: '2026-06-08T00:00:00',
    valid: true,
    ...overrides
  };
}

function acceptResponse(overrides: Partial<InvitationAcceptResponse> = {}): InvitationAcceptResponse {
  return {
    householdId: 1,
    householdName: 'Dream Loft Vienna',
    role: 'ROOMMATE',
    ...overrides
  };
}
