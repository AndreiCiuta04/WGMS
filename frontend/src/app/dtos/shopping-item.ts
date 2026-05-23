export enum ShoppingItemStatus {
  NEEDED = 'NEEDED',
  BOUGHT = 'BOUGHT',
  CANCELLED = 'CANCELLED'
}

export interface ShoppingItemCreateDto {
  name: string;
  quantity?: string;
  notes?: string;
}

export interface ShoppingItemUpdateDto {
  name: string;
  quantity?: string;
  notes?: string;
}

export interface ShoppingItemStatusUpdateDto {
  status: ShoppingItemStatus;
}

export interface ShoppingItemDto {
  id: number;
  name: string;
  quantity?: string;
  notes?: string;
  status: ShoppingItemStatus;
  householdId: number;
  createdById: number;
  createdByEmail: string;
  createdByFirstName: string;
  createdByLastName: string;
  createdAt: string;
  updatedAt: string;
}
