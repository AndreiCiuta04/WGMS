import {BrowserModule} from '@angular/platform-browser';
import {NgModule, provideZoneChangeDetection} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {RegisterComponent} from './components/register/register.component';
import {MessageComponent} from './components/message/message.component';
import {HouseholdCreateComponent} from './components/household-create/household-create.component';
import {HouseholdMembersComponent} from './components/household-members/household-members.component';
import {HouseholdInvitationsComponent} from './components/household-invitations/household-invitations.component';
import {InvitationAcceptComponent} from './components/invitation-accept/invitation-accept.component';
import {TaskListComponent} from './components/task-list/task-list.component';
import {TaskCreateComponent} from './components/task-create/task-create.component';
import {TaskEditComponent} from './components/task-edit/task-edit.component';
import {ExpensesComponent} from './components/expenses/expenses.component';
import {ShoppingItemListComponent} from './components/shopping-item-list/shopping-item-list.component';
import {ShoppingItemCreateComponent} from './components/shopping-item-create/shopping-item-create.component';
import {ShoppingItemEditComponent} from './components/shopping-item-edit/shopping-item-edit.component';
import {CleaningScheduleListComponent} from './components/cleaning-schedule-list/cleaning-schedule-list.component';
import {CleaningScheduleCreateComponent} from './components/cleaning-schedule-create/cleaning-schedule-create.component';
import {CleaningScheduleEditComponent} from './components/cleaning-schedule-edit/cleaning-schedule-edit.component';
import {HistoryComponent} from './components/history/history.component';
import {SidebarComponent} from './components/sidebar/sidebar.component';
import {BottomTabBarComponent} from './components/bottom-tab-bar/bottom-tab-bar.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {LucideAngularModule, LayoutDashboard, CheckSquare, CalendarCheck, Users, Mail, User, LogOut, Clock, ShoppingCart, ShoppingBag, Trash2, Pencil, CircleCheck, CircleX, RotateCcw, ReceiptText, HandCoins, Scale, Search, SlidersHorizontal, ChevronDown, Ellipsis} from 'lucide-angular';
import {PaymentStatusComponent} from './components/payment-status/payment-status.component';
import {ProfileComponent} from './components/profile/profile.component';
import {ProfileEditComponent} from './components/profile-edit/profile-edit.component';
import {PrivacyComponent} from './components/privacy/privacy.component';
import {TermsComponent} from './components/terms/terms.component';
import {LegalComponent} from './components/legal/legal.component';
import {NotificationSettingsComponent} from './components/notification-settings/notification-settings.component';
import {NotificationPanelComponent} from './components/notification-panel/notification-panel.component';
import {ConfirmDialogComponent} from './components/confirm-dialog/confirm-dialog.component';
import {MobileNavMenuComponent} from './components/mobile-nav-menu/mobile-nav-menu.component';
import {ShoppingQuantityPipe} from './pipes/shopping-quantity.pipe';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    MessageComponent,
    HouseholdCreateComponent,
    HouseholdMembersComponent,
    HouseholdInvitationsComponent,
    InvitationAcceptComponent,
    TaskListComponent,
    TaskCreateComponent,
    TaskEditComponent,
    ExpensesComponent,
    ShoppingItemListComponent,
    ShoppingItemCreateComponent,
    ShoppingItemEditComponent,
    CleaningScheduleListComponent,
    CleaningScheduleCreateComponent,
    CleaningScheduleEditComponent,
    HistoryComponent,
    SidebarComponent,
    BottomTabBarComponent,
    PaymentStatusComponent,
    ProfileComponent,
    ProfileEditComponent,
    PrivacyComponent,
    TermsComponent,
    LegalComponent,
    NotificationSettingsComponent,
    NotificationPanelComponent,
    ConfirmDialogComponent,
    MobileNavMenuComponent,
    ShoppingQuantityPipe,
  ],
  bootstrap: [AppComponent],
  imports: [BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    LucideAngularModule.pick({LayoutDashboard, CheckSquare, CalendarCheck, Users, Mail, User, LogOut, Clock, ShoppingCart, ShoppingBag, Trash2, Pencil, CircleCheck, CircleX, RotateCcw, ReceiptText, HandCoins, Scale, Search, SlidersHorizontal, ChevronDown, Ellipsis})],
  providers: [
    httpInterceptorProviders,
    provideHttpClient(withInterceptorsFromDi()),
    provideZoneChangeDetection(),
  ] })
export class AppModule {
}
