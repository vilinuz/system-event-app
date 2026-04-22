import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'Login — Service Health Monitor',
  },
  {
    path: '',
    loadComponent: () => import('./components/dashboard/dashboard.component').then(m => m.DashboardComponent),
    title: 'Dashboard — Service Health Monitor',
    canActivate: [authGuard],
  },
  {
    path: 'events/:serviceId',
    loadComponent: () => import('./components/event-log/event-log.component').then(m => m.EventLogComponent),
    title: 'Event Log — Service Health Monitor',
    canActivate: [authGuard],
  },
  {
    path: 'services/new',
    loadComponent: () => import('./components/add-service/add-service.component').then(m => m.AddServiceComponent),
    title: 'Add Service — Service Health Monitor',
    canActivate: [authGuard],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
