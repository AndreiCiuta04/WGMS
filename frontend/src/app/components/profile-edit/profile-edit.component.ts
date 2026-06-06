import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../dtos/user-profile';

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.scss'],
  standalone: false
})
export class ProfileEditComponent implements OnInit {

  profile: UserProfile | null = null;
  originalEmail = '';
  email = '';
  firstName = '';
  lastName = '';
  errorMessage = '';
  successMessage = '';
  submitted = false;
  saving = false;
  leavingHousehold = false;

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.authService.getCurrentUserProfile().subscribe({
      next: profile => {
        this.profile = profile;
        this.originalEmail = profile.email;
        this.email = profile.email;
        this.firstName = profile.firstName;
        this.lastName = profile.lastName;
      },
      error: () => this.errorMessage = 'Could not load your editable profile information.'
    });
  }

  saveProfile(): void {
    this.submitted = true;
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.email.trim() || !this.firstName.trim() || !this.lastName.trim()) {
      return;
    }

    this.saving = true;
    this.authService.updateCurrentUserProfile({
      email: this.email.trim(),
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim()
    }).subscribe({
      next: profile => {
        if (profile.email !== this.originalEmail) {
          this.authService.logoutUser();
          this.router.navigate(['/login']);
        } else {
          this.profile = profile;
          this.originalEmail = profile.email;
          this.email = profile.email;
          this.firstName = profile.firstName;
          this.lastName = profile.lastName;
          this.saving = false;
          this.successMessage = 'Profile changes saved.';
        }
      },
      error: () => {
        this.saving = false;
        this.errorMessage = 'Could not save your profile changes.';
      }
    });
  }

  leaveHousehold(): void {
    const confirmed = window.confirm('Leave your current WG? Historical household records will stay readable.');
    if (!confirmed) {
      return;
    }

    this.leavingHousehold = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.leaveCurrentHousehold().subscribe({
      next: profile => {
        this.profile = profile;
        this.successMessage = 'You left your current WG.';
        this.leavingHousehold = false;
      },
      error: () => {
        this.leavingHousehold = false;
        this.errorMessage = 'Could not leave your current WG.';
      }
    });
  }
}
