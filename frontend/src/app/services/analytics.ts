import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface StudentAttendance {
  studentId: number;
  firstName: string;
  lastName: string;
  attended: number;
  totalSessions: number;
  percentage: number;
}

export interface SessionAttendance {
  sessionId: number;
  date: string;
  status: string;
  presentCount: number;
  totalEnrolled: number;
  attendancePercentage: number;
}

export interface BelowThresholdResult {
  threshold: number;
  students: StudentAttendance[];
}

export interface GroupComparison {
  group: string;
  averageAttendancePercentage: number;
  sessionsCounted: number;
}

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private http = inject(HttpClient);
  private baseUrl = '/api/analytics/course';

  getAttendanceByStudent(courseId: number) {
    return this.http.get<StudentAttendance[]>(`${this.baseUrl}/${courseId}/attendance-by-student`);
  }

  getAttendanceBySession(courseId: number) {
    return this.http.get<SessionAttendance[]>(`${this.baseUrl}/${courseId}/attendance-by-session`);
  }

  getBelowThreshold(courseId: number) {
    return this.http.get<BelowThresholdResult>(`${this.baseUrl}/${courseId}/below-threshold`);
  }

  getGroupComparison(courseId: number) {
    return this.http.get<GroupComparison[]>(`${this.baseUrl}/${courseId}/group-comparison`);
  }
}