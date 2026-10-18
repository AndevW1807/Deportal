import { Routes } from '@angular/router';
import { activeSessionGuard } from './auth/guards/active-session-guard';
import { adminGuard, signedInGuard } from './auth/guards/admin-guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [activeSessionGuard],
    loadComponent: () => import('./home/home').then((module) => module.Home),
  },
  {
    path: 'login',
    canActivate: [activeSessionGuard],
    loadComponent: () => import('./auth/login/login').then((module) => module.Login),
  },
  {
    path: 'register',
    canActivate: [activeSessionGuard],
    loadComponent: () => import('./auth/register/register').then((module) => module.Register),
  },
  {
    path: 'app',
    canActivate: [signedInGuard],
    loadComponent: () =>
      import('./admin/admin-shell/admin-shell').then((module) => module.AdminShell),
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./admin/admin-shell/admin-shell').then((module) => module.AdminShell),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'courts' },
      {
        path: 'courts',
        loadComponent: () => import('./admin/courts/courts').then((module) => module.Courts),
      },
      {
        path: 'users',
        loadComponent: () => import('./admin/users/users').then((module) => module.Users),
      },
      {
        path: 'reports',
        loadComponent: () => import('./admin/reports/reports').then((module) => module.Reports),
      },
    ],
  },
];
