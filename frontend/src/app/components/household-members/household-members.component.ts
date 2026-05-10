import { Component, OnInit } from '@angular/core';
import { HouseholdService } from '../../services/household.service';
import { Roommate } from '../../dtos/roommate';
import { Household } from '../../dtos/household';

@Component({
  selector: 'app-household-members',
  templateUrl: './household-members.component.html',
  styleUrls: ['./household-members.component.scss'],
  standalone: false
})
export class HouseholdMembersComponent implements OnInit {

  household: Household | null = null;
  members: Roommate[] = [];
  errorMessage: string = '';

  constructor(private householdService: HouseholdService) { }

  ngOnInit(): void {
    this.householdService.getMyHousehold().subscribe({
      next: h => {
        this.household = h;
        this.loadMembers(h.id);
      },
      error: err => {
        this.errorMessage = 'Could not load household information.';
      }
    });
  }

  loadMembers(id: number): void {
    this.householdService.getMembers(id).subscribe({
      next: members => this.members = members,
      error: err => this.errorMessage = 'Could not load members.'
    });
  }

}
