import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, Subject, throwError } from 'rxjs';
import { LucideAngularModule, CircleCheck, Clock, HandCoins, Scale } from 'lucide-angular';
import { PaymentOverviewDto } from '../../dtos/payment';
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

  beforeEach(async () => {
    paymentServiceSpy = jasmine.createSpyObj('PaymentService', ['getOverview', 'create']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['getUserEmail']);

    paymentServiceSpy.getOverview.and.returnValue(of(emptyOverview));
    paymentServiceSpy.create.and.returnValue(of(populatedOverview.payments[0]));
    authServiceSpy.getUserEmail.and.returnValue('max@test.com');

    await TestBed.configureTestingModule({
      declarations: [PaymentStatusComponent],
      imports: [
        CommonModule,
        ReactiveFormsModule,
        LucideAngularModule.pick({CircleCheck, Clock, HandCoins, Scale})
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
});
