import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './components/home/home.component';
import {LoginComponent} from './components/login/login.component';
import {RegisterComponent} from './components/register/register.component';
import {AuthGuard} from './guards/auth.guard';
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
import {PaymentStatusComponent} from './components/payment-status/payment-status.component';
import {ProfileComponent} from './components/profile/profile.component';
import {ProfileEditComponent} from './components/profile-edit/profile-edit.component';
import {PrivacyComponent} from './components/privacy/privacy.component';
import {TermsComponent} from './components/terms/terms.component';
import {LegalComponent} from './components/legal/legal.component';

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'privacy', component: PrivacyComponent},
  {path: 'terms', component: TermsComponent},
  {path: 'legal', component: LegalComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},
  {path: 'profile', component: ProfileComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'profile/edit', component: ProfileEditComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'household/create', component: HouseholdCreateComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'household/members', component: HouseholdMembersComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'household/invitations', component: HouseholdInvitationsComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'invite/:token', component: InvitationAcceptComponent},
  {path: 'tasks', component: TaskListComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'tasks/create', component: TaskCreateComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'tasks/:id/edit', component: TaskEditComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'expenses', component: ExpensesComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'payment-status', component: PaymentStatusComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'history', component: HistoryComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'payment-status', component: PaymentStatusComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'shopping-items', component: ShoppingItemListComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'shopping-items/create', component: ShoppingItemCreateComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'shopping-items/:id/edit', component: ShoppingItemEditComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'cleaning-schedules', component: CleaningScheduleListComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'cleaning-schedules/create', component: CleaningScheduleCreateComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'cleaning-schedules/:id/edit', component: CleaningScheduleEditComponent, canActivate: mapToCanActivate([AuthGuard])}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
