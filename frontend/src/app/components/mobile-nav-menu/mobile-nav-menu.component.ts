import { Component } from '@angular/core';
import { NgbActiveOffcanvas } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../services/auth.service';
import { NAV_ITEMS } from '../../global/nav-items';

/** Content of the mobile "More" bottom sheet: the nav items that aren't primary tabs, plus Profile/Logout. */
@Component({
  selector: 'app-mobile-nav-menu',
  templateUrl: './mobile-nav-menu.component.html',
  styleUrls: ['./mobile-nav-menu.component.scss'],
  standalone: false
})
export class MobileNavMenuComponent {

  overflowItems = NAV_ITEMS.filter(item => !item.primaryMobile);

  constructor(
    public activeOffcanvas: NgbActiveOffcanvas,
    private authService: AuthService
  ) { }

  close(): void {
    this.activeOffcanvas.dismiss();
  }

  logout(): void {
    this.authService.logoutUser();
    this.close();
  }
}
