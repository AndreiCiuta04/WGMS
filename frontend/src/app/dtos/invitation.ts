export interface Invitation {
  id: number;
  inviteUrl: string;
  expiresAt: string;
  invitedEmail?: string;
  status: string;
}

export interface InvitationCreateRequest {
  invitedEmail?: string;
  expiresInHours?: number;
}

export interface InvitationPreview {
  householdName: string;
  invitedEmail?: string;
  expiresAt: string;
  valid: boolean;
}

export interface InvitationAcceptResponse {
  householdId: number;
  householdName: string;
  role: 'MAIN_TENANT' | 'ROOMMATE';
}
