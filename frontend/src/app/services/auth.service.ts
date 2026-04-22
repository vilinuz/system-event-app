import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';

export interface AuthResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = 'http://localhost:8081/api/auth';
  
  readonly token = signal<string | null>(localStorage.getItem('jwt_token'));
  readonly isAuthenticated = signal<boolean>(!!this.token());

  login(credentials: any) {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('jwt_token', response.token);
        this.token.set(response.token);
        this.isAuthenticated.set(true);
        this.router.navigate(['/']);
      })
    );
  }

  logout() {
    localStorage.removeItem('jwt_token');
    this.token.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }
}
