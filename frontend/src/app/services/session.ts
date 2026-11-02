import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Session {
  id: number;
  activityType: string;
  status: 'CREATED' | 'ACTIVE' | 'CLOSED';
  date: string;
  startTime: string;
  groupNumber: number;
  course: { id: number; name: string };
}

export interface AttendanceRecord {
  id: number;
  checkInTime: string;
  ipAddress: string;
  userAgent: string;
  student: {
    firstName: string;
    lastName: string;
    studentNumber: string;
  };
  suspicious: boolean;
}

export interface CreateSessionRequest {
  courseId: number;
  activityType: string;
  date: string;
  startTime: string;
  groupLabel: string;
}

@Injectable({ providedIn: 'root' })
export class SessionService {
  private http = inject(HttpClient);
  private baseUrl = '/api/sessions';

  getMySessions() {
    return this.http.get<Session[]>(`${this.baseUrl}/mine`);
  }

  createSession(request: CreateSessionRequest) {
    return this.http.post<Session>(this.baseUrl, request);
  }

  activateSession(id: number) {
    return this.http.post<Session>(`${this.baseUrl}/${id}/activate`, {});
  }

  closeSession(id: number) {
    return this.http.post<Session>(`${this.baseUrl}/${id}/close`, {});
  }

  getQrCode(id: number) {
    return this.http.get<{ qrCodeBase64: string }>(`${this.baseUrl}/${id}/qr`);
  }

  getAttendance(id: number) {
    return this.http.get<AttendanceRecord[]>(`${this.baseUrl}/${id}/attendance`);
  }

  deleteAttendanceRecord(sessionId: number, recordId: number) {
    return this.http.delete<void>(`${this.baseUrl}/${sessionId}/attendance/${recordId}`);
  }

  downloadSessionExport(sessionId: number, format: 'excel' | 'pdf') {
    const url = `${this.baseUrl}/${sessionId}/export/${format}`;
    this.http.get(url, { responseType: 'blob' }).subscribe((blob) => {
      const filename = `sesija-${sessionId}.${format === 'excel' ? 'xlsx' : 'pdf'}`;
      this.triggerDownload(blob, filename);
    });
  }

  private triggerDownload(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  getGroupLabels(activityType: string) {
    return this.http.get<string[]>(`${this.baseUrl}/group-labels?activityType=${activityType}`);
  }
}