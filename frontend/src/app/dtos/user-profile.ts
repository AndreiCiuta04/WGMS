export interface UserProfile {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  householdId: number | null;
  householdName: string | null;
  roommateId: number | null;
}
