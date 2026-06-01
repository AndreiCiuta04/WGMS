import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { HouseholdInvitationsComponent } from './household-invitations.component';
import { HouseholdService } from '../../services/household.service';
import { InvitationService } from '../../services/invitation.service';
import { Household } from '../../dtos/household';
import { Invitation } from '../../dtos/invitation';

describe('HouseholdInvitationsComponent', () => {
  let component: HouseholdInvitationsComponent;
  let fixture: ComponentFixture<HouseholdInvitationsComponent>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let invitationService: jasmine.SpyObj<InvitationService>;

  beforeEach(async () => {
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold']);
    invitationService = jasmine.createSpyObj<InvitationService>('InvitationService', [
      'getInvitations',
      'createInvitation',
      'deleteInvitation'
    ]);

    householdService.getMyHousehold.and.returnValue(of(household()));
    invitationService.getInvitations.and.returnValue(of([invitation()]));
    invitationService.createInvitation.and.returnValue(of(invitation({ id: 2, inviteUrl: 'http://localhost/#/invite/new-token' })));
    invitationService.deleteInvitation.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      declarations: [HouseholdInvitationsComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: HouseholdService, useValue: householdService },
        { provide: InvitationService, useValue: invitationService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(HouseholdInvitationsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load current household and existing invitations on init', () => {
    fixture.detectChanges();

    expect(householdService.getMyHousehold).toHaveBeenCalled();
    expect(invitationService.getInvitations).toHaveBeenCalledOnceWith(1);
    expect(component.household?.name).toBe('Dream Loft Vienna');
    expect(component.invitations).toEqual([invitation()]);
  });

  it('should block create request for invalid invitation email', () => {
    fixture.detectChanges();
    component.form.patchValue({ invitedEmail: 'not-an-email', expiresInHours: 168 });

    component.create();

    expect(component.form.get('invitedEmail')?.errors?.email).toBeTruthy();
    expect(invitationService.createInvitation).not.toHaveBeenCalled();
  });

  it('should call InvitationService for valid invitation email', () => {
    fixture.detectChanges();
    component.form.patchValue({ invitedEmail: 'friend@example.com', expiresInHours: 24 });

    component.create();

    expect(invitationService.createInvitation).toHaveBeenCalledOnceWith(1, {
      invitedEmail: 'friend@example.com',
      expiresInHours: 24
    });
  });

  it('should store returned invite link, reset form, and refresh invitations after successful creation', () => {
    fixture.detectChanges();
    invitationService.getInvitations.calls.reset();
    component.form.patchValue({ invitedEmail: 'friend@example.com', expiresInHours: 24 });

    component.create();

    expect(component.lastCreatedUrl).toBe('http://localhost/#/invite/new-token');
    expect(component.form.value).toEqual({ invitedEmail: '', expiresInHours: 168 });
    expect(invitationService.getInvitations).toHaveBeenCalledOnceWith(1);
  });

  it('should revoke invitation and refresh invitations', () => {
    fixture.detectChanges();
    invitationService.getInvitations.calls.reset();

    component.delete(7);

    expect(invitationService.deleteInvitation).toHaveBeenCalledOnceWith(7);
    expect(invitationService.getInvitations).toHaveBeenCalledOnceWith(1);
  });

  it('should show access denied message when loading invitations is forbidden', () => {
    invitationService.getInvitations.and.returnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Access Denied: Only the Main Tenant can manage invitations.');
  });

  it('should show readable error when invitation creation fails', () => {
    fixture.detectChanges();
    invitationService.createInvitation.and.returnValue(throwError(() => ({ status: 500 })));
    component.form.patchValue({ invitedEmail: 'friend@example.com', expiresInHours: 24 });

    component.create();

    expect(component.errorMessage).toBe('Could not create invitation.');
  });
});

function household(overrides: Partial<Household> = {}): Household {
  return {
    id: 1,
    name: 'Dream Loft Vienna',
    address: 'Main street 1',
    createdAt: '2026-06-01T00:00:00',
    currentUserRole: 'MAIN_TENANT',
    ...overrides
  };
}

function invitation(overrides: Partial<Invitation> = {}): Invitation {
  return {
    id: 1,
    inviteUrl: 'http://localhost/#/invite/token',
    expiresAt: '2026-06-08T00:00:00',
    invitedEmail: 'friend@example.com',
    status: 'ACTIVE',
    ...overrides
  };
}
