import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of, throwError } from 'rxjs';

import { HouseholdMembersComponent } from './household-members.component';
import { HouseholdService } from '../../services/household.service';
import { Household } from '../../dtos/household';
import { Roommate } from '../../dtos/roommate';

describe('HouseholdMembersComponent', () => {
  let component: HouseholdMembersComponent;
  let fixture: ComponentFixture<HouseholdMembersComponent>;
  let householdService: jasmine.SpyObj<HouseholdService>;

  beforeEach(async () => {
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', [
      'getMyHousehold',
      'getMembers'
    ]);
    householdService.getMyHousehold.and.returnValue(of(household()));
    householdService.getMembers.and.returnValue(of([member(), member({ id: 2, email: 'main@example.com', role: 'MAIN_TENANT' })]));

    await TestBed.configureTestingModule({
      declarations: [HouseholdMembersComponent],
      providers: [
        { provide: HouseholdService, useValue: householdService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(HouseholdMembersComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load current household and members on init', () => {
    fixture.detectChanges();

    expect(householdService.getMyHousehold).toHaveBeenCalled();
    expect(householdService.getMembers).toHaveBeenCalledOnceWith(1);
    expect(component.household?.name).toBe('Dream Loft Vienna');
    expect(component.members.length).toBe(2);
  });

  it('should display household name and members', () => {
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Dream Loft Vienna');
    expect(fixture.nativeElement.textContent).toContain('Anna Berger');
    expect(fixture.nativeElement.textContent).toContain('friend@example.com');
  });

  it('should handle no-household or forbidden backend error gracefully', () => {
    householdService.getMyHousehold.and.returnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.household).toBeNull();
    expect(component.errorMessage).toBe('Could not load household information.');
    expect(householdService.getMembers).not.toHaveBeenCalled();
  });

  it('should handle members backend error gracefully', () => {
    householdService.getMembers.and.returnValue(throwError(() => ({ status: 500 })));

    fixture.detectChanges();

    expect(component.household?.id).toBe(1);
    expect(component.members).toEqual([]);
    expect(component.errorMessage).toBe('Could not load members.');
  });

  it('should preserve member role and email data returned by the service', () => {
    fixture.detectChanges();

    expect(component.members.map(m => ({ email: m.email, role: m.role }))).toEqual([
      { email: 'friend@example.com', role: 'ROOMMATE' },
      { email: 'main@example.com', role: 'MAIN_TENANT' }
    ]);
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

function member(overrides: Partial<Roommate> = {}): Roommate {
  return {
    id: 1,
    userId: 1,
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'friend@example.com',
    role: 'ROOMMATE',
    joinedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}
