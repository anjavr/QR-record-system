import { Routes } from '@angular/router';
import { LoginComponent} from './pages/login/login';
import { authGuard } from './guards/auth.guard';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { ScanComponent } from './pages/scan/scan';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/dashboard/dashboard').then(m => m.DashboardComponent)
  },
  {
    path: 'scan',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/scan/scan').then(m => m.ScanComponent)
  },
  {
    path: 'settings',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/settings/settings').then(m => m.SettingsComponent)
  }
];