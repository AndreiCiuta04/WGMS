import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../dtos/user-profile';
import { NAV_ITEMS } from '../../global/nav-items';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  standalone: false
})
export class SidebarComponent implements OnInit {

  profile: UserProfile | null = null;
  navItems = NAV_ITEMS;

  constructor(public authService: AuthService) { }

  ngOnInit(): void {
    this.authService.getCurrentUserProfile().subscribe({
      next: profile => this.profile = profile,
      error: () => this.profile = null
    });
  }

  getUserDisplayName(): string {
    if (this.profile) {
      return `${this.profile.firstName} ${this.profile.lastName}`.trim();
    }
    const email = this.authService.getUserEmail();
    if (email) {
      return email.split('@')[0];
    }
    return 'User';
  }
}
