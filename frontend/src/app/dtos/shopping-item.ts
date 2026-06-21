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

export enum QuantityUnit {
  MILLILITER = 'MILLILITER',
  LITER = 'LITER',
  GRAM = 'GRAM',
  KILOGRAM = 'KILOGRAM',
  PIECE = 'PIECE',
  PACK = 'PACK'
}

/** Short display symbol for each unit, e.g. LITER -> "L". */
export const QUANTITY_UNIT_SYMBOL: Record<QuantityUnit, string> = {
  [QuantityUnit.MILLILITER]: 'ml',
  [QuantityUnit.LITER]: 'L',
  [QuantityUnit.GRAM]: 'g',
  [QuantityUnit.KILOGRAM]: 'kg',
  [QuantityUnit.PIECE]: 'pcs',
  [QuantityUnit.PACK]: 'pack'
};

/** Dropdown options (value + label) for the unit picker. */
export const QUANTITY_UNIT_OPTIONS: { value: QuantityUnit; label: string }[] = [
  { value: QuantityUnit.PIECE, label: 'pcs' },
  { value: QuantityUnit.PACK, label: 'pack' },
  { value: QuantityUnit.GRAM, label: 'g' },
  { value: QuantityUnit.KILOGRAM, label: 'kg' },
  { value: QuantityUnit.MILLILITER, label: 'ml' },
  { value: QuantityUnit.LITER, label: 'L' }
];

/** The three structured quantity fields shared by every shopping-item shape. */
export interface StructuredQuantity {
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | string | null;
}

/**
 * Renders a structured quantity as a single human string:
 * count + amount + unit -> "5 × 3 L", amount + unit -> "3 L",
 * count only -> "5×", nothing -> "".
 */
export function formatQuantity(item: StructuredQuantity | null | undefined): string {
  if (!item) {
    return '';
  }
  const { quantityCount, quantityAmount, quantityUnit } = item;
  const symbol = quantityUnit ? (QUANTITY_UNIT_SYMBOL[quantityUnit as QuantityUnit] ?? '') : '';

  let size = '';
  if (quantityAmount != null) {
    size = symbol ? `${quantityAmount} ${symbol}` : `${quantityAmount}`;
  } else if (symbol) {
    size = symbol;
  }

  if (quantityCount != null) {
    return size ? `${quantityCount} × ${size}` : `${quantityCount}×`;
  }
  return size;
}

export interface ShoppingItemCreateDto {
  name: string;
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | null;
  notes?: string | null;
  visibility: ShoppingItemVisibility;
  targetUserEmail?: string | null;
}

export interface ShoppingItemUpdateDto {
  name: string;
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | null;
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
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | null;
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
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | null;
  notes?: string | null;
  createdById: number;
  createdByEmail: string;
  createdByFirstName: string;
  createdByLastName: string;
  createdAt: string;
}

export interface AggregatedShoppingItemDto {
  name: string;
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | null;
  itemCount: number;
  sourceItems: AggregatedShoppingItemSourceDto[];
}

export interface ShoppingItemSuggestionCreateDto {
  name: string;
  quantity?: string | null;
  notes?: string | null;
}

export interface ShoppingItemSuggestionVoterDto {
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
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
  voters: ShoppingItemSuggestionVoterDto[];
  createdAt: string;
  updatedAt: string;
}

export interface ShoppingItemScanDto {
  payload: string;
}

/** Type tag every WGMS shopping QR carries, so foreign codes are rejected. */
export const WGMS_SHOPPING_QR_TYPE = 'wgms.shopping-item';
/** Schema version of the QR payload the backend understands. */
export const WGMS_SHOPPING_QR_VERSION = 1;

/**
 * Builds the canonical QR payload string for a shopping item (used by the
 * "Share via QR" dialog). The shape mirrors exactly what the backend
 * ShoppingQrPayloadParser validates. A TARGETED item is shared as SHARED, since
 * a QR cannot carry a household-scoped target user.
 */
export function buildShoppingItemQrPayload(item: {
  name: string;
  quantityCount?: number | null;
  quantityAmount?: number | null;
  quantityUnit?: QuantityUnit | string | null;
  notes?: string | null;
  visibility?: ShoppingItemVisibility | string | null;
}): string {
  const payload: Record<string, unknown> = {
    type: WGMS_SHOPPING_QR_TYPE,
    version: WGMS_SHOPPING_QR_VERSION,
    name: item.name
  };
  if (item.quantityCount != null) {
    payload['quantityCount'] = item.quantityCount;
  }
  if (item.quantityAmount != null) {
    payload['quantityAmount'] = item.quantityAmount;
  }
  if (item.quantityUnit) {
    payload['quantityUnit'] = item.quantityUnit;
  }
  if (item.notes) {
    payload['notes'] = item.notes;
  }
  payload['visibility'] = item.visibility === ShoppingItemVisibility.PERSONAL ? 'PERSONAL' : 'SHARED';
  return JSON.stringify(payload);
}
