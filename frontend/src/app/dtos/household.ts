export interface Household {
  id: number;
  name: string;
  address?: string;
  createdAt: string;
  currentUserRole: 'MAIN_TENANT' | 'ROOMMATE';
}

export interface HouseholdCreateRequest {
  name: string;
  address?: string;
}
