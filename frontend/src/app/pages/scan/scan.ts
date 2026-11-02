import { Component, OnInit, OnDestroy, inject, ChangeDetectorRef, ElementRef, ViewChild } from '@angular/core';
import { BrowserQRCodeReader, IScannerControls } from '@zxing/browser';
import { Result } from '@zxing/library';
import { AttendanceService, StudentCourseSummary } from '../../services/attendance';
import { AuthService } from '../../services/auth.service';

type ScanState = 'idle' | 'scanning' | 'checking' | 'success' | 'error';

@Component({
  selector: 'app-scan',
  standalone: true,
  imports: [],
  templateUrl: './scan.html',
  styleUrl: './scan.css'
})
export class ScanComponent implements OnInit, OnDestroy {
  private attendanceService = inject(AttendanceService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  @ViewChild('video') videoElement!: ElementRef<HTMLVideoElement>;

  private codeReader = new BrowserQRCodeReader();
  private controls: IScannerControls | null = null;

  state: ScanState = 'idle';
  resultMessage = '';
  studentName = '';

  myCourses: StudentCourseSummary[] = [];
  coursesLoading = true;

  private pollHandle: any = null;

  private lastKnownAttendedCount: Map<number, number> = new Map();
  removalNoticeMessage: string | null = null;

  // lifecycle

  ngOnInit() {
    this.authService.me$.subscribe((me) => {
      if (me && me.role === 'STUDENT') {
        this.studentName = `${me.data.firstName} ${me.data.lastName}`;
        this.cdr.detectChanges();
      }
    });
    this.authService.fetchMe();
    this.loadMyCourses();

    this.pollHandle = setInterval(() => this.loadMyCourses(), 10000);
  }

  ngOnDestroy() {
    this.stopScanning();
    if (this.pollHandle) {
      clearInterval(this.pollHandle);
      this.pollHandle = null;
    }
  }

  // courses overview

  loadMyCourses(onLoaded?: () => void) {
    this.coursesLoading = true;
    this.attendanceService.getMyCourses().subscribe({
      next: (courses) => {
        for (const course of courses) {
          const previous = this.lastKnownAttendedCount.get(course.courseId);
          if (previous !== undefined && course.attendedSessions < previous) {
            this.lastKnownAttendedCount.set(course.courseId, course.attendedSessions);
            this.showRemovalNotice();
          }
        }
        this.myCourses = courses;
        this.coursesLoading = false;
        this.cdr.detectChanges();
        if (onLoaded) onLoaded();
      },
      error: () => {
        this.coursesLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private showRemovalNotice() {
    this.removalNoticeMessage = 'Zaposleni je uklonio jednu od tvojih prijava. Ako smatraš da je ovo greška, obrati se predmetnom nastavniku.';
    this.state = 'idle';
    this.cdr.detectChanges();

    setTimeout(() => {
      this.removalNoticeMessage = null;
      this.cdr.detectChanges();
    }, 6000);
  }

  // camera / scanning

  async startScanning() {
    this.state = 'scanning';
    this.resultMessage = '';
    this.cdr.detectChanges();

    try {
      this.controls = await this.codeReader.decodeFromVideoDevice(
        undefined,
        this.videoElement.nativeElement,
        (result: Result | undefined, error: unknown) => {
          if (result) {
            this.onQrDetected(result.getText());
          }
        }
      );
    } catch (err) {
      this.state = 'error';
      this.resultMessage = 'Nije moguće pristupiti kameri. Provjeri dozvole u browseru.';
      this.cdr.detectChanges();
    }
  }

  stopScanning() {
    if (this.controls) {
      this.controls.stop();
      this.controls = null;
    }
  }

  private onQrDetected(token: string) {
    if (this.state === 'checking') return;

    this.stopScanning();
    this.state = 'checking';
    this.cdr.detectChanges();

    this.attendanceService.checkIn(token).subscribe({
      next: () => {
        this.state = 'success';
        this.resultMessage = 'Prisustvo uspješno evidentirano!';
        this.loadMyCourses(() => {
          for (const course of this.myCourses) {
            this.lastKnownAttendedCount.set(course.courseId, course.attendedSessions);
          }
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.state = 'error';
        this.resultMessage = this.mapErrorMessage(err);
        this.cdr.detectChanges();
      }
    });
  }

  private mapErrorMessage(err: any): string {
    const status = err.status;
    const backendMessage: string | undefined = err.error?.message;

    if (status === 0) {
      return 'Nema internet konekcije. Provjeri mrežu i pokušaj ponovo.';
    }

    if (status === 409) {
      if (backendMessage?.includes('already recorded')) {
        return 'Već si evidentiran/a za ovu sesiju.';
      }
      if (backendMessage?.includes('expired') || backendMessage?.includes('refreshed')) {
        return 'QR kod je istekao ili je promijenjen. Skeniraj trenutno prikazani kod.';
      }
      if (backendMessage?.includes('not active')) {
        return 'Sesija je zatvorena, prijava nije moguća.';
      }
      return backendMessage ?? 'Došlo je do konflikta pri prijavi.';
    }

    if (status === 403) {
      if (backendMessage?.includes('not enrolled')) {
        return 'Nisi upisan/a na ovaj predmet.';
      }
      if (backendMessage?.includes('does not belong to the group')) {
        return `Ova sesija je namijenjena drugoj grupi. ${backendMessage}`;
      }
      return backendMessage ?? 'Nemaš pravo prijave na ovu sesiju.';
    }

    if (status === 400) {
      return 'Nevažeći QR kod.';
    }

    return 'Došlo je do greške. Pokušaj ponovo.';
  }

  scanAgain() {
    this.startScanning();
  }

  // account

  onLogoutClick() {
    this.authService.logout();
  }
}