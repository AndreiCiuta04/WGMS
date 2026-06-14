import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ElementRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { ExpensesComponent } from './expenses.component';
import { ExpenseService } from '../../services/expense.service';
import { HouseholdService } from '../../services/household.service';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { BalanceDto, ExpenseDto, ExpenseSplitCreateDto } from '../../dtos/expense';
import { Household } from '../../dtos/household';
import { Roommate } from '../../dtos/roommate';
import { ReceiptOcrService } from '../../services/receipt-ocr.service';

describe('ExpensesComponent', () => {
  let component: ExpensesComponent;
  let fixture: ComponentFixture<ExpensesComponent>;
  let expenseService: jasmine.SpyObj<ExpenseService>;
  let householdService: jasmine.SpyObj<HouseholdService>;
  let authService: jasmine.SpyObj<AuthService>;
  let receiptOcrService: jasmine.SpyObj<ReceiptOcrService>;
  let confirmDialog: jasmine.SpyObj<ConfirmDialogService>;

  beforeEach(async () => {
    expenseService = jasmine.createSpyObj<ExpenseService>('ExpenseService', [
      'findAll',
      'findOne',
      'create',
      'update',
      'delete',
      'calculateEqualSplits',
      'previewBillExpense',
      'calculateBalances'
    ]);
    householdService = jasmine.createSpyObj<HouseholdService>('HouseholdService', ['getMyHousehold', 'getMembers']);
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getUserEmail']);
    receiptOcrService = jasmine.createSpyObj<ReceiptOcrService>('ReceiptOcrService', ['scanCanvas']);
    confirmDialog = jasmine.createSpyObj<ConfirmDialogService>('ConfirmDialogService', ['confirm']);
    confirmDialog.confirm.and.returnValue(Promise.resolve(true));

    householdService.getMyHousehold.and.returnValue(of(household()));
    householdService.getMembers.and.returnValue(of(roommates()));
    authService.getUserEmail.and.returnValue('main@example.com');
    receiptOcrService.scanCanvas.and.resolveTo({
      description: 'Scanned supermarket receipt',
      totalAmount: 12.5,
      date: '2026-06-05'
    });
    expenseService.findAll.and.returnValue(of([expense()]));
    expenseService.calculateBalances.and.returnValue(of([balance()]));
    expenseService.calculateEqualSplits.and.returnValue(of([
      { roommateId: 1, amountOwed: 6 },
      { roommateId: 2, amountOwed: 6 }
    ]));
    expenseService.previewBillExpense.and.returnValue(of({
      description: 'Bill scanner groceries',
      totalAmount: 12,
      date: '2026-06-05',
      paidByRoommateId: 1,
      splits: [
        { roommateId: 1, amountOwed: 6 },
        { roommateId: 2, amountOwed: 6 }
      ]
    }));
    expenseService.create.and.returnValue(of(expense()));
    expenseService.update.and.returnValue(of(expense({ description: 'Updated groceries' })));
    expenseService.delete.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      declarations: [ExpensesComponent],
      imports: [ReactiveFormsModule, FormsModule],
      providers: [
        { provide: ExpenseService, useValue: expenseService },
        { provide: HouseholdService, useValue: householdService },
        { provide: AuthService, useValue: authService },
        { provide: ReceiptOcrService, useValue: receiptOcrService },
        { provide: ConfirmDialogService, useValue: confirmDialog }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ExpensesComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should load current household, members, current roommate, expenses, and balances on init', () => {
    fixture.detectChanges();

    expect(householdService.getMyHousehold).toHaveBeenCalled();
    expect(householdService.getMembers).toHaveBeenCalledOnceWith(1);
    expect(authService.getUserEmail).toHaveBeenCalled();
    expect(component.roommates).toEqual(roommates());
    expect(component.currentRoommate?.email).toBe('main@example.com');
    expect(component.expenses).toEqual([expense()]);
    expect(component.balances).toEqual([balance()]);
  });

  it('should create split rows for members and select the current payer by default', () => {
    fixture.detectChanges();

    expect(component.splitRows.length).toBe(2);
    expect(component.splitRows.map(row => ({ email: row.roommate.email, selected: row.selected }))).toEqual([
      { email: 'main@example.com', selected: true },
      { email: 'roommate@example.com', selected: false }
    ]);
  });

  it('should block create request when description is empty', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue(validExpenseForm({ description: '' }));
    component.splitRows[0].amountOwed = '12,00';

    component.submit();

    expect(component.submitted).toBeTrue();
    expect(component.errorMessage).toBe('Please fill in all required fields.');
    expect(expenseService.create).not.toHaveBeenCalled();
  });

  it('should block create request when total amount is negative', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue(validExpenseForm({ totalAmount: '-1' }));
    component.splitRows[0].amountOwed = '1,00';

    component.submit();

    expect(component.errorMessage).toBe('Enter a positive total amount.');
    expect(expenseService.create).not.toHaveBeenCalled();
  });

  it('should block create request when selected split has no positive amount', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue(validExpenseForm());
    component.splitRows[0].amountOwed = null;

    component.submit();

    expect(component.errorMessage).toBe('Select participants and enter a positive amount for every selected split.');
    expect(expenseService.create).not.toHaveBeenCalled();
  });

  it('should update split row selection and clear amount when deselected', () => {
    fixture.detectChanges();
    const roommateRow = component.splitRows[1];
    roommateRow.amountOwed = '6,00';

    component.updateSplitSelection(roommateRow, true);
    expect(roommateRow.selected).toBeTrue();

    component.updateSplitSelection(roommateRow, false);
    expect(roommateRow.selected).toBeFalse();
    expect(roommateRow.amountOwed).toBeNull();
  });

  it('should call equal split service with total amount and selected participant ids', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue({ totalAmount: '12,00' });
    component.updateSplitSelection(component.splitRows[1], true);

    component.calculateEqualSplit();

    expect(expenseService.calculateEqualSplits).toHaveBeenCalledOnceWith({
      totalAmount: 12,
      participantRoommateIds: [1, 2]
    });
  });

  it('should apply equal split result to selected split rows and formatted amounts', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue({ totalAmount: '12,00' });
    component.updateSplitSelection(component.splitRows[1], true);

    component.calculateEqualSplit();

    expect(component.splitRows.map(row => ({ id: row.roommate.id, selected: row.selected, amount: row.amountOwed }))).toEqual([
      { id: 1, selected: true, amount: '6,00' },
      { id: 2, selected: true, amount: '6,00' }
    ]);
  });

  it('should call bill preview service with bill data and selected participant ids', () => {
    fixture.detectChanges();
    component.billForm.patchValue(validExpenseForm({ description: 'Bill scanner groceries' }));
    component.updateSplitSelection(component.splitRows[1], true);

    component.previewBillExpense();

    expect(expenseService.previewBillExpense).toHaveBeenCalledOnceWith({
      description: 'Bill scanner groceries',
      totalAmount: 12,
      date: '2026-06-05',
      participantRoommateIds: [1, 2]
    });
  });

  it('should apply bill preview to the editable expense form for review', () => {
    fixture.detectChanges();
    component.billCalculatorOpen = true;
    component.billForm.patchValue(validExpenseForm({ description: 'Bill scanner groceries' }));
    component.updateSplitSelection(component.splitRows[1], true);

    component.previewBillExpense();

    expect(component.successMessage).toBe('Bill data ready for review.');
    expect(component.billCalculatorOpen).toBeFalse();
    expect(component.expenseForm.value).toEqual(validExpenseForm({ description: 'Bill scanner groceries' }));
    expect(component.splitRows.map(row => ({ id: row.roommate.id, selected: row.selected, amount: row.amountOwed }))).toEqual([
      { id: 1, selected: true, amount: '6,00' },
      { id: 2, selected: true, amount: '6,00' }
    ]);
  });

  it('should show readable error when bill preview input is incomplete', () => {
    fixture.detectChanges();
    component.billForm.patchValue(validExpenseForm({ description: '', totalAmount: '' }));

    component.previewBillExpense();

    expect(component.errorMessage).toBe('Enter bill data and select at least one participant.');
    expect(expenseService.previewBillExpense).not.toHaveBeenCalled();
  });

  it('should show readable backend bill preview error', () => {
    fixture.detectChanges();
    expenseService.previewBillExpense.and.returnValue(throwError(() => ({ error: { message: 'The paying roommate must be included.' } })));
    component.billForm.patchValue(validExpenseForm({ description: 'Bill scanner groceries' }));

    component.previewBillExpense();

    expect(component.errorMessage).toBe('The paying roommate must be included.');
    expect(component.isPreviewingBill).toBeFalse();
  });

  it('should capture a camera frame, scan it, and prefill bill data for review', async () => {
    fixture.detectChanges();
    const fakeCanvas = stubCameraCanvas(component);

    component.captureAndScan();
    await fixture.whenStable();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(receiptOcrService.scanCanvas).toHaveBeenCalledOnceWith(fakeCanvas);
    expect(component.billForm.value).toEqual({
      description: 'Scanned supermarket receipt',
      totalAmount: '12,50',
      date: '2026-06-05'
    });
    expect(component.successMessage).toBe('Receipt scanned. Review the extracted bill data.');
    expect(component.isScanningReceipt).toBeFalse();
    expect(component.isCameraActive).toBeFalse();
  });

  it('should prompt for manual entry when the scanned receipt has no amount', async () => {
    fixture.detectChanges();
    receiptOcrService.scanCanvas.and.resolveTo({ description: 'Blurry receipt', totalAmount: null, date: '2026-06-05' });
    stubCameraCanvas(component);

    component.captureAndScan();
    await fixture.whenStable();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.billForm.get('totalAmount')?.value).toBe('');
    expect(component.successMessage).toBe('Receipt scanned, but no amount was found. Enter the bill total manually.');
    expect(component.isScanningReceipt).toBeFalse();
  });

  it('should surface a readable error when the receipt scan fails', async () => {
    fixture.detectChanges();
    receiptOcrService.scanCanvas.and.rejectWith(new Error('Vision API error: 500'));
    stubCameraCanvas(component);

    component.captureAndScan();
    await fixture.whenStable();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(component.errorMessage).toBe('Could not scan the receipt. Enter the bill data manually.');
    expect(component.isScanningReceipt).toBeFalse();
  });

  it('should stop the live camera stream when the bill panel is toggled closed', () => {
    fixture.detectChanges();
    const track = { stop: jasmine.createSpy('stop') };
    component.videoStream = { getTracks: () => [track] } as unknown as MediaStream;
    component.isCameraActive = true;
    component.billCalculatorOpen = true;

    component.toggleBillCalculator();

    expect(track.stop).toHaveBeenCalled();
    expect(component.isCameraActive).toBeFalse();
    expect(component.videoStream).toBeNull();
    expect(component.billCalculatorOpen).toBeFalse();
  });

  it('should show readable error when equal split input is incomplete', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue({ totalAmount: '' });

    component.calculateEqualSplit();

    expect(component.errorMessage).toBe('Enter an amount and select at least one participant.');
    expect(expenseService.calculateEqualSplits).not.toHaveBeenCalled();
  });

  it('should call ExpenseService.create with payer and split payload for valid create form', () => {
    fixture.detectChanges();
    component.expenseForm.patchValue(validExpenseForm());
    component.splitRows[0].amountOwed = '6,00';
    component.updateSplitSelection(component.splitRows[1], true);
    component.splitRows[1].amountOwed = '6,00';

    component.submit();

    expect(expenseService.create).toHaveBeenCalledOnceWith({
      description: 'Groceries',
      totalAmount: 12,
      date: '2026-06-05',
      paidByRoommateId: 1,
      splits: [
        { roommateId: 1, amountOwed: 6 },
        { roommateId: 2, amountOwed: 6 }
      ]
    });
  });

  it('should reset form and refresh expenses and balances after successful create', () => {
    fixture.detectChanges();
    expenseService.findAll.calls.reset();
    expenseService.calculateBalances.calls.reset();
    component.expenseForm.patchValue(validExpenseForm());
    component.splitRows[0].amountOwed = '12,00';

    component.submit();

    expect(component.successMessage).toBe('Expense created.');
    expect(component.editingExpense).toBeNull();
    expect(component.expenseForm.value.description).toBe('');
    expect(component.expenseForm.value.totalAmount).toBe('');
    expect(expenseService.findAll).toHaveBeenCalledTimes(1);
    expect(expenseService.calculateBalances).toHaveBeenCalledTimes(1);
    expect(component.isSaving).toBeFalse();
  });

  it('should show readable backend create error', () => {
    fixture.detectChanges();
    expenseService.create.and.returnValue(throwError(() => ({ error: { message: 'Split sum mismatch.' } })));
    component.expenseForm.patchValue(validExpenseForm());
    component.splitRows[0].amountOwed = '12,00';

    component.submit();

    expect(component.errorMessage).toBe('Split sum mismatch.');
    expect(component.isSaving).toBeFalse();
  });

  it('should populate form and split rows when editing an expense', () => {
    fixture.detectChanges();

    component.editExpense(expense());

    expect(component.editingExpense?.id).toBe(1);
    expect(component.expenseForm.value.description).toBe('Groceries');
    expect(component.expenseForm.value.totalAmount).toBe('12,00');
    expect(component.splitRows.map(row => ({ id: row.roommate.id, selected: row.selected, amount: row.amountOwed }))).toEqual([
      { id: 1, selected: true, amount: '6,00' },
      { id: 2, selected: true, amount: '6,00' }
    ]);
  });

  it('should call ExpenseService.update and refresh after submitting while editing', () => {
    fixture.detectChanges();
    component.editExpense(expense());
    expenseService.findAll.calls.reset();
    expenseService.calculateBalances.calls.reset();

    component.submit();

    expect(expenseService.update).toHaveBeenCalledOnceWith(1, {
      description: 'Groceries',
      totalAmount: 12,
      date: '2026-06-05',
      paidByRoommateId: 1,
      splits: [
        { roommateId: 1, amountOwed: 6 },
        { roommateId: 2, amountOwed: 6 }
      ]
    });
    expect(component.successMessage).toBe('Expense updated.');
    expect(expenseService.findAll).toHaveBeenCalledTimes(1);
    expect(expenseService.calculateBalances).toHaveBeenCalledTimes(1);
  });

  it('should not delete when the confirmation dialog is cancelled', fakeAsync(() => {
    fixture.detectChanges();
    confirmDialog.confirm.and.returnValue(Promise.resolve(false));

    component.deleteExpense(expense());
    tick();

    expect(confirmDialog.confirm).toHaveBeenCalled();
    expect(expenseService.delete).not.toHaveBeenCalled();
  }));

  it('should delete the expense and refresh expenses and balances when confirmed', fakeAsync(() => {
    fixture.detectChanges();
    expenseService.findAll.calls.reset();
    expenseService.calculateBalances.calls.reset();

    component.deleteExpense(expense());
    tick();

    expect(expenseService.delete).toHaveBeenCalledOnceWith(1);
    expect(component.successMessage).toBe('Expense deleted.');
    expect(expenseService.findAll).toHaveBeenCalledTimes(1);
    expect(expenseService.calculateBalances).toHaveBeenCalledTimes(1);
  }));

  it('should show readable delete error', fakeAsync(() => {
    fixture.detectChanges();
    expenseService.delete.and.returnValue(throwError(() => ({ error: 'Delete forbidden.' })));

    component.deleteExpense(expense());
    tick();

    expect(component.errorMessage).toBe('Delete forbidden.');
  }));

  it('should handle no-household load error gracefully', () => {
    householdService.getMyHousehold.and.returnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('You need to be part of a WG before tracking expenses.');
    expect(component.roommates).toEqual([]);
  });

  it('should handle member-loading error gracefully', () => {
    householdService.getMembers.and.returnValue(throwError(() => ({ error: { message: 'Members unavailable.' } })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Members unavailable.');
    expect(component.roommates).toEqual([]);
  });

  it('should handle load-expenses error gracefully', () => {
    expenseService.findAll.and.returnValue(throwError(() => ({ error: { message: 'Expenses unavailable.' } })));

    fixture.detectChanges();

    expect(component.errorMessage).toBe('Expenses unavailable.');
    expect(component.expenses).toEqual([]);
  });
});

function stubCameraCanvas(component: ExpensesComponent, videoWidth = 100, videoHeight = 100): HTMLCanvasElement {
  const realCreateElement = document.createElement.bind(document);
  const video = realCreateElement('video') as HTMLVideoElement;
  Object.defineProperty(video, 'videoWidth', { value: videoWidth, configurable: true });
  Object.defineProperty(video, 'videoHeight', { value: videoHeight, configurable: true });
  component.receiptVideoRef = { nativeElement: video } as ElementRef<HTMLVideoElement>;

  const context = { drawImage: jasmine.createSpy('drawImage') } as unknown as CanvasRenderingContext2D;
  const fakeCanvas = { width: 0, height: 0, getContext: () => context } as unknown as HTMLCanvasElement;
  spyOn(document, 'createElement').and.callFake(((tag: string) =>
    tag === 'canvas' ? fakeCanvas : realCreateElement(tag)) as typeof document.createElement);
  return fakeCanvas;
}

function validExpenseForm(overrides: Partial<{
  description: string;
  totalAmount: string;
  date: string;
}> = {}) {
  return {
    description: 'Groceries',
    totalAmount: '12,00',
    date: '2026-06-05',
    ...overrides
  };
}

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

function roommates(): Roommate[] {
  return [
    roommate({ id: 1, userId: 1, email: 'main@example.com', role: 'MAIN_TENANT', firstName: 'Main' }),
    roommate({ id: 2, userId: 2, email: 'roommate@example.com', role: 'ROOMMATE', firstName: 'Room' })
  ];
}

function roommate(overrides: Partial<Roommate> = {}): Roommate {
  return {
    id: 1,
    userId: 1,
    firstName: 'Anna',
    lastName: 'Berger',
    email: 'main@example.com',
    role: 'MAIN_TENANT',
    joinedAt: '2026-06-01T00:00:00',
    ...overrides
  };
}

function expense(overrides: Partial<ExpenseDto> = {}): ExpenseDto {
  return {
    id: 1,
    householdId: 1,
    description: 'Groceries',
    totalAmount: 12,
    date: '2026-06-05',
    createdAt: '2026-06-01T00:00:00',
    paidByRoommateId: 1,
    paidByName: 'Main Berger',
    paidByEmail: 'main@example.com',
    createdByRoommateId: 1,
    createdByName: 'Main Berger',
    createdByEmail: 'main@example.com',
    splits: [
      {
        id: 1,
        roommateId: 1,
        roommateName: 'Main Berger',
        roommateEmail: 'main@example.com',
        amountOwed: 6
      },
      {
        id: 2,
        roommateId: 2,
        roommateName: 'Room Berger',
        roommateEmail: 'roommate@example.com',
        amountOwed: 6
      }
    ],
    ...overrides
  };
}

function balance(overrides: Partial<BalanceDto> = {}): BalanceDto {
  return {
    fromRoommateId: 2,
    fromRoommateName: 'Room Berger',
    fromRoommateEmail: 'roommate@example.com',
    toRoommateId: 1,
    toRoommateName: 'Main Berger',
    toRoommateEmail: 'main@example.com',
    amount: 6,
    ...overrides
  };
}
