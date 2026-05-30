import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  standalone: false
})
export class SidebarComponent {

  constructor(public authService: AuthService) { }

  getUserDisplayName(): string {
    const email = this.authService.getUserEmail();
    if (email) {
      return email.split('@')[0];
    }
    return 'User';
  }
}
