import {BrowserModule} from '@angular/platform-browser';
import {NgModule, provideZoneChangeDetection} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {FooterComponent} from './components/footer/footer.component';
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
import {CleaningScheduleListComponent} from './components/cleaning-schedule-list/cleaning-schedule-list.component';
import {CleaningScheduleCreateComponent} from './components/cleaning-schedule-create/cleaning-schedule-create.component';
import {CleaningScheduleEditComponent} from './components/cleaning-schedule-edit/cleaning-schedule-edit.component';
import {SidebarComponent} from './components/sidebar/sidebar.component';
import {BottomTabBarComponent} from './components/bottom-tab-bar/bottom-tab-bar.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {httpInterceptorProviders} from './interceptors';
import {LucideAngularModule, LayoutDashboard, CheckSquare, CalendarCheck, Users, Mail, User, LogOut, Clock} from 'lucide-angular';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
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
    CleaningScheduleListComponent,
    CleaningScheduleCreateComponent,
    CleaningScheduleEditComponent,
    SidebarComponent,
    BottomTabBarComponent,
  ],
  bootstrap: [AppComponent],
  imports: [BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    LucideAngularModule.pick({LayoutDashboard, CheckSquare, CalendarCheck, Users, Mail, User, LogOut, Clock})],
  providers: [
    httpInterceptorProviders,
    provideHttpClient(withInterceptorsFromDi()),
    provideZoneChangeDetection(),
  ] })
export class AppModule {
}

