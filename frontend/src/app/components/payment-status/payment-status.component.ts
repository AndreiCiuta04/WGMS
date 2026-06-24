import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BalanceDto } from '../../dtos/expense';
import { PaymentDto, PaymentOverviewDto, PendingSettlementDto } from '../../dtos/payment';
import { AuthService } from '../../services/auth.service';
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
  pendingSettlements: PendingSettlementDto[] = [];
  selectedBalance: BalanceDto | null = null;
  submitted = false;
  isLoading = false;
  isLoadingSettlements = false;
  isSaving = false;
  settlementActionId: string | null = null;
  overviewLoadFailed = false;
  settlementLoadFailed = false;
  settlementOverlayDismissed = false;
  errorMessage = '';
  successMessage = '';
  readonly settlementSwipeThreshold = 90;
  readonly settlementSwipeLimit = 150;
  currentSettlementIndex = 0;
  settlementSwipeOffsets: Record<string, number> = {};
  private swipeStartX: number | null = null;
  private swipedSettlementId: string | null = null;
  private settlingSettlementId: string | null = null;

  constructor(
    private formBuilder: FormBuilder,
    private paymentService: PaymentService,
    private authService: AuthService
  ) {
    this.paymentForm = this.formBuilder.group({
      amount: ['', [Validators.required]],
      date: [this.today(), [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadOverview();
    this.loadPendingSettlements();
  }

  loadOverview(): void {
    this.isLoading = true;
    this.overviewLoadFailed = false;
    this.paymentService.getOverview().subscribe({
      next: overview => {
        this.overview = overview ?? this.emptyOverview();
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: err => {
        this.overview = this.emptyOverview();
        this.overviewLoadFailed = true;
        this.errorMessage = this.extractErrorMessage(err, 'Could not load payment status.');
        this.isLoading = false;
      }
    });
  }

  currentSettlement(): PendingSettlementDto | null {
    return this.pendingSettlements[this.currentSettlementIndex] ?? null;
  }

  loadPendingSettlements(): void {
    this.isLoadingSettlements = true;
    this.settlementLoadFailed = false;
    this.paymentService.getPendingSettlements().subscribe({
      next: settlements => {
        this.pendingSettlements = settlements ?? [];
        this.currentSettlementIndex = 0;
        if (this.pendingSettlements.length === 0) {
          this.settlementOverlayDismissed = false;
        }
        this.isLoadingSettlements = false;
      },
      error: err => {
        this.pendingSettlements = [];
        this.settlementLoadFailed = true;
        this.errorMessage = this.extractErrorMessage(err, 'Could not load settlement approvals.');
        this.isLoadingSettlements = false;
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
        this.loadPendingSettlements();
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

  settlements(): PendingSettlementDto[] {
    return this.pendingSettlements ?? [];
  }

  showSettlementOverlay(): boolean {
    return this.settlements().length > 0 && !this.settlementOverlayDismissed && !this.settlementLoadFailed;
  }

  closeSettlementOverlay(): void {
    this.settlementOverlayDismissed = true;
    this.swipeStartX = null;
    this.swipedSettlementId = null;
    this.settlingSettlementId = null;
    this.settlementSwipeOffsets = {};
  }

  canRecord(balance: BalanceDto): boolean {
    const currentUserEmail = this.authService.getUserEmail();
    return currentUserEmail !== null && currentUserEmail === balance.fromRoommateEmail;
  }

  decideSettlement(settlement: PendingSettlementDto, decision: 'APPROVE' | 'REJECT'): void {
    if (this.settlementActionId || !settlement.canDecide) {
      return;
    }

    this.clearMessages();
    this.settlementActionId = settlement.id;
    const request = decision === 'APPROVE'
      ? this.paymentService.approveSettlement(settlement.id)
      : this.paymentService.rejectSettlement(settlement.id);

    request.subscribe({
      next: () => {
        this.successMessage = decision === 'APPROVE'
          ? 'Settlement approved and payment recorded.'
          : 'Settlement rejected.';
        this.pendingSettlements = this.pendingSettlements.filter(s => s.id !== settlement.id);
        if (this.currentSettlementIndex >= this.pendingSettlements.length) {
          this.currentSettlementIndex = Math.max(0, this.pendingSettlements.length - 1);
        }
        if (this.pendingSettlements.length === 0) {
          this.settlementOverlayDismissed = true;
        }
        this.loadOverview();
      },
      error: err => {
        this.errorMessage = this.extractErrorMessage(err, 'Could not update settlement.');
        this.settlementActionId = null;
        this.resetSettlementSwipe(settlement.id);
      },
      complete: () => this.settlementActionId = null
    });
  }

  startSettlementSwipe(settlement: PendingSettlementDto, event: PointerEvent | MouseEvent): void {
    if (!settlement.canDecide || this.settlementActionId) {
      return;
    }
    if (this.swipedSettlementId !== null && this.swipedSettlementId !== settlement.id) {
      return;
    }
    this.swipeStartX = event.clientX;
    this.swipedSettlementId = settlement.id;
    this.settlingSettlementId = null;
    this.settlementSwipeOffsets[settlement.id] = 0;
    if ('pointerId' in event) {
      (event.currentTarget as HTMLElement | null)?.setPointerCapture?.(event.pointerId);
    }
  }

  moveSettlementSwipe(settlement: PendingSettlementDto, event: PointerEvent | MouseEvent): void {
    if (this.swipedSettlementId !== settlement.id || this.swipeStartX === null) {
      return;
    }

    const deltaX = event.clientX - this.swipeStartX;
    const boundedDeltaX = Math.max(-this.settlementSwipeLimit, Math.min(this.settlementSwipeLimit, deltaX));
    this.settlementSwipeOffsets = {
      ...this.settlementSwipeOffsets,
      [settlement.id]: boundedDeltaX
    };

    if (Math.abs(deltaX) > 8) {
      event.preventDefault();
    }
  }

  finishSettlementSwipe(settlement: PendingSettlementDto, event: PointerEvent | MouseEvent): void {
    if (this.swipedSettlementId !== settlement.id || this.swipeStartX === null) {
      return;
    }

    const deltaX = this.settlementSwipeOffsets[settlement.id] ?? event.clientX - this.swipeStartX;
    this.swipeStartX = null;
    this.swipedSettlementId = null;
    if ('pointerId' in event) {
      (event.currentTarget as HTMLElement | null)?.releasePointerCapture?.(event.pointerId);
    }

    if (Math.abs(deltaX) < this.settlementSwipeThreshold) {
      this.resetSettlementSwipe(settlement.id);
      return;
    }

    this.settlementSwipeOffsets = {
      ...this.settlementSwipeOffsets,
      [settlement.id]: deltaX > 0 ? this.settlementSwipeLimit : -this.settlementSwipeLimit
    };
    this.decideSettlement(settlement, deltaX > 0 ? 'APPROVE' : 'REJECT');
  }

  cancelSettlementSwipe(settlement: PendingSettlementDto): void {
    if (this.swipedSettlementId !== settlement.id) {
      return;
    }

    this.swipeStartX = null;
    this.swipedSettlementId = null;
    this.resetSettlementSwipe(settlement.id);
  }

  settlementSwipeTransform(settlement: PendingSettlementDto): string {
    const offset = this.settlementSwipeOffsets[settlement.id] ?? 0;
    const rotation = offset / 28;
    return `translateX(${offset}px) rotate(${rotation}deg)`;
  }

  settlementSwipeProgress(settlement: PendingSettlementDto): number {
    const offset = Math.abs(this.settlementSwipeOffsets[settlement.id] ?? 0);
    return Math.min(1, offset / this.settlementSwipeThreshold);
  }

  settlementSwipeDirection(settlement: PendingSettlementDto): 'approve' | 'reject' | null {
    const offset = this.settlementSwipeOffsets[settlement.id] ?? 0;
    if (offset >= this.settlementSwipeThreshold) {
      return 'approve';
    }
    if (offset <= -this.settlementSwipeThreshold) {
      return 'reject';
    }
    return null;
  }

  isSettlementSwiping(settlement: PendingSettlementDto): boolean {
    return this.swipedSettlementId === settlement.id;
  }

  isSettlementSettling(settlement: PendingSettlementDto): boolean {
    return this.settlingSettlementId === settlement.id;
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

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private resetSettlementSwipe(settlementId: string): void {
    this.settlingSettlementId = settlementId;
    this.settlementSwipeOffsets = {
      ...this.settlementSwipeOffsets,
      [settlementId]: 0
    };
    window.setTimeout(() => {
      if (this.settlingSettlementId === settlementId) {
        this.settlingSettlementId = null;
      }
    }, 180);
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
