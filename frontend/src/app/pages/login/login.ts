import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import Keycloak from 'keycloak-js';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {
  private keycloak = inject(Keycloak);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  checkingSession = true;
  isAuthenticated = false;
  errorMessage: string | null = null;
  debugMessage = '';

  ngOnInit() {
    this.isAuthenticated = this.keycloak.authenticated ?? false;
    this.checkingSession = false;

    if (this.isAuthenticated) {
      this.checkRoleAndRedirect();
    }
  }

  onLoginClick() {
    this.authService.login();
  }

  private checkRoleAndRedirect() {
  this.authService.me$.subscribe((me) => {
    if (!me) return;
    if (me.role === 'EMPLOYEE') {
      this.router.navigate(['/dashboard']);
    } else if (me.role === 'STUDENT') {
      this.router.navigate(['/scan']);
    }
  });

  this.authService.fetchMe();

  setTimeout(async () => {
    let manualTest = '';
    try {
      const response = await fetch('/api/auth/me', {
        headers: { Authorization: 'Bearer ' + this.keycloak.token }
      });
      const text = await response.text();
      manualTest = 'Status: ' + response.status + ' | Body: ' + text.substring(0, 150);
    } catch (err: any) {
      manualTest = 'Fetch error: ' + err.message;
    }

    this.debugMessage = 'KC auth: ' + this.keycloak.authenticated +
      ' | Manual fetch: ' + manualTest;
    this.cdr.detectChanges();
  }, 2000);
}
}