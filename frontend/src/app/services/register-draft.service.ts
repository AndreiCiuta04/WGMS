import { Injectable } from '@angular/core';

export interface RegisterDraft {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  termsAccepted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class RegisterDraftService {

  private draft: RegisterDraft | null = null;

  save(draft: RegisterDraft): void {
    this.draft = { ...draft };
  }

  load(): RegisterDraft | null {
    return this.draft ? { ...this.draft } : null;
  }

  clear(): void {
    this.draft = null;
  }
}
