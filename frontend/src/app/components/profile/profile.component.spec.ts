import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { LucideAngularModule, User } from 'lucide-angular';

import { ProfileComponent } from './profile.component';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../dtos/user-profile';

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;
  let authService: jasmine.SpyObj<AuthService>;
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

    await TestBed.configureTestingModule({
      declarations: [ProfileComponent],
      imports: [
        RouterTestingModule,
        LucideAngularModule.pick({ User })
      ],
      providers: [
        { provide: AuthService, useValue: authService }
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

  it('should delete account after confirmation and log out', () => {
    spyOn(window, 'confirm').and.returnValue(true);

    component.deleteAccount();

    expect(authService.anonymizeCurrentUser).toHaveBeenCalled();
    expect(authService.logoutUser).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should request my data export', () => {
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:url');
    spyOn(window.URL, 'revokeObjectURL');
    spyOn(HTMLAnchorElement.prototype, 'click');

    component.exportMyData();

    expect(authService.getMyDataExport).toHaveBeenCalled();
    expect(component.exportMessage).toBe('Data export generated.');
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
