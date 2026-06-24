import { BalanceDto } from './expense';

export interface PaymentCreateDto {
  toRoommateId: number;
  amount: number;
  date: string;
}

export interface PaymentDto {
  id: number;
  fromRoommateId: number;
  fromRoommateName: string;
  fromRoommateEmail: string;
  toRoommateId: number;
  toRoommateName: string;
  toRoommateEmail: string;
  amount: number;
  date: string;
  createdAt: string;
}

export interface PaymentOverviewDto {
  openBalances: BalanceDto[];
  payments: PaymentDto[];
  totalIOwe: number;
  totalOwedToMe: number;
}

export type SettlementDecisionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface PendingSettlementDto {
  id: string;
  expenseDescription: string;
  fromRoommateId: number;
  fromRoommateName: string;
  fromRoommateEmail: string;
  toRoommateId: number;
  toRoommateName: string;
  toRoommateEmail: string;
  amount: number;
  status: SettlementDecisionStatus;
  canDecide: boolean;
  createdAt: string | null;
  decidedAt: string | null;
  decidedByName: string | null;
}
