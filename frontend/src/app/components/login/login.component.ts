import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly loginForm = this.fb.nonNullable.group({
    username: ['admin', [Validators.required]],
    password: ['admin', [Validators.required]]
  });

  readonly error = signal<string | null>(null);
  readonly isLoading = signal(false);

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading.set(true);
      this.error.set(null);
      
      this.authService.login(this.loginForm.getRawValue()).subscribe({
        next: () => {
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.error.set('Invalid username or password');
        }
      });
    }
  }
}
