import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Course {
  id: number;
  code: string;
  name: string;
  studyProgram: string;
  studyType: string;
  studyYear: number;
  semester: number;
  teachingForm: string;
  groupNumber: number;
  studentCount: number;
  academicYear:number;
}

@Injectable({ providedIn: 'root' })
export class CourseService {
  private http = inject(HttpClient);
  private baseUrl = '/api/courses';

  getMyCourses() {
    return this.http.get<Course[]>(`${this.baseUrl}/mine`);
  }

  uploadCourse(formData: FormData) {
    return this.http.post<{
      courseName: string;
      studentsProcessed: number;
      studentsCreated: number;
      studentsLinked: number;
      studentsUpdated: number;
    }>(`${this.baseUrl}/upload`, formData);
  }

  downloadCourseExport(courseId: number, format: 'excel' | 'pdf') {
    const url = `${this.baseUrl}/${courseId}/export/${format}`;
    this.http.get(url, { responseType: 'blob' }).subscribe((blob) => {
      const filename = `predmet-${courseId}-kumulativno.${format === 'excel' ? 'xlsx' : 'pdf'}`;
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
}