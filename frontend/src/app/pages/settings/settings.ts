import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SettingsService } from '../../services/settings';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './settings.html',
  styleUrl: './settings.css'
})
export class SettingsComponent implements OnInit {
  private settingsService = inject(SettingsService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  domain = '';
  qrRefreshInterval: number | null = null;

  loading = true;
  savingDomain = false;
  savingInterval = false;
  domainMessage: string | null = null;
  intervalMessage: string | null = null;

  attendanceThreshold: number | null = null;
  savingThreshold = false;
  thresholdMessage: string | null = null;

  private loadedCount = 0;

  // lifecycle

  ngOnInit() {
    this.loadSettings();
  }

  // loading

  loadSettings() {
    this.loading = true;
    this.loadedCount = 0;

    this.settingsService.getSetting('institution.domain').subscribe((res) => {
      this.domain = res.value ?? '';
      this.markLoaded();
    });

    this.settingsService.getSetting('qr.refresh.interval.seconds').subscribe((res) => {
      this.qrRefreshInterval = res.value ? Number(res.value) : 15;
      this.markLoaded();
    });

    this.settingsService.getSetting('attendance.threshold.percentage').subscribe((res) => {
      this.attendanceThreshold = res.value ? Number(res.value) : 60;
      this.markLoaded();
    });
  }

  private markLoaded() {
    this.loadedCount++;
    if (this.loadedCount >= 3) {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  // domain

  onSaveDomain() {
    if (!this.domain.trim()) return;
    this.savingDomain = true;
    this.domainMessage = null;

    this.settingsService.updateSetting('institution.domain', this.domain.trim()).subscribe({
      next: () => {
        this.savingDomain = false;
        this.domainMessage = 'Domen uspješno sačuvan.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.savingDomain = false;
        this.domainMessage = 'Greška pri čuvanju domena.';
        this.cdr.detectChanges();
      }
    });
  }

  // qr refresh interval

  onSaveInterval() {
    if (!this.qrRefreshInterval || this.qrRefreshInterval < 1) return;
    this.savingInterval = true;
    this.intervalMessage = null;

    this.settingsService.updateSetting('qr.refresh.interval.seconds', this.qrRefreshInterval.toString()).subscribe({
      next: () => {
        this.savingInterval = false;
        this.intervalMessage = 'Interval uspješno sačuvan.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.savingInterval = false;
        this.intervalMessage = 'Greška pri čuvanju intervala.';
        this.cdr.detectChanges();
      }
    });
  }

  // navigation

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  onSaveThreshold() {
    if (this.attendanceThreshold === null || this.attendanceThreshold < 0 || this.attendanceThreshold > 100) return;
    this.savingThreshold = true;
    this.thresholdMessage = null;

    this.settingsService.updateSetting('attendance.threshold.percentage', this.attendanceThreshold.toString()).subscribe({
      next: () => {
        this.savingThreshold = false;
        this.thresholdMessage = 'Prag uspješno sačuvan.';
        this.cdr.detectChanges();
      },
      error: () => {
        this.savingThreshold = false;
        this.thresholdMessage = 'Greška pri čuvanju praga.';
        this.cdr.detectChanges();
      }
    });
  }
}