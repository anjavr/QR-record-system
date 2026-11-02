import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import Keycloak from 'keycloak-js';
import { BehaviorSubject } from 'rxjs';

export interface MeResponse {
  role: 'EMPLOYEE' | 'STUDENT';
  data: any;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private keycloak = inject(Keycloak);

  private meSubject = new BehaviorSubject<MeResponse | null>(null);
  me$ = this.meSubject.asObservable();

  login() {
    return this.keycloak.login();
  }

  logout() {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  get isLoggedIn(): boolean {
    return this.keycloak.authenticated ?? false;
  }

  fetchMe() {
  console.log('fetchMe called');
  return this.http.get<MeResponse>('/api/auth/me').subscribe({
    next: (res) => {
      console.log('me response:', res);
      this.meSubject.next(res);
    },
    error: (err) => {
      console.error('me error:', err);
      this.meSubject.next(null);
    }
  });
}
}