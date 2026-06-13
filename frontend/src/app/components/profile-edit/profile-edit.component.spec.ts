import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

import { ProfileEditComponent } from './profile-edit.component';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { UserProfile } from '../../dtos/user-profile';

describe('ProfileEditComponent', () => {
  let fixture: ComponentFixture<ProfileEditComponent>;
  let component: ProfileEditComponent;
  let authService: jasmine.SpyObj<AuthService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;
  let router: Router;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'getCurrentUserProfile',
      'updateCurrentUserProfile',
      'leaveCurrentHousehold',
      'logoutUser'
    ]);
    authService.getCurrentUserProfile.and.returnValue(of(profile()));
    authService.updateCurrentUserProfile.and.returnValue(of(profile({ firstName: 'Updated', lastName: 'User' })));
    authService.leaveCurrentHousehold.and.returnValue(of(profile({
      householdId: null,
      householdName: null,
      roommateId: null
    })));

    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      declarations: [ProfileEditComponent],
      imports: [FormsModule, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: ConfirmDialogService, useValue: confirmDialog }
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(ProfileEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load only editable profile fields', () => {
    expect(component.email).toBe('anna@example.com');
    expect(component.firstName).toBe('Anna');
    expect(component.lastName).toBe('Berger');
  });

  it('should save editable profile fields and show success state', () => {
    component.firstName = 'Updated';
    component.lastName = 'User';

    component.saveProfile();

    expect(authService.updateCurrentUserProfile).toHaveBeenCalledWith({
      email: 'anna@example.com',
      firstName: 'Updated',
      lastName: 'User'
    });
    expect(router.navigate).not.toHaveBeenCalled();
    expect(component.successMessage).toBe('Profile changes saved.');
    expect(component.saving).toBeFalse();
  });

  it('should log out after saving a changed email', () => {
    authService.updateCurrentUserProfile.and.returnValue(of(profile({ email: 'new@example.com' })));
    component.email = 'new@example.com';

    component.saveProfile();

    expect(authService.logoutUser).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should reject blank editable fields client-side', () => {
    component.email = ' ';
    component.firstName = 'Anna';
    component.lastName = 'User';

    component.saveProfile();

    expect(authService.updateCurrentUserProfile).not.toHaveBeenCalled();
    expect(component.submitted).toBeTrue();
  });

  it('should leave the current household after confirmation', fakeAsync(() => {
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    component.leaveHousehold();
    tick();

    expect(authService.leaveCurrentHousehold).toHaveBeenCalled();
    expect(component.profile?.householdId).toBeNull();
    expect(component.successMessage).toBe('You left your current WG.');
  }));

  it('should show an error when profile loading fails', () => {
    authService.getCurrentUserProfile.and.returnValue(throwError(() => new Error('failed')));

    fixture = TestBed.createComponent(ProfileEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.errorMessage).toBe('Could not load your editable profile information.');
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
