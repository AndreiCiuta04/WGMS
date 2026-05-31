import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { switchMap } from 'rxjs';
import { BalanceDto } from '../../dtos/expense';
import { PaymentDto, PaymentOverviewDto } from '../../dtos/payment';
import { Roommate } from '../../dtos/roommate';
import { AuthService } from '../../services/auth.service';
import { HouseholdService } from '../../services/household.service';
import { PaymentService } from '../../services/payment.service';

@Component({
  selector: 'app-payment-status',
  templateUrl: './payment-status.component.html',
  styleUrls: ['./payment-status.component.scss'],
  standalone: false
})
export class PaymentStatusComponent implements OnInit {

  paymentForm: FormGroup;
  overview: PaymentOverviewDto = this.emptyOverview();
  currentRoommate: Roommate | null = null;
  selectedBalance: BalanceDto | null = null;
  submitted = false;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private paymentService: PaymentService,
    private householdService: HouseholdService,
    private authService: AuthService
  ) {
    this.paymentForm = this.formBuilder.group({
      amount: ['', [Validators.required]],
      date: [this.today(), [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadHouseholdContext();
    this.loadOverview();
  }

  loadHouseholdContext(): void {
    this.householdService.getMyHousehold().pipe(
      switchMap(household => this.householdService.getMembers(household.id))
    ).subscribe({
      next: members => this.currentRoommate = this.findCurrentRoommate(members),
      error: err => this.errorMessage = this.extractErrorMessage(err, 'You need to be part of a WG before recording payments.')
    });
  }

  loadOverview(): void {
    this.isLoading = true;
    this.paymentService.getOverview().subscribe({
      next: overview => {
        this.overview = overview ?? this.emptyOverview();
        this.isLoading = false;
      },
      error: err => {
        this.overview = this.emptyOverview();
        this.errorMessage = this.shouldShowOverviewError(err)
          ? this.extractErrorMessage(err, 'Could not load payment status.')
          : '';
        this.isLoading = false;
      }
    });
  }

  selectBalance(balance: BalanceDto): void {
    this.clearMessages();
    this.submitted = false;
    this.selectedBalance = balance;
    this.paymentForm.reset({
      amount: this.formatAmount(balance.amount),
      date: this.today()
    });
  }

  cancelPayment(): void {
    this.selectedBalance = null;
    this.submitted = false;
    this.paymentForm.reset({
      amount: '',
      date: this.today()
    });
  }

  submit(): void {
    if (this.isSaving || !this.selectedBalance) {
      return;
    }

    this.submitted = true;
    this.clearMessages();

    if (this.paymentForm.invalid) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    const amount = this.parseAmount(this.paymentForm.get('amount')?.value);
    if (amount === null || amount <= 0) {
      this.errorMessage = 'Enter a positive amount.';
      return;
    }

    if (amount > this.selectedBalance.amount) {
      this.errorMessage = 'Payment cannot exceed the open balance.';
      return;
    }

    this.isSaving = true;
    this.paymentService.create({
      toRoommateId: this.selectedBalance.toRoommateId,
      amount,
      date: this.paymentForm.get('date')?.value
    }).subscribe({
      next: () => {
        this.successMessage = 'Payment recorded.';
        this.cancelPayment();
        this.loadOverview();
      },
      error: err => {
        this.errorMessage = this.extractErrorMessage(err, 'Could not record payment.');
        this.isSaving = false;
      },
      complete: () => this.isSaving = false
    });
  }

  normalizeAmount(): void {
    const amount = this.parseAmount(this.paymentForm.get('amount')?.value);
    if (amount !== null) {
      this.paymentForm.patchValue({amount: this.formatAmount(amount)});
    }
  }

  openBalances(): BalanceDto[] {
    return this.overview.openBalances ?? [];
  }

  payments(): PaymentDto[] {
    return this.overview.payments ?? [];
  }

  canRecord(balance: BalanceDto): boolean {
    return this.currentRoommate?.id === balance.fromRoommateId;
  }

  totalIOwe(): number {
    return this.overview.totalIOwe ?? 0;
  }

  totalOwedToMe(): number {
    return this.overview.totalOwedToMe ?? 0;
  }

  private emptyOverview(): PaymentOverviewDto {
    return {
      openBalances: [],
      payments: [],
      totalIOwe: 0,
      totalOwedToMe: 0
    };
  }

  private shouldShowOverviewError(err: any): boolean {
    return err?.status === 401 || err?.status === 403;
  }

  private findCurrentRoommate(roommates: Roommate[]): Roommate | null {
    const email = this.authService.getUserEmail();
    return roommates.find(roommate => roommate.email === email) ?? null;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private extractErrorMessage(err: any, fallback: string): string {
    if (typeof err?.error === 'string' && err.error.trim()) {
      return err.error;
    }
    if (err?.error?.message) {
      return err.error.message;
    }
    return fallback;
  }

  private today(): string {
    return this.toLocalIsoDate(new Date());
  }

  private parseAmount(value: string | number | null | undefined): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    const normalized = String(value).replace(',', '.');
    const parsed = Number(normalized);
    if (!Number.isFinite(parsed)) {
      return null;
    }
    return Math.round(parsed * 100) / 100;
  }

  private formatAmount(value: number): string {
    return value.toFixed(2).replace('.', ',');
  }

  private toLocalIsoDate(date: Date): string {
    const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return localDate.toISOString().slice(0, 10);
  }
}
