export interface Roommate {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: 'MAIN_TENANT' | 'ROOMMATE';
  joinedAt: string;
}
