export enum ShoppingItemStatus {
  NEEDED = 'NEEDED',
  BOUGHT = 'BOUGHT',
  CANCELLED = 'CANCELLED'
}

export enum ShoppingItemVisibility {
  PERSONAL = 'PERSONAL',
  TARGETED = 'TARGETED',
  SHARED = 'SHARED'
}

export interface ShoppingItemCreateDto {
  name: string;
  quantity?: string | null;
  notes?: string | null;
  visibility: ShoppingItemVisibility;
  targetUserEmail?: string | null;
}

export interface ShoppingItemUpdateDto {
  name: string;
  quantity?: string | null;
  notes?: string | null;
  visibility: ShoppingItemVisibility;
  targetUserEmail?: string | null;
}

export interface ShoppingItemStatusUpdateDto {
  status: ShoppingItemStatus;
}

export interface ShoppingItemDto {
  id: number;
  name: string;
  quantity?: string | null;
  notes?: string | null;
  status: ShoppingItemStatus;
  visibility: ShoppingItemVisibility;
  householdId: number;
  createdById: number;
  createdByEmail: string;
  createdByFirstName: string;
  createdByLastName: string;
  targetUserId?: number | null;
  targetUserEmail?: string | null;
  targetUserFirstName?: string | null;
  targetUserLastName?: string | null;
  createdAt: string;
  updatedAt: string;
}
