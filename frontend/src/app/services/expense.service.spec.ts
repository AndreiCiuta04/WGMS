import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { ExpenseService } from './expense.service';
import { Globals } from '../global/globals';
import {
  BalanceDto,
  BillExpensePreviewRequestDto,
  EqualSplitRequestDto,
  ExpenseCreateDto,
  ExpenseDto,
  ExpenseUpdateDto
} from '../dtos/expense';

describe('ExpenseService', () => {
  const backendUri = 'http://backend.test/api/v1';
  let service: ExpenseService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Globals, useValue: { backendUri } }
      ]
    });

    service = TestBed.inject(ExpenseService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should send GET to find all expenses', () => {
    service.findAll().subscribe(response => {
      expect(response).toEqual([expense()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses`);
    expect(request.request.method).toBe('GET');
    request.flush([expense()]);
  });

  it('should send GET to find one expense', () => {
    service.findOne(7).subscribe(response => {
      expect(response).toEqual(expense({ id: 7 }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses/7`);
    expect(request.request.method).toBe('GET');
    request.flush(expense({ id: 7 }));
  });

  it('should send POST to create expense with body', () => {
    const body = expensePayload();

    service.create(body).subscribe(response => {
      expect(response).toEqual(expense());
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush(expense());
  });

  it('should send PUT to update expense with body', () => {
    const body: ExpenseUpdateDto = expensePayload({ description: 'Updated groceries' });

    service.update(7, body).subscribe(response => {
      expect(response).toEqual(expense({ id: 7, description: 'Updated groceries' }));
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses/7`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush(expense({ id: 7, description: 'Updated groceries' }));
  });

  it('should send DELETE to delete expense', () => {
    service.delete(7).subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses/7`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('should send POST to calculate equal splits with body', () => {
    const body: EqualSplitRequestDto = {
      totalAmount: 12,
      participantRoommateIds: [1, 2]
    };

    service.calculateEqualSplits(body).subscribe(response => {
      expect(response).toEqual([
        { roommateId: 1, amountOwed: 6 },
        { roommateId: 2, amountOwed: 6 }
      ]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses/equal-split`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush([
      { roommateId: 1, amountOwed: 6 },
      { roommateId: 2, amountOwed: 6 }
    ]);
  });

  it('should send POST to preview bill expense with body', () => {
    const body: BillExpensePreviewRequestDto = {
      description: 'Bill scanner groceries',
      totalAmount: 12,
      date: '2026-06-05',
      participantRoommateIds: [1, 2]
    };

    service.previewBillExpense(body).subscribe(response => {
      expect(response).toEqual({
        description: 'Bill scanner groceries',
        totalAmount: 12,
        date: '2026-06-05',
        paidByRoommateId: 1,
        splits: [
          { roommateId: 1, amountOwed: 6 },
          { roommateId: 2, amountOwed: 6 }
        ]
      });
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses/bill-preview`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush({
      description: 'Bill scanner groceries',
      totalAmount: 12,
      date: '2026-06-05',
      paidByRoommateId: 1,
      splits: [
        { roommateId: 1, amountOwed: 6 },
        { roommateId: 2, amountOwed: 6 }
      ]
    });
  });

  it('should send GET to calculate balances', () => {
    service.calculateBalances().subscribe(response => {
      expect(response).toEqual([balance()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/expenses/balances`);
    expect(request.request.method).toBe('GET');
    request.flush([balance()]);
  });
});

function expensePayload(overrides: Partial<ExpenseCreateDto> = {}): ExpenseCreateDto {
  return {
    description: 'Groceries',
    totalAmount: 12,
    date: '2026-06-05',
    paidByRoommateId: 1,
    splits: [
      { roommateId: 1, amountOwed: 6 },
      { roommateId: 2, amountOwed: 6 }
    ],
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
