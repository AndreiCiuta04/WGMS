import { Component } from '@angular/core';
import { NgbOffcanvas } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../services/auth.service';
import { NAV_ITEMS } from '../../global/nav-items';
import { MobileNavMenuComponent } from '../mobile-nav-menu/mobile-nav-menu.component';

@Component({
  selector: 'app-bottom-tab-bar',
  templateUrl: './bottom-tab-bar.component.html',
  styleUrls: ['./bottom-tab-bar.component.scss'],
  standalone: false
})
export class BottomTabBarComponent {

  primaryItems = NAV_ITEMS.filter(item => item.primaryMobile);

  constructor(public authService: AuthService, private offcanvas: NgbOffcanvas) { }

  openMore(): void {
    this.offcanvas.open(MobileNavMenuComponent, {
      position: 'bottom',
      panelClass: 'mobile-nav-sheet-panel'
    });
  }
}
