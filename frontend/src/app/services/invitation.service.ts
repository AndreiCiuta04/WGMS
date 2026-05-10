import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invitation, InvitationAcceptResponse, InvitationCreateRequest, InvitationPreview } from '../dtos/invitation';
import { Globals } from '../global/globals';

@Injectable({ providedIn: 'root' })
export class InvitationService {
  private baseUri: string = this.globals.backendUri;

  constructor(private http: HttpClient, private globals: Globals) {}

  createInvitation(householdId: number, request: InvitationCreateRequest): Observable<Invitation> {
    return this.http.post<Invitation>(`${this.baseUri}/households/${householdId}/invitations`, request);
  }

  previewInvitation(token: string): Observable<InvitationPreview> {
    return this.http.get<InvitationPreview>(`${this.baseUri}/invitations/${token}`);
  }

  acceptInvitation(token: string): Observable<InvitationAcceptResponse> {
    return this.http.post<InvitationAcceptResponse>(`${this.baseUri}/invitations/${token}/accept`, {});
  }
  
  getInvitations(householdId: number): Observable<Invitation[]> {
    return this.http.get<Invitation[]>(`${this.baseUri}/households/${householdId}/invitations`);
  }
  
  deleteInvitation(invitationId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/invitations/${invitationId}`);
  }
}
