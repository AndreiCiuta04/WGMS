import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HistoryComponent } from './history.component';
import { HistoryService } from '../../services/history.service';
import { HistoryDto } from '../../dtos/history';
import { LucideAngularModule, CheckSquare, ReceiptText } from 'lucide-angular';

describe('HistoryComponent', () => {
  let component: HistoryComponent;
  let fixture: ComponentFixture<HistoryComponent>;
  let serviceSpy: jasmine.SpyObj<HistoryService>;

  const history: HistoryDto = {
    tasks: [
      {
        id: 1,
        householdId: 10,
        cleaningScheduleId: 2,
        title: 'Clean kitchen',
        description: 'Wipe counters',
        dueDate: '2026-05-17',
        status: 'DONE',
        createdByEmail: 'owner@test.com',
        assignedToEmail: 'roommate@test.com',
        createdAt: '2026-05-16T10:00:00',
        updatedAt: '2026-05-17T11:00:00'
      }
    ],
    payments: [
      {
        id: 5,
        title: 'Groceries',
        amount: 24.5,
        status: 'RECORDED',
        payerEmail: 'payer@test.com',
        receiverEmail: null,
        createdAt: '2026-05-18T12:00:00',
        updatedAt: '2026-05-18T12:00:00'
      }
    ]
  };

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('HistoryService', ['getHistory']);
    serviceSpy.getHistory.and.returnValue(of(history));

    await TestBed.configureTestingModule({
      declarations: [HistoryComponent],
      imports: [
        LucideAngularModule.pick({CheckSquare, ReceiptText})
      ],
      providers: [{ provide: HistoryService, useValue: serviceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(HistoryComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('calls HistoryService.getHistory on init', () => {
    fixture.detectChanges();
    expect(serviceSpy.getHistory).toHaveBeenCalled();
  });

  it('displays task history when returned', () => {
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Clean kitchen');
    expect(text).toContain('DONE');
    expect(text).toContain('roommate@test.com');
    expect(text).toContain('Schedule #2');
  });

  it('displays payment history when returned', () => {
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Groceries');
    expect(text).toContain('RECORDED');
    expect(text).toContain('payer@test.com');
    expect(text).toContain('Recorded expense/payment-related activity');
  });

  it('displays empty states when arrays are empty', () => {
    serviceSpy.getHistory.and.returnValue(of({ tasks: [], payments: [] }));
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No completed or cancelled tasks yet.');
    expect(text).toContain('No payment or expense history yet.');
  });

  it('displays an error message when loading fails', () => {
    serviceSpy.getHistory.and.returnValue(throwError(() => ({
      status: 500,
      error: { message: 'History unavailable' }
    })));
    fixture.detectChanges();
    expect(component.errorMessage).toBe('History unavailable');
    expect(fixture.nativeElement.textContent).toContain('History unavailable');
    expect(component.loading).toBeFalse();
  });
});
