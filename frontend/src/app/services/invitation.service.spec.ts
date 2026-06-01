import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { InvitationService } from './invitation.service';
import { Globals } from '../global/globals';
import { Invitation, InvitationAcceptResponse, InvitationPreview } from '../dtos/invitation';

describe('InvitationService', () => {
  const backendUri = 'http://backend.test/api/v1';
  let service: InvitationService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: Globals, useValue: { backendUri } }
      ]
    });

    service = TestBed.inject(InvitationService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should create', () => {
    expect(service).toBeTruthy();
  });

  it('should send POST to create invitation URL with request body', () => {
    const requestBody = { invitedEmail: 'friend@example.com', expiresInHours: 24 };

    service.createInvitation(7, requestBody).subscribe(response => {
      expect(response).toEqual(invitation());
    });

    const request = httpTestingController.expectOne(`${backendUri}/households/7/invitations`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(requestBody);
    request.flush(invitation());
  });

  it('should send GET to list invitations URL', () => {
    service.getInvitations(7).subscribe(response => {
      expect(response).toEqual([invitation()]);
    });

    const request = httpTestingController.expectOne(`${backendUri}/households/7/invitations`);
    expect(request.request.method).toBe('GET');
    request.flush([invitation()]);
  });

  it('should send DELETE to revoke invitation URL', () => {
    service.deleteInvitation(12).subscribe(response => {
      expect(response).toBeNull();
    });

    const request = httpTestingController.expectOne(`${backendUri}/invitations/12`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('should send GET to preview invitation URL', () => {
    service.previewInvitation('invite-token').subscribe(response => {
      expect(response).toEqual(preview());
    });

    const request = httpTestingController.expectOne(`${backendUri}/invitations/invite-token`);
    expect(request.request.method).toBe('GET');
    request.flush(preview());
  });

  it('should send POST to accept invitation URL with empty body', () => {
    service.acceptInvitation('invite-token').subscribe(response => {
      expect(response).toEqual(acceptResponse());
    });

    const request = httpTestingController.expectOne(`${backendUri}/invitations/invite-token/accept`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush(acceptResponse());
  });
});

function invitation(overrides: Partial<Invitation> = {}): Invitation {
  return {
    id: 1,
    inviteUrl: 'http://localhost/#/invite/token',
    expiresAt: '2026-06-08T00:00:00',
    invitedEmail: 'friend@example.com',
    status: 'ACTIVE',
    ...overrides
  };
}

function preview(overrides: Partial<InvitationPreview> = {}): InvitationPreview {
  return {
    householdName: 'Dream Loft Vienna',
    invitedEmail: 'friend@example.com',
    expiresAt: '2026-06-08T00:00:00',
    valid: true,
    ...overrides
  };
}

function acceptResponse(overrides: Partial<InvitationAcceptResponse> = {}): InvitationAcceptResponse {
  return {
    householdId: 1,
    householdName: 'Dream Loft Vienna',
    role: 'ROOMMATE',
    ...overrides
  };
}
