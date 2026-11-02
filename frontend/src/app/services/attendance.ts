import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface AttendanceRecord {
  id: number;
  checkInTime: string;
  ipAddress: string;
  userAgent: string;
}

export interface StudentCourseSummary {
  courseId: number;
  courseName: string;
  courseCode: string;
  totalSessions: number;
  attendedSessions: number;
  attendancePercentage: number;
  lectureGroup: string | null;
  labGroup: string | null;
}

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private http = inject(HttpClient);
  private baseUrl = '/api/attendance';
  private studentsUrl = '/api/students';

  checkIn(token: string) {
    return this.http.post<AttendanceRecord>(`${this.baseUrl}/check-in`, { token });
  }

  getMyCourses() {
    return this.http.get<StudentCourseSummary[]>(`${this.studentsUrl}/me/courses`);
  }
}