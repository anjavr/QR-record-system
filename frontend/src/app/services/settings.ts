import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Setting {
  key: string;
  value: string | null;
}

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private http = inject(HttpClient);
  private baseUrl = '/api/settings';

  getSetting(key: string) {
    return this.http.get<Setting>(`${this.baseUrl}/${key}`);
  }

  updateSetting(key: string, value: string) {
    return this.http.put<Setting>(`${this.baseUrl}/${key}`, { value });
  }
}