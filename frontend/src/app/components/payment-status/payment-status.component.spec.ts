import { CommonModule } from '@angular/common';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, Subject, throwError } from 'rxjs';
import { LucideAngularModule, CircleCheck, CircleX, Clock, HandCoins, Scale } from 'lucide-angular';
import { PaymentOverviewDto, PendingSettlementDto } from '../../dtos/payment';
import { AuthService } from '../../services/auth.service';
import { PaymentService } from '../../services/payment.service';
import { PaymentStatusComponent } from './payment-status.component';

describe('PaymentStatusComponent', () => {
  let component: PaymentStatusComponent;
  let fixture: ComponentFixture<PaymentStatusComponent>;
  let paymentServiceSpy: jasmine.SpyObj<PaymentService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const emptyOverview: PaymentOverviewDto = {
    openBalances: [],
    payments: [],
    totalIOwe: 0,
    totalOwedToMe: 0
  };

  const populatedOverview: PaymentOverviewDto = {
    openBalances: [
      {
        fromRoommateId: 10,
        fromRoommateName: 'Max Mustermann',
        fromRoommateEmail: 'max@test.com',
        toRoommateId: 11,
        toRoommateName: 'Anna Musterfrau',
        toRoommateEmail: 'anna@test.com',
        amount: 12.5
      },
      {
        fromRoommateId: 12,
        fromRoommateName: 'Long Name With Many Characters',
        fromRoommateEmail: 'long.email.address.for.qa@test.com',
        toRoommateId: 10,
        toRoommateName: 'Max Mustermann',
        toRoommateEmail: 'max@test.com',
        amount: 7.25
      }
    ],
    payments: [
      {
        id: 1,
        fromRoommateId: 10,
        fromRoommateName: 'Max Mustermann',
        fromRoommateEmail: 'max@test.com',
        toRoommateId: 11,
        toRoommateName: 'Anna Musterfrau',
        toRoommateEmail: 'anna@test.com',
        amount: 5,
        date: '2026-05-31',
        createdAt: '2026-05-31T10:00:00'
      },
      {
        id: 2,
        fromRoommateId: 12,
        fromRoommateName: null as any,
        fromRoommateEmail: null as any,
        toRoommateId: 10,
        toRoommateName: 'Max Mustermann',
        toRoommateEmail: 'max@test.com',
        amount: 3.5,
        date: '2026-05-30',
        createdAt: '2026-05-30T10:00:00'
      }
    ],
    totalIOwe: 12.5,
    totalOwedToMe: 7.25
  };

  const pendingSettlements: PendingSettlementDto[] = [
    {
      id: '10-11-1250',
      expenseDescription: 'Groceries',
      fromRoommateId: 10,
      fromRoommateName: 'Max Mustermann',
      fromRoommateEmail: 'max@test.com',
      toRoommateId: 11,
      toRoommateName: 'Anna Musterfrau',
      toRoommateEmail: 'anna@test.com',
      amount: 12.5,
      status: 'PENDING',
      canDecide: true,
      createdAt: null,
      decidedAt: null,
      decidedByName: null
    }
  ];

  beforeEach(async () => {
    paymentServiceSpy = jasmine.createSpyObj('PaymentService', [
      'getOverview',
      'create',
      'getPendingSettlements',
      'approveSettlement',
      'rejectSettlement'
    ]);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserEmail']);

    paymentServiceSpy.getOverview.and.returnValue(of(emptyOverview));
    paymentServiceSpy.create.and.returnValue(of(populatedOverview.payments[0]));
    paymentServiceSpy.getPendingSettlements.and.returnValue(of([]));
    paymentServiceSpy.approveSettlement.and.returnValue(of({ ...pendingSettlements[0], status: 'APPROVED' }));
    paymentServiceSpy.rejectSettlement.and.returnValue(of({ ...pendingSettlements[0], status: 'REJECTED' }));
    authServiceSpy.getUserEmail.and.returnValue('max@test.com');

    await TestBed.configureTestingModule({
      declarations: [PaymentStatusComponent],
      imports: [
        CommonModule,
        ReactiveFormsModule,
        LucideAngularModule.pick({CircleCheck, CircleX, Clock, HandCoins, Scale})
      ],
      providers: [
        { provide: PaymentService, useValue: paymentServiceSpy },
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PaymentStatusComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load payment overview on init', () => {
    fixture.detectChanges();

    expect(paymentServiceSpy.getOverview).toHaveBeenCalled();
    expect(paymentServiceSpy.getPendingSettlements).toHaveBeenCalled();
    expect(component.isLoading).toBeFalse();
  });

  it('should show loading state while overview request is pending', () => {
    const overviewSubject = new Subject<PaymentOverviewDto>();
    paymentServiceSpy.getOverview.and.returnValue(overviewSubject.asObservable());

    component.loadOverview();

    expect(component.isLoading).toBeTrue();

    overviewSubject.next(emptyOverview);
    overviewSubject.complete();
    expect(component.isLoading).toBeFalse();
  });

  it('should show empty state when there are no balances or payments', () => {
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('There are no open balances.');
    expect(text).toContain('No payments recorded yet.');
    expect(fixture.nativeElement.querySelector('[data-cy="settlement-approval-overlay"]')).toBeNull();
    expect(text).not.toContain('Could not load payment status.');
  });

  it('should show an auth error when overview loading is forbidden', () => {
    paymentServiceSpy.getOverview.and.returnValue(throwError(() => ({
      status: 403,
      error: { message: 'Forbidden' }
    })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Forbidden');
    expect(fixture.nativeElement.textContent).toContain('Forbidden');
    expect(component.isLoading).toBeFalse();
  });

  it('should show a load error instead of an empty state for network failures', () => {
    paymentServiceSpy.getOverview.and.returnValue(throwError(() => ({
      status: 0,
      error: {}
    })));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(component.errorMessage).toBe('Could not load payment status.');
    expect(component.overviewLoadFailed).toBeTrue();
    expect(text).toContain('Could not load payment status.');
    expect(text).not.toContain('There are no open balances.');
    expect(text).not.toContain('No payments recorded yet.');
  });

  it('should render multiple balances and payment history records', () => {
    paymentServiceSpy.getOverview.and.returnValue(of(populatedOverview));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Max Mustermann owes Anna Musterfrau');
    expect(text).toContain('Long Name With Many Characters owes Max Mustermann');
    expect(text).toContain('Max Mustermann paid Anna Musterfrau');
    expect(text).toContain('€12.50');
    expect(text).toContain('€7.25');
  });

  it('should render pending settlement approval cards', () => {
    paymentServiceSpy.getPendingSettlements.and.returnValue(of(pendingSettlements));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(fixture.nativeElement.querySelector('[data-cy="settlement-approval-overlay"]')).not.toBeNull();
    expect(text).toContain('Groceries');
    expect(text).toContain('Max Mustermann owes Anna Musterfrau');
    expect(text).toContain('€12.50');
    expect(fixture.nativeElement.querySelector('[data-cy="approve-settlement-button"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('[data-cy="reject-settlement-button"]')).not.toBeNull();
  });

  it('should close the settlement approval overlay with the close button', () => {
    paymentServiceSpy.getPendingSettlements.and.returnValue(of(pendingSettlements));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-cy="settlement-approval-overlay"]')).not.toBeNull();

    const closeButton = fixture.nativeElement.querySelector('[data-cy="close-settlement-overlay"]') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();

    expect(component.settlementOverlayDismissed).toBeTrue();
    expect(fixture.nativeElement.querySelector('[data-cy="settlement-approval-overlay"]')).toBeNull();
  });

  it('should approve a pending settlement via button fallback and reload finance data', () => {
    paymentServiceSpy.getPendingSettlements.and.returnValue(of(pendingSettlements));
    fixture.detectChanges();

    const approveButton = fixture.nativeElement.querySelector('[data-cy="approve-settlement-button"]') as HTMLButtonElement;
    approveButton.click();

    expect(paymentServiceSpy.approveSettlement).toHaveBeenCalledOnceWith('10-11-1250');
    expect(component.successMessage).toBe('Settlement approved and payment recorded.');
    expect(paymentServiceSpy.getOverview).toHaveBeenCalledTimes(2);
    expect(paymentServiceSpy.getPendingSettlements).toHaveBeenCalledTimes(2);
  });

  it('should hide stale settlement cards while refreshed settlement ids load after a decision', () => {
    const staleSecondSettlement: PendingSettlementDto = {
      ...pendingSettlements[0],
      id: '10-12-stale',
      toRoommateId: 12,
      toRoommateName: 'Chris Creditor',
      toRoommateEmail: 'chris@test.com'
    };
    const refreshedSecondSettlement: PendingSettlementDto = {
      ...staleSecondSettlement,
      id: '10-12-refreshed'
    };
    const refreshSubject = new Subject<PendingSettlementDto[]>();
    paymentServiceSpy.getPendingSettlements.and.returnValues(
      of([pendingSettlements[0], staleSecondSettlement]),
      refreshSubject.asObservable()
    );
    fixture.detectChanges();

    component.decideSettlement(pendingSettlements[0], 'APPROVE');

    expect(component.pendingSettlements).toEqual([]);
    expect(component.currentSettlement()).toBeNull();
    expect(component.showSettlementOverlay()).toBeFalse();

    refreshSubject.next([refreshedSecondSettlement]);
    refreshSubject.complete();

    expect(component.currentSettlement()?.id).toBe('10-12-refreshed');
    expect(component.showSettlementOverlay()).toBeTrue();
  });

  it('should reject a pending settlement via button fallback', () => {
    paymentServiceSpy.getPendingSettlements.and.returnValue(of(pendingSettlements));
    fixture.detectChanges();

    const rejectButton = fixture.nativeElement.querySelector('[data-cy="reject-settlement-button"]') as HTMLButtonElement;
    rejectButton.click();

    expect(paymentServiceSpy.rejectSettlement).toHaveBeenCalledOnceWith('10-11-1250');
    expect(component.successMessage).toBe('Settlement rejected.');
  });

  it('should show a readable error when settlement decision fails', () => {
    paymentServiceSpy.approveSettlement.and.returnValue(throwError(() => ({
      error: { message: 'Settlement is no longer pending.' }
    })));

    component.decideSettlement(pendingSettlements[0], 'APPROVE');

    expect(component.errorMessage).toBe('Settlement is no longer pending.');
    expect(component.settlementActionId).toBeNull();
  });

  it('should allow recording only balances from the current user email', () => {
    expect(component.canRecord(populatedOverview.openBalances[0])).toBeTrue();
    expect(component.canRecord(populatedOverview.openBalances[1])).toBeFalse();
  });

  it('should not render undefined, NaN, or object placeholders for nullable backend fields', () => {
    paymentServiceSpy.getOverview.and.returnValue(of(populatedOverview));

    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).not.toContain('undefined');
    expect(text).not.toContain('NaN');
    expect(text).not.toContain('[object Object]');
  });

  it('should select an own balance and create a payment without household id', () => {
    paymentServiceSpy.getOverview.and.returnValue(of(populatedOverview));
    fixture.detectChanges();

    component.selectBalance(populatedOverview.openBalances[0]);
    component.paymentForm.patchValue({amount: '6,50', date: '2026-05-31'});
    component.submit();

    expect(paymentServiceSpy.create).toHaveBeenCalledWith({
      toRoommateId: 11,
      amount: 6.5,
      date: '2026-05-31'
    });
    expect((paymentServiceSpy.create.calls.mostRecent().args[0] as any).householdId).toBeUndefined();
  });

  it('should reject invalid and excessive payment amounts before calling the backend', () => {
    component.selectBalance(populatedOverview.openBalances[0]);

    component.paymentForm.patchValue({amount: '12.345', date: '2026-05-31'});
    component.submit();
    expect(component.errorMessage).toBe('Enter a positive amount with at most two decimal places.');

    component.paymentForm.patchValue({amount: '12.51', date: '2026-05-31'});
    component.submit();
    expect(component.errorMessage).toBe('Payment cannot exceed the open balance.');
    expect(paymentServiceSpy.create).not.toHaveBeenCalled();
  });

  it('should normalize valid comma and dot amounts and leave invalid values unchanged', () => {
    component.paymentForm.patchValue({amount: '4.5'});
    component.normalizeAmount();
    expect(component.paymentForm.get('amount')?.value).toBe('4,50');

    component.paymentForm.patchValue({amount: 'abc'});
    component.normalizeAmount();
    expect(component.paymentForm.get('amount')?.value).toBe('abc');
  });

  it('should handle pending settlement load failures with fallback text', () => {
    paymentServiceSpy.getPendingSettlements.and.returnValue(throwError(() => ({error: {}})));

    component.loadPendingSettlements();

    expect(component.pendingSettlements).toEqual([]);
    expect(component.settlementLoadFailed).toBeTrue();
    expect(component.errorMessage).toBe('Could not load settlement approvals.');
    expect(component.showSettlementOverlay()).toBeFalse();
  });

  it('should ignore settlement decisions while another action is running or when user cannot decide', () => {
    component.settlementActionId = 'busy';
    component.decideSettlement(pendingSettlements[0], 'APPROVE');
    expect(paymentServiceSpy.approveSettlement).not.toHaveBeenCalled();

    component.settlementActionId = null;
    component.decideSettlement({...pendingSettlements[0], canDecide: false}, 'REJECT');
    expect(paymentServiceSpy.rejectSettlement).not.toHaveBeenCalled();
  });

  it('should approve and reject settlements through swipe thresholds', () => {
    const settlement = pendingSettlements[0];
    const preventDefault = jasmine.createSpy('preventDefault');

    component.startSettlementSwipe(settlement, {clientX: 10} as MouseEvent);
    component.moveSettlementSwipe(settlement, {clientX: 130, preventDefault} as unknown as MouseEvent);
    expect(preventDefault).toHaveBeenCalled();
    expect(component.settlementSwipeDirection(settlement)).toBe('approve');
    expect(component.settlementSwipeTransform(settlement)).toContain('translateX(120px)');
    component.finishSettlementSwipe(settlement, {clientX: 130} as MouseEvent);
    expect(paymentServiceSpy.approveSettlement).toHaveBeenCalledWith(settlement.id);

    paymentServiceSpy.approveSettlement.calls.reset();
    component.pendingSettlements = [settlement];
    component.settlementOverlayDismissed = false;
    component.startSettlementSwipe(settlement, {clientX: 130} as MouseEvent);
    component.moveSettlementSwipe(settlement, {clientX: 0, preventDefault} as unknown as MouseEvent);
    expect(component.settlementSwipeDirection(settlement)).toBe('reject');
    component.finishSettlementSwipe(settlement, {clientX: 0} as MouseEvent);
    expect(paymentServiceSpy.rejectSettlement).toHaveBeenCalledWith(settlement.id);
  });

  it('should reset short and cancelled settlement swipes', fakeAsync(() => {
    const settlement = pendingSettlements[0];

    component.startSettlementSwipe(settlement, {clientX: 10} as MouseEvent);
    component.moveSettlementSwipe(settlement, {clientX: 40, preventDefault: jasmine.createSpy()} as unknown as MouseEvent);
    component.finishSettlementSwipe(settlement, {clientX: 40} as MouseEvent);
    expect(component.settlementSwipeOffsets[settlement.id]).toBe(0);
    expect(component.isSettlementSettling(settlement)).toBeTrue();
    tick(181);
    expect(component.isSettlementSettling(settlement)).toBeFalse();

    component.startSettlementSwipe(settlement, {clientX: 10} as MouseEvent);
    component.cancelSettlementSwipe(settlement);
    expect(component.isSettlementSwiping(settlement)).toBeFalse();
    expect(component.settlementSwipeOffsets[settlement.id]).toBe(0);
  }));
});
