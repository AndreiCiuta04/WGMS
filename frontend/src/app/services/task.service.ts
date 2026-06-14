import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  FairChoreRecommendationDto,
  TaskCreateDto,
  TaskDetailDto,
  TaskStatusUpdateDto,
  TaskUpdateDto
} from '../dtos/task';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private baseUri: string = this.globals.backendUri + '/tasks';

  constructor(private http: HttpClient, private globals: Globals) { }

  create(task: TaskCreateDto): Observable<TaskDetailDto> {
    return this.http.post<TaskDetailDto>(this.baseUri, task);
  }

  findAll(): Observable<TaskDetailDto[]> {
    return this.http.get<TaskDetailDto[]>(this.baseUri);
  }

  findAssignedToMe(): Observable<TaskDetailDto[]> {
    return this.http.get<TaskDetailDto[]>(`${this.baseUri}/my`);
  }

  getFairBalancingRecommendation(params?: {
    historyDays?: number;
    taskTitle?: string;
    dueDate?: string;
    cleaningScheduleId?: number;
  }): Observable<FairChoreRecommendationDto> {
    const queryParams: any = {};
    if (params?.historyDays) {
      queryParams.historyDays = params.historyDays;
    }
    if (params?.taskTitle) {
      queryParams.taskTitle = params.taskTitle;
    }
    if (params?.dueDate) {
      queryParams.dueDate = params.dueDate;
    }
    if (params?.cleaningScheduleId) {
      queryParams.cleaningScheduleId = params.cleaningScheduleId;
    }
    return this.http.get<FairChoreRecommendationDto>(`${this.baseUri}/fair-balancing/recommendation`, {
      params: queryParams
    });
  }

  findOne(id: number): Observable<TaskDetailDto> {
    return this.http.get<TaskDetailDto>(`${this.baseUri}/${id}`);
  }

  update(id: number, task: TaskUpdateDto): Observable<TaskDetailDto> {
    return this.http.put<TaskDetailDto>(`${this.baseUri}/${id}`, task);
  }

  updateStatus(id: number, statusDto: TaskStatusUpdateDto): Observable<TaskDetailDto> {
    return this.http.patch<TaskDetailDto>(`${this.baseUri}/${id}/status`, statusDto);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/${id}`);
  }
}
