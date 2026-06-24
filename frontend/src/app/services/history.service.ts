import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Globals } from '../global/globals';
import { HistoryDto, ExpenseStatisticsDto, ShoppingStatisticsDto,  StatisticsFilter,  StatisticsOverviewDto,  TaskStatisticsDto } from '../dtos/history';

@Injectable({
  providedIn: 'root'
})
export class HistoryService {

  private baseUri: string = this.globals.backendUri + '/history';

  constructor(private http: HttpClient, private globals: Globals) { }

  getHistory(): Observable<HistoryDto> {
    return this.http.get<HistoryDto>(this.baseUri);
  }

  getStatisticsOverview(filter: StatisticsFilter): Observable<StatisticsOverviewDto> {
    return this.http.get<StatisticsOverviewDto>(`${this.baseUri}/statistics/overview`, {
      params: this.statisticsParams(filter, true)
    });
  }

  getTaskStatistics(filter: StatisticsFilter): Observable<TaskStatisticsDto> {
    return this.http.get<TaskStatisticsDto>(`${this.baseUri}/statistics/tasks`, {
      params: this.statisticsParams(filter, false)
    });
  }

  getExpenseStatistics(filter: StatisticsFilter): Observable<ExpenseStatisticsDto> {
    return this.http.get<ExpenseStatisticsDto>(`${this.baseUri}/statistics/expenses`, {
      params: this.statisticsParams(filter, false)
    });
  }

  getShoppingStatistics(filter: StatisticsFilter): Observable<ShoppingStatisticsDto> {
    return this.http.get<ShoppingStatisticsDto>(`${this.baseUri}/statistics/shopping`, {
      params: this.statisticsParams(filter, false)
    });
  }

  private statisticsParams(filter: StatisticsFilter, includeModules: boolean): HttpParams {
    let params = new HttpParams()
      .set('from', filter.from)
      .set('to', filter.to)
      .set('grouping', filter.grouping);

    if (includeModules) {
      for (const module of filter.modules ?? []) {
        params = params.append('modules', module);
      }
    }

    return params;
  }
}
