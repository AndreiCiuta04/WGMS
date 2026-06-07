import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ExpenseService } from '../../services/expense.service';
import { HouseholdService } from '../../services/household.service';
import { BalanceDto, ExpenseCreateDto, ExpenseDto, ExpenseSplitCreateDto } from '../../dtos/expense';
import { Roommate } from '../../dtos/roommate';
import { ReceiptOcrService } from '../../services/receipt-ocr.service';

interface SplitRow {
  roommate: Roommate;
  selected: boolean;
  amountOwed: string | null;
}

@Component({
  selector: 'app-expenses',
  templateUrl: './expenses.component.html',
  styleUrls: ['./expenses.component.scss'],
  standalone: false
})
export class ExpensesComponent implements OnInit {

  expenseForm: FormGroup;
  billForm: FormGroup;
  expenses: ExpenseDto[] = [];
  balances: BalanceDto[] = [];
  roommates: Roommate[] = [];
  splitRows: SplitRow[] = [];
  currentRoommate: Roommate | null = null;
  editingExpense: ExpenseDto | null = null;
  expensePendingDelete: ExpenseDto | null = null;
  submitted = false;
  billSubmitted = false;
  isSaving = false;
  isPreviewingBill = false;
  isScanningReceipt = false;
  billCalculatorOpen = false;
  selectedReceiptFileName = '';
  errorMessage = '';
  successMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private expenseService: ExpenseService,
    private householdService: HouseholdService,
    private authService: AuthService,
    private receiptOcrService: ReceiptOcrService
  ) {
    this.expenseForm = this.formBuilder.group({
      description: ['', [Validators.required, Validators.maxLength(500)]],
      totalAmount: ['', [Validators.required]],
      date: [this.today(), [Validators.required]]
    });
    this.billForm = this.formBuilder.group({
      description: ['', [Validators.required, Validators.maxLength(500)]],
      totalAmount: ['', [Validators.required]],
      date: [this.today(), [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.loadHouseholdContext();
    this.loadExpenses();
    this.loadBalances();
  }

  loadHouseholdContext(): void {
    this.householdService.getMyHousehold().subscribe({
      next: household => {
        this.householdService.getMembers(household.id).subscribe({
          next: members => {
            this.roommates = members;
            this.currentRoommate = this.findCurrentRoommate(members);
            this.splitRows = members.map(roommate => ({
              roommate,
              selected: this.currentRoommate?.id === roommate.id,
              amountOwed: null
            }));
          },
          error: err => this.errorMessage = this.extractErrorMessage(err, 'Could not load household members.')
        });
      },
      error: err => this.errorMessage = this.extractErrorMessage(err, 'You need to be part of a WG before tracking expenses.')
    });
  }

  loadExpenses(): void {
    this.expenseService.findAll().subscribe({
      next: expenses => this.expenses = expenses,
      error: err => this.errorMessage = this.extractErrorMessage(err, 'Could not load expenses.')
    });
  }

  loadBalances(): void {
    this.expenseService.calculateBalances().subscribe({
      next: balances => this.balances = balances,
      error: err => this.errorMessage = this.extractErrorMessage(err, 'Could not load balances.')
    });
  }

  calculateEqualSplit(): void {
    this.clearMessages();
    const totalAmount = this.parseAmount(this.expenseForm.get('totalAmount')?.value);
    const participantRoommateIds = this.selectedRows().map(row => row.roommate.id);

    if (totalAmount === null || participantRoommateIds.length === 0) {
      this.errorMessage = 'Enter an amount and select at least one participant.';
      return;
    }

    this.expenseService.calculateEqualSplits({ totalAmount, participantRoommateIds }).subscribe({
      next: splits => this.applyCalculatedSplits(splits),
      error: err => this.errorMessage = this.extractErrorMessage(err, 'Could not calculate equal split.')
    });
  }

  toggleBillCalculator(): void {
    this.clearMessages();
    this.billCalculatorOpen = !this.billCalculatorOpen;
    if (this.billCalculatorOpen && !this.billForm.get('date')?.value) {
      this.billForm.patchValue({ date: this.today() });
    }
  }

  previewBillExpense(): void {
    if (this.isPreviewingBill || this.editingExpense) {
      return;
    }

    this.billSubmitted = true;
    this.clearMessages();

    const totalAmount = this.parseAmount(this.billForm.get('totalAmount')?.value);
    const participantRoommateIds = this.selectedRows().map(row => row.roommate.id);

    if (this.billForm.invalid || totalAmount === null || totalAmount <= 0 || participantRoommateIds.length === 0) {
      this.errorMessage = 'Enter bill data and select at least one participant.';
      return;
    }

    this.isPreviewingBill = true;
    this.expenseService.previewBillExpense({
      description: this.billForm.get('description')?.value,
      totalAmount,
      date: this.billForm.get('date')?.value,
      participantRoommateIds
    }).subscribe({
      next: preview => {
        this.expenseForm.patchValue({
          description: preview.description,
          totalAmount: this.formatAmount(preview.totalAmount),
          date: preview.date
        });
        this.applyCalculatedSplits(preview.splits);
        this.successMessage = 'Bill data ready for review.';
        this.billCalculatorOpen = false;
        this.submitted = false;
      },
      error: err => {
        this.errorMessage = this.extractErrorMessage(err, 'Could not prepare bill data.');
        this.isPreviewingBill = false;
      },
      complete: () => this.isPreviewingBill = false
    });
  }

  scanReceiptImage(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.clearMessages();
    this.selectedReceiptFileName = file.name;
    this.isScanningReceipt = true;

    this.receiptOcrService.scanReceipt(file)
      .then(draft => {
        this.billForm.patchValue({
          description: draft.description,
          totalAmount: draft.totalAmount !== null ? this.formatAmount(draft.totalAmount) : '',
          date: draft.date
        });
        this.successMessage = draft.totalAmount !== null
          ? 'Receipt scanned. Review the extracted bill data.'
          : 'Receipt scanned, but no amount was found. Enter the bill total manually.';
      })
      .catch(() => {
        this.errorMessage = 'Could not scan the receipt. Enter the bill data manually.';
      })
      .finally(() => {
        this.isScanningReceipt = false;
        input.value = '';
      });
  }

  resetBillCalculator(): void {
    this.billSubmitted = false;
    this.selectedReceiptFileName = '';
    this.billForm.reset({
      description: '',
      totalAmount: '',
      date: this.today()
    });
  }

  submit(): void {
    if (this.isSaving) {
      return;
    }

    this.submitted = true;
    this.clearMessages();

    if (this.expenseForm.invalid || !this.currentRoommate) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    const payload = this.buildPayload();
    if (!payload) {
      return;
    }

    const request = this.editingExpense
      ? this.expenseService.update(this.editingExpense.id, payload)
      : this.expenseService.create(payload);

    this.isSaving = true;
    request.subscribe({
      next: () => {
        this.successMessage = this.editingExpense ? 'Expense updated.' : 'Expense created.';
        this.resetForm();
        this.loadExpenses();
        this.loadBalances();
      },
      error: err => {
        this.errorMessage = this.extractErrorMessage(err, 'Could not save expense.');
        this.isSaving = false;
      },
      complete: () => this.isSaving = false
    });
  }

  editExpense(expense: ExpenseDto): void {
    this.clearMessages();
    this.editingExpense = expense;
    this.expenseForm.patchValue({
      description: expense.description,
      totalAmount: this.formatAmount(expense.totalAmount),
      date: expense.date
    });

    this.splitRows = this.roommates.map(roommate => {
      const split = expense.splits.find(s => s.roommateId === roommate.id);
      return {
        roommate,
        selected: !!split,
        amountOwed: split ? this.formatAmount(split.amountOwed) : null
      };
    });
  }

  deleteExpense(expense: ExpenseDto): void {
    this.expensePendingDelete = expense;
  }

  cancelDelete(): void {
    this.expensePendingDelete = null;
  }

  confirmDelete(): void {
    if (!this.expensePendingDelete) {
      return;
    }

    const expense = this.expensePendingDelete;
    this.expenseService.delete(expense.id).subscribe({
      next: () => {
        this.successMessage = 'Expense deleted.';
        this.expensePendingDelete = null;
        this.loadExpenses();
        this.loadBalances();
        if (this.editingExpense?.id === expense.id) {
          this.resetForm();
        }
      },
      error: err => this.errorMessage = this.extractErrorMessage(err, 'Could not delete expense.')
    });
  }

  balancesIOwe(): BalanceDto[] {
    if (!this.currentRoommate) {
      return [];
    }
    return this.balances.filter(balance => balance.fromRoommateId === this.currentRoommate?.id);
  }

  isMainTenant(): boolean {
    return this.currentRoommate?.role === 'MAIN_TENANT';
  }

  visibleExpenses(): ExpenseDto[] {
    return this.expenses;
  }

  resetForm(): void {
    this.submitted = false;
    this.editingExpense = null;
    this.expenseForm.reset({
      description: '',
      totalAmount: '',
      date: this.today()
    });
    this.splitRows = this.roommates.map(roommate => ({
      roommate,
      selected: this.currentRoommate?.id === roommate.id,
      amountOwed: null
    }));
    this.resetBillCalculator();
  }

  updateSplitSelection(row: SplitRow, selected: boolean): void {
    if (this.editingExpense) {
      return;
    }
    row.selected = selected;
    if (!selected) {
      row.amountOwed = null;
    }
  }

  normalizeTotalAmount(): void {
    const amount = this.parseAmount(this.expenseForm.get('totalAmount')?.value);
    if (amount !== null) {
      this.expenseForm.patchValue({totalAmount: this.formatAmount(amount)});
    }
  }

  normalizeBillAmount(): void {
    const amount = this.parseAmount(this.billForm.get('totalAmount')?.value);
    if (amount !== null) {
      this.billForm.patchValue({totalAmount: this.formatAmount(amount)});
    }
  }

  normalizeSplitAmount(row: SplitRow): void {
    const amount = this.parseAmount(row.amountOwed);
    if (amount !== null) {
      row.amountOwed = this.formatAmount(amount);
    }
  }

  private buildPayload(): ExpenseCreateDto | null {
    const totalAmount = this.parseAmount(this.expenseForm.get('totalAmount')?.value);
    if (totalAmount === null || totalAmount <= 0) {
      this.errorMessage = 'Enter a positive total amount.';
      return null;
    }

    const splits = this.selectedRows().map(row => ({
      roommateId: row.roommate.id,
      amountOwed: this.parseAmount(row.amountOwed)
    }));

    if (splits.length === 0 || splits.some(split => split.amountOwed === null || split.amountOwed <= 0)) {
      this.errorMessage = 'Select participants and enter a positive amount for every selected split.';
      return null;
    }

    const paidByRoommateId = this.currentRoommate?.id;
    if (!paidByRoommateId || !splits.some(split => split.roommateId === paidByRoommateId)) {
      this.errorMessage = 'You must be included in the splits because you paid the expense.';
      return null;
    }

    return {
      description: this.expenseForm.get('description')?.value,
      totalAmount,
      date: this.expenseForm.get('date')?.value,
      paidByRoommateId,
      splits: splits.map(split => ({
        roommateId: split.roommateId,
        amountOwed: split.amountOwed as number
      }))
    };
  }

  private selectedRows(): SplitRow[] {
    return this.splitRows.filter(row => row.selected);
  }

  private applyCalculatedSplits(splits: ExpenseSplitCreateDto[]): void {
    this.splitRows = this.splitRows.map(row => {
      const split = splits.find(s => s.roommateId === row.roommate.id);
      return {
        ...row,
        selected: !!split,
        amountOwed: split ? this.formatAmount(split.amountOwed) : null
      };
    });
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
