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

const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'login', component: LoginComponent},
  {path: 'register', component: RegisterComponent},
  {path: 'message', canActivate: mapToCanActivate([AuthGuard]), component: MessageComponent},
  {path: 'household/create', component: HouseholdCreateComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'household/members', component: HouseholdMembersComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'household/invitations', component: HouseholdInvitationsComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'invite/:token', component: InvitationAcceptComponent},
  {path: 'tasks', component: TaskListComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'tasks/create', component: TaskCreateComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'tasks/:id/edit', component: TaskEditComponent, canActivate: mapToCanActivate([AuthGuard])},
  {path: 'expenses', component: ExpensesComponent, canActivate: mapToCanActivate([AuthGuard])}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
