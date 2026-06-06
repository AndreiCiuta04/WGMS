export interface UserDataExport {
  generatedAt: string;
  account: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
    termsAcceptedAt: string | null;
    termsVersion: string | null;
    privacyAcknowledgedAt: string | null;
    privacyVersion: string | null;
  };
  householdMembership: {
    householdId: number;
    householdName: string;
    roommateId: number;
    membershipRole: string;
    joinedAt: string;
  } | null;
  tasks: unknown[];
  cleaningSchedules: unknown[];
  expenses: unknown[];
  payments: unknown[];
  shoppingItems: unknown[];
  history: unknown;
}
