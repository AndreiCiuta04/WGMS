import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TaskCreateDto, TaskDetailDto, TaskStatusUpdateDto, TaskUpdateDto } from '../dtos/task';
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
