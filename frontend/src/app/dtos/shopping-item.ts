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

export interface AggregatedShoppingItemSourceDto {
  id: number;
  name: string;
  quantity?: string | null;
  notes?: string | null;
  createdById: number;
  createdByEmail: string;
  createdByFirstName: string;
  createdByLastName: string;
  createdAt: string;
}

export interface AggregatedShoppingItemDto {
  name: string;
  quantity?: string | null;
  itemCount: number;
  sourceItems: AggregatedShoppingItemSourceDto[];
}

export interface ShoppingItemSuggestionCreateDto {
  name: string;
  quantity?: string | null;
  notes?: string | null;
}

export interface ShoppingItemSuggestionDto {
  id: number;
  name: string;
  quantity?: string | null;
  notes?: string | null;
  householdId: number;
  createdById: number;
  createdByEmail: string;
  createdByFirstName: string;
  createdByLastName: string;
  voteCount: number;
  votedByCurrentUser: boolean;
  createdAt: string;
  updatedAt: string;
}