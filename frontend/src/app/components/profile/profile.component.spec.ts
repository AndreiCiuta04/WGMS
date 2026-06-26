import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { LucideAngularModule, User } from 'lucide-angular';

import { ProfileComponent } from './profile.component';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { UserProfile } from '../../dtos/user-profile';

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;
  let authService: jasmine.SpyObj<AuthService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;
  let router: Router;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'getCurrentUserProfile',
      'anonymizeCurrentUser',
      'getMyDataExport',
      'logoutUser'
    ]);
    authService.getCurrentUserProfile.and.returnValue(of(profile()));
    authService.anonymizeCurrentUser.and.returnValue(of(undefined));
    authService.getMyDataExport.and.returnValue(of({
      generatedAt: '2026-06-05T12:00:00Z',
      account: {
        id: 1,
        email: 'anna@example.com',
        firstName: 'Anna',
        lastName: 'Berger',
        role: 'USER',
        termsAcceptedAt: null,
        termsVersion: null,
        privacyAcknowledgedAt: null,
        privacyVersion: null
      },
      householdMembership: null,
      tasks: [],
      cleaningSchedules: [],
      expenses: [],
      payments: [],
      shoppingItems: [],
      history: {}
    }));

    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      declarations: [ProfileComponent],
      imports: [
        RouterTestingModule,
        LucideAngularModule.pick({ User })
      ],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ConfirmDialogService, useValue: confirmDialog }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load the current user profile', () => {
    expect(component.profile).toEqual(profile());
    expect(component.getDisplayName()).toBe('Anna Berger');
  });

  it('should show an error when profile loading fails', () => {
    authService.getCurrentUserProfile.and.returnValue(throwError(() => new Error('failed')));

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.profile).toBeNull();
    expect(component.errorMessage).toBe('Could not load your profile information.');
  });

  it('should delete account after confirmation and log out', fakeAsync(() => {
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    component.deleteAccount();
    tick();

    expect(authService.anonymizeCurrentUser).toHaveBeenCalled();
    expect(authService.logoutUser).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('should not delete account when confirmation is cancelled', fakeAsync(() => {
    confirmDialog.confirm.and.returnValue(Promise.resolve(false));

    component.deleteAccount();
    tick();

    expect(authService.anonymizeCurrentUser).not.toHaveBeenCalled();
    expect(component.deleting).toBeFalse();
  }));

  it('should show an error when account deletion fails', fakeAsync(() => {
    authService.anonymizeCurrentUser.and.returnValue(throwError(() => new Error('failed')));

    component.deleteAccount();
    tick();

    expect(component.deleting).toBeFalse();
    expect(component.errorMessage).toBe('Could not delete your account.');
    expect(authService.logoutUser).not.toHaveBeenCalled();
  }));

  it('should request my data export', () => {
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:url');
    spyOn(window.URL, 'revokeObjectURL');
    spyOn(HTMLAnchorElement.prototype, 'click');

    component.exportMyData();

    expect(authService.getMyDataExport).toHaveBeenCalled();
    expect(component.exportMessage).toBe('Data export generated.');
  });

  it('should show an error when data export fails', () => {
    authService.getMyDataExport.and.returnValue(throwError(() => new Error('failed')));

    component.exportMyData();

    expect(component.exporting).toBeFalse();
    expect(component.errorMessage).toBe('Could not export your data.');
  });

  it('should use fallback display names for unloaded and blank profiles', () => {
    component.profile = null;
    expect(component.getDisplayName()).toBe('Your profile');

    component.profile = profile({ firstName: ' ', lastName: ' ' });
    expect(component.getDisplayName()).toBe('');
  });
});

function profile(overrides: Partial<UserProfile> = {}): UserProfile {
  return {
    id: 1,
    email: 'anna@example.com',
    firstName: 'Anna',
    lastName: 'Berger',
    role: 'USER',
    householdId: 2,
    householdName: 'WG Test',
    roommateId: 3,
    ...overrides
  };
}
