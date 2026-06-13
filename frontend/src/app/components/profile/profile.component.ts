import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { UserProfile } from '../../dtos/user-profile';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
  standalone: false
})
export class ProfileComponent implements OnInit {

  profile: UserProfile | null = null;
  errorMessage = '';
  exportMessage = '';
  deleting = false;
  exporting = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private confirmDialog: ConfirmDialogService
  ) { }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.authService.getCurrentUserProfile().subscribe({
      next: profile => this.profile = profile,
      error: () => this.errorMessage = 'Could not load your profile information.'
    });
  }

  deleteAccount(): void {
    this.confirmDialog.confirm({
      title: 'Delete your account?',
      message: 'Your login will be disabled and your identifying account data will be anonymized.',
      confirmLabel: 'Delete account',
      danger: true
    }).then(confirmed => {
      if (!confirmed) {
        return;
      }

      this.deleting = true;
      this.errorMessage = '';
      this.authService.anonymizeCurrentUser().subscribe({
        next: () => {
          this.authService.logoutUser();
          this.router.navigate(['/login']);
        },
        error: () => {
          this.deleting = false;
          this.errorMessage = 'Could not delete your account.';
        }
      });
    });
  }

  exportMyData(): void {
    this.exporting = true;
    this.exportMessage = '';
    this.errorMessage = '';

    this.authService.getMyDataExport().subscribe({
      next: exportData => {
        const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'wgms-my-data-export.json';
        link.click();
        window.URL.revokeObjectURL(url);
        this.exporting = false;
        this.exportMessage = 'Data export generated.';
      },
      error: () => {
        this.exporting = false;
        this.errorMessage = 'Could not export your data.';
      }
    });
  }

  getDisplayName(): string {
    if (!this.profile) {
      return 'Your profile';
    }
    return `${this.profile.firstName} ${this.profile.lastName}`.trim();
  }
}
