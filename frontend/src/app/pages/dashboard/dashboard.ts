import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef } from '@angular/core';
import { CourseService, Course } from '../../services/course';
import { SessionService, Session, AttendanceRecord, CreateSessionRequest } from '../../services/session';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { Router } from '@angular/router';
import { AnalyticsService, StudentAttendance, SessionAttendance, BelowThresholdResult, GroupComparison } from '../../services/analytics';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule, SlicePipe],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private courseService = inject(CourseService);
  private sessionService = inject(SessionService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  // courses
  courses: Course[] = [];
  coursesByYear: Map<number, Course[]> = new Map();
  selectedCourse: Course | null = null;

  // sessions
  sessions: Session[] = [];
  activeTab: 'CREATED' | 'ACTIVE' | 'CLOSED' = 'CREATED';

  // year filter
  selectedAcademicYear: number | null = null;

  // analytics
  private analyticsService = inject(AnalyticsService);

  showAnalytics = false;
  activeAnalyticsTab: 'by-student' | 'by-session' | 'trend' | 'threshold' | 'groups' = 'by-student';
  attendanceByStudent: StudentAttendance[] = [];
  attendanceBySession: SessionAttendance[] = [];
  belowThresholdResult: BelowThresholdResult | null = null;
  groupComparison: GroupComparison[] = [];
  analyticsLoading = false;

  showCreateForm = false;

  newSession: CreateSessionRequest = {
    courseId: 0,
    activityType: 'LECTURE',
    date: '',
    startTime: '',
    groupLabel: ''
  };

  groupLabels: string[] = [];

  // active session (qr code + attendees)
  activeSession: Session | null = null;
  qrCodeBase64: string | null = null;
  attendanceList: AttendanceRecord[] = [];
  private pollHandle: any = null;

  // list upload
  selectedFile: File | null = null;
  uploadMessage: string | null = null;
  showUploadModal = false;
  uploadInProgress = false;
  uploadSuccess = false;
  academicYear: number | null = null;
  showReplaceOption = false;

  // display labels
  statusLabels: Record<string, string> = {
    CREATED: 'Kreirana',
    ACTIVE: 'Aktivna',
    CLOSED: 'Zatvorena'
  };

  activityTypeLabels: Record<string, string> = {
    LECTURE: 'Predavanje',
    TUTORIAL: 'Auditorne vježbe',
    LAB: 'Laboratorijske vježbe'
  };

  // lifecycle

  ngOnInit() {
    this.loadCourses();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  // courses

  loadCourses() {
    this.courseService.getMyCourses().subscribe((courses) => {
      this.courses = courses;
      this.applyAcademicYearFilter();
      this.cdr.detectChanges();
    });
  }

  onAcademicYearFilterChange(value: string) {
    this.selectedAcademicYear = value === 'all' ? null : Number(value);
    this.applyAcademicYearFilter();
    this.cdr.detectChanges();
  }

  private applyAcademicYearFilter() {
    const filtered = this.selectedAcademicYear === null
      ? this.courses
      : this.courses.filter(c => c.academicYear === this.selectedAcademicYear);
    this.groupByYear(filtered);
  }

  private groupByYear(courses: Course[]) {
    this.coursesByYear = new Map();
    for (const course of courses) {
      const year = course.studyYear;
      if (!this.coursesByYear.has(year)) {
        this.coursesByYear.set(year, []);
      }
      this.coursesByYear.get(year)!.push(course);
    }
  }

  get years(): number[] {
    return Array.from(this.coursesByYear.keys()).sort((a, b) => a - b);
  }

  selectCourse(course: Course) {
    this.selectedCourse = course;
    this.activeSession = null;
    this.showAnalytics = false;
    this.stopPolling();
    this.newSession.courseId = course.id;
    this.loadSessions(course.id);
  }

  get academicYears(): number[] {
    const years = new Set(this.courses.map(c => c.academicYear));
    return Array.from(years).sort((a, b) => b - a);
  }
  // sessions

  loadSessions(courseId: number) {
    this.sessionService.getMySessions().subscribe((sessions) => {
      this.sessions = sessions
        .filter(s => s.course.id === courseId)
        .sort((a, b) => {
          const dateTimeA = `${a.date}T${a.startTime}`;
          const dateTimeB = `${b.date}T${b.startTime}`;
          return dateTimeB.localeCompare(dateTimeA);
        });

      const active = this.sessions.find(s => s.status === 'ACTIVE');
      if (active) {
        this.openActiveSession(active);
        this.activeTab = 'ACTIVE';
      } else {
        this.activeTab = 'CREATED';
      }
      this.cdr.detectChanges();
    });
  }

  get filteredSessions(): Session[] {
    return this.sessions.filter(s => s.status === this.activeTab);
  }

  get sessionCounts() {
    return {
      CREATED: this.sessions.filter(s => s.status === 'CREATED').length,
      ACTIVE: this.sessions.filter(s => s.status === 'ACTIVE').length,
      CLOSED: this.sessions.filter(s => s.status === 'CLOSED').length
    };
  }

  selectTab(tab: 'CREATED' | 'ACTIVE' | 'CLOSED') {
    this.activeTab = tab;
  }

  toggleCreateForm() {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm) {
      this.onActivityTypeChange();
    }
  }

  onCreateSession() {
    this.sessionService.createSession(this.newSession).subscribe(() => {
      this.showCreateForm = false;
      if (this.selectedCourse) this.loadSessions(this.selectedCourse.id);
    });
  }

  onActivate(session: Session) {
    this.sessionService.activateSession(session.id).subscribe((updated) => {
      this.openActiveSession(updated);
      if (this.selectedCourse) this.loadSessions(this.selectedCourse.id);
    });
  }

  onClose(session: Session) {
    this.sessionService.closeSession(session.id).subscribe(() => {
      this.stopPolling();
      this.activeSession = null;
      this.qrCodeBase64 = null;
      if (this.selectedCourse) this.loadSessions(this.selectedCourse.id);
    });
  }

  onExportSession(sessionId: number, format: 'excel' | 'pdf') {
    this.sessionService.downloadSessionExport(sessionId, format);
  }

  onExportCourse(format: 'excel' | 'pdf') {
    if (!this.selectedCourse) return;
    this.courseService.downloadCourseExport(this.selectedCourse.id, format);
  }

  // active session - qr code + attendees

  private openActiveSession(session: Session) {
    this.activeSession = session;
    this.refreshQrAndAttendance();
    this.pollHandle = setInterval(() => this.refreshQrAndAttendance(), 5000);
  }

  private refreshQrAndAttendance() {
    if (!this.activeSession) return;
    this.sessionService.getQrCode(this.activeSession.id).subscribe((res) => {
      this.qrCodeBase64 = res.qrCodeBase64;
      this.cdr.detectChanges();
    });
    this.sessionService.getAttendance(this.activeSession.id).subscribe((records) => {
      this.attendanceList = records;
      this.cdr.detectChanges();
    });
  }

  private stopPolling() {
    if (this.pollHandle) {
      clearInterval(this.pollHandle);
      this.pollHandle = null;
    }
  }

  onDeleteAttendanceRecord(recordId: number) {
    if (!this.activeSession) return;

    const confirmed = confirm('Da li si sigurna da želiš ukloniti ovu prijavu?');
    if (!confirmed) return;

    this.sessionService.deleteAttendanceRecord(this.activeSession.id, recordId).subscribe({
      next: () => {
        this.attendanceList = this.attendanceList.filter(r => r.id !== recordId);
        this.cdr.detectChanges();
      },
      error: () => {
        alert('Greška pri brisanju unosa.');
      }
    });
  }

  // list upload

  openUploadModal() {
    this.showUploadModal = true;
    this.selectedFile = null;
    this.uploadMessage = null;
    this.uploadSuccess = false;
    this.showReplaceOption = false;
    this.academicYear = new Date().getFullYear();
  }

  closeUploadModal() {
    this.showUploadModal = false;
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
    this.uploadMessage = null;
  }

  onUpload() {
    if (!this.selectedFile || !this.academicYear) return;
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('academicYear', this.academicYear.toString());

    this.uploadInProgress = true;
    this.uploadMessage = null;

    this.courseService.uploadCourse(formData).subscribe({
      next: (result) => {
        this.uploadInProgress = false;
        this.uploadSuccess = true;
        this.uploadMessage = `Uspješno uploadovano: ${result.courseName} (${result.studentsProcessed} studenata, ${result.studentsCreated} novih, ${result.studentsUpdated} ažuriranih)`;
        this.selectedFile = null;
        this.loadCourses();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.closeUploadModal();
          this.cdr.detectChanges();
        }, 2000);
      },
      error: (err) => {
        this.uploadInProgress = false;
        this.uploadSuccess = false;

        if (err.status === 409) {
          this.uploadMessage = `Predmet za ${this.academicYear}. godinu već postoji (${err.error.courseName}, ${err.error.existingStudentCount} studenata). Klikni "Zamijeni" da obrišeš stari i uploaduješ novi.`;
          this.showReplaceOption = true;
        } else {
          this.uploadMessage = 'Greška pri uploadu — provjeri format fajla.';
        }
        this.cdr.detectChanges();
        console.error('Upload greška:', err);
      }
    });
  }

  onConfirmReplace() {
    if (!this.selectedFile || !this.academicYear) return;
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('academicYear', this.academicYear.toString());
    formData.append('confirmReplace', 'true');

    this.uploadInProgress = true;

    this.courseService.uploadCourse(formData).subscribe({
      next: (result) => {
        this.uploadInProgress = false;
        this.uploadSuccess = true;
        this.showReplaceOption = false;
        this.uploadMessage = `Zamijenjeno: ${result.courseName} (${result.studentsProcessed} studenata, ${result.studentsUpdated} ažuriranih)`;
        this.selectedFile = null;
        this.loadCourses();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.closeUploadModal();
          this.cdr.detectChanges();
        }, 2000);
      },
      error: () => {
        this.uploadInProgress = false;
        this.uploadMessage = 'Greška pri zamjeni predmeta.';
        this.cdr.detectChanges();
      }
    });
  }

  // labels and formatting

  getStatusLabel(status: string): string {
    return this.statusLabels[status] ?? status;
  }

  getActivityLabel(type: string): string {
    return this.activityTypeLabels[type] ?? type;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [year, month, day] = dateStr.split('-');
    return `${day}.${month}.${year}.`;
  }

  formatTime(timeStr: string): string {
    if (!timeStr) return '';
    return timeStr.slice(0, 5);
  }

  // account

  goToSettings() {
    this.router.navigate(['/settings']);
  }

  onLogoutClick() {
    this.authService.logout();
  }

  onActivityTypeChange() {
    this.newSession.groupLabel = '';
    this.sessionService.getGroupLabels(this.newSession.activityType).subscribe((labels) => {
      this.groupLabels = labels;
      this.cdr.detectChanges();
    });
  }

  // analytics

  toggleAnalytics() {
    this.showAnalytics = !this.showAnalytics;
    if (this.showAnalytics && this.selectedCourse) {
      this.loadAnalytics();
    }
  }

  loadAnalytics() {
    if (!this.selectedCourse) return;
    const courseId = this.selectedCourse.id;
    this.analyticsLoading = true;

    this.analyticsService.getAttendanceByStudent(courseId).subscribe((data) => {
      this.attendanceByStudent = data;
      this.cdr.detectChanges();
    });

    this.analyticsService.getAttendanceBySession(courseId).subscribe((data) => {
      this.attendanceBySession = data;
      this.analyticsLoading = false;
      this.cdr.detectChanges();
    });

    this.analyticsService.getBelowThreshold(courseId).subscribe((data) => {
      this.belowThresholdResult = data;
      this.cdr.detectChanges();
    });

    this.analyticsService.getGroupComparison(courseId).subscribe((data) => {
      this.groupComparison = data;
      this.cdr.detectChanges();
    });
  }

  get trendChartPoints(): string {
    if (this.attendanceBySession.length === 0) return '';
    const width = 400;
    const height = 120;
    const step = width / Math.max(this.attendanceBySession.length - 1, 1);
    return this.attendanceBySession
      .map((s, i) => `${i * step},${height - (s.attendancePercentage / 100) * height}`)
      .join(' ');
  }

  selectAnalyticsTab(tab: 'by-student' | 'by-session' | 'trend' | 'threshold' | 'groups') {
  this.activeAnalyticsTab = tab;
}
}