import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CleaningScheduleCreateDto,
  CleaningScheduleDetailDto,
  CleaningScheduleUpdateDto,
  CleaningScheduleActiveUpdateDto
} from '../dtos/cleaning-schedule';
import { TaskDetailDto } from '../dtos/task';
import { Globals } from '../global/globals';

@Injectable({
  providedIn: 'root'
})
export class CleaningScheduleService {

  private baseUri: string = this.globals.backendUri + '/cleaning-schedules';

  constructor(private http: HttpClient, private globals: Globals) { }

  create(dto: CleaningScheduleCreateDto): Observable<CleaningScheduleDetailDto> {
    return this.http.post<CleaningScheduleDetailDto>(this.baseUri, dto);
  }

  findAll(): Observable<CleaningScheduleDetailDto[]> {
    return this.http.get<CleaningScheduleDetailDto[]>(this.baseUri);
  }

  findActive(): Observable<CleaningScheduleDetailDto[]> {
    return this.http.get<CleaningScheduleDetailDto[]>(`${this.baseUri}/active`);
  }

  findOne(id: number): Observable<CleaningScheduleDetailDto> {
    return this.http.get<CleaningScheduleDetailDto>(`${this.baseUri}/${id}`);
  }

  update(id: number, dto: CleaningScheduleUpdateDto): Observable<CleaningScheduleDetailDto> {
    return this.http.put<CleaningScheduleDetailDto>(`${this.baseUri}/${id}`, dto);
  }

  updateActive(id: number, dto: CleaningScheduleActiveUpdateDto): Observable<CleaningScheduleDetailDto> {
    return this.http.patch<CleaningScheduleDetailDto>(`${this.baseUri}/${id}/active`, dto);
  }

  generateTask(id: number): Observable<TaskDetailDto> {
    return this.http.post<TaskDetailDto>(`${this.baseUri}/${id}/generate-task`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUri}/${id}`);
  }
}
