import { Component, OnInit } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { InvitationService } from '../../services/invitation.service';
import { HouseholdService } from '../../services/household.service';
import { Invitation } from '../../dtos/invitation';
import { Household } from '../../dtos/household';

@Component({
  selector: 'app-household-invitations',
  templateUrl: './household-invitations.component.html',
  styleUrls: ['./household-invitations.component.scss'],
  standalone: false
})
export class HouseholdInvitationsComponent implements OnInit {

  household: Household | null = null;
  invitations: Invitation[] = [];
  form: UntypedFormGroup;
  lastCreatedUrl: string = '';
  errorMessage: string = '';

  constructor(
    private fb: UntypedFormBuilder,
    private invitationService: InvitationService,
    private householdService: HouseholdService
  ) {
    this.form = this.fb.group({
      invitedEmail: ['', [Validators.email]],
      expiresInHours: [168]
    });
  }

  ngOnInit(): void {
    this.householdService.getMyHousehold().subscribe({
      next: h => {
        this.household = h;
        this.loadInvitations();
      },
      error: err => this.errorMessage = 'Could not load household information.'
    });
  }

  loadInvitations(): void {
    if (this.household) {
      this.invitationService.getInvitations(this.household.id).subscribe({
        next: invs => this.invitations = invs,
        error: err => {
          if (err.status === 403) {
            this.errorMessage = 'Access Denied: Only the Main Tenant can manage invitations.';
          } else {
            this.errorMessage = 'Could not load invitations.';
          }
        }
      });
    }
  }

  create(): void {
    if (this.household) {
      this.invitationService.createInvitation(this.household.id, this.form.value).subscribe({
        next: inv => {
          this.lastCreatedUrl = inv.inviteUrl;
          this.loadInvitations();
          this.form.reset({ expiresInHours: 168 });
        },
        error: err => this.errorMessage = 'Could not create invitation.'
      });
    }
  }

  delete(id: number): void {
    this.invitationService.deleteInvitation(id).subscribe({
      next: () => this.loadInvitations(),
      error: err => this.errorMessage = 'Could not revoke invitation.'
    });
  }

  copyToClipboard(val: string): void {
    navigator.clipboard.writeText(val);
  }

}
