import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, CommonModule],
  template: `
    <nav class="navbar">
      <div class="navbar-inner container">
        <a routerLink="/" class="navbar-brand">
          <span class="brand-icon">⚡</span>
          <span class="brand-text">ServiceMonitor</span>
        </a>

        <ng-container *ngIf="authService.isAuthenticated()">
          <div class="navbar-links">
            <a routerLink="/"
               routerLinkActive="active"
               [routerLinkActiveOptions]="{ exact: true }"
               class="nav-link"
               id="nav-dashboard">
              <span class="nav-icon">📊</span>
              Dashboard
            </a>
            <a routerLink="/services/new"
               routerLinkActive="active"
               class="nav-link"
               id="nav-add-service">
              <span class="nav-icon">➕</span>
              Add Service
            </a>
          </div>

          <div class="navbar-actions">
            <div class="navbar-status">
              <span class="status-dot"></span>
              <span class="status-text">Live</span>
            </div>
            <button class="nav-link logout-btn" (click)="authService.logout()">
              Logout
            </button>
          </div>
        </ng-container>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      position: sticky;
      top: 0;
      z-index: 100;
      background: rgba(10, 14, 26, 0.85);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border-bottom: 1px solid var(--border-glass);
    }

    .navbar-inner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 64px;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 1.15rem;
      font-weight: 700;
      color: var(--text-primary) !important;
      -webkit-text-fill-color: initial;
    }

    .brand-icon {
      font-size: 1.4rem;
    }

    .brand-text {
      background: var(--accent-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .navbar-links {
      display: flex;
      gap: 4px;
    }

    .navbar-actions {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--text-secondary);
      border-radius: var(--radius-sm);
      transition: all var(--transition-fast);
      cursor: pointer;
      background: transparent;
      border: none;
      font-family: inherit;
    }

    .nav-link:hover {
      color: var(--text-primary);
      background: var(--bg-glass);
    }

    .nav-link.active {
      color: var(--accent-cyan);
      background: rgba(34, 211, 238, 0.08);
    }

    .logout-btn {
      color: var(--status-critical);
    }
    
    .logout-btn:hover {
      background: rgba(239, 68, 68, 0.1);
    }

    .nav-icon {
      font-size: 1rem;
    }

    .navbar-status {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 6px 14px;
      background: rgba(52, 211, 153, 0.08);
      border: 1px solid rgba(52, 211, 153, 0.2);
      border-radius: 100px;
    }

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--status-healthy);
      animation: pulse-dot 2s ease-in-out infinite;
    }

    .status-text {
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--status-healthy);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
  `],
})
export class NavbarComponent {
  authService = inject(AuthService);
}
