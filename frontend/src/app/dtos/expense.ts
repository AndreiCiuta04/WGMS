export interface ExpenseSplitCreateDto {
  roommateId: number;
  amountOwed: number;
}

export interface ExpenseCreateDto {
  description: string;
  totalAmount: number;
  date: string;
  paidByRoommateId: number;
  splits: ExpenseSplitCreateDto[];
}

export interface ExpenseUpdateDto extends ExpenseCreateDto {}

export interface ExpenseSplitDto {
  id: number;
  roommateId: number;
  roommateName: string;
  roommateEmail: string;
  amountOwed: number;
}

export interface ExpenseDto {
  id: number;
  householdId: number;
  description: string;
  totalAmount: number;
  date: string;
  createdAt: string;
  paidByRoommateId: number;
  paidByName: string;
  paidByEmail: string;
  createdByRoommateId: number;
  createdByName: string;
  createdByEmail: string;
  splits: ExpenseSplitDto[];
}

export interface BalanceDto {
  fromRoommateId: number;
  fromRoommateName: string;
  fromRoommateEmail: string;
  toRoommateId: number;
  toRoommateName: string;
  toRoommateEmail: string;
  amount: number;
}

export interface EqualSplitRequestDto {
  totalAmount: number;
  participantRoommateIds: number[];
}

export interface BillExpensePreviewRequestDto {
  description: string;
  totalAmount: number;
  date: string;
  participantRoommateIds: number[];
}

export interface BillExpensePreviewDto {
  description: string;
  totalAmount: number;
  date: string;
  paidByRoommateId: number;
  splits: ExpenseSplitCreateDto[];
}
