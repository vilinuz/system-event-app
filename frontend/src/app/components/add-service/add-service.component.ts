import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ApiService, CreateServiceRequest } from '../../services/api.service';

@Component({
  selector: 'app-add-service',
  imports: [FormsModule],
  templateUrl: './add-service.component.html',
  styleUrl: './add-service.component.css',
})
export class AddServiceComponent {
  private readonly apiService = inject(ApiService);
  private readonly router = inject(Router);

  readonly name = signal('');
  readonly environment = signal<'DEV' | 'STAGING' | 'PROD'>('DEV');
  readonly owner = signal('');
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal(false);

  onSubmit() {
    if (!this.name() || !this.owner()) {
      this.error.set('Please fill in all required fields.');
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const request: CreateServiceRequest = {
      name: this.name(),
      environment: this.environment(),
      owner: this.owner(),
    };

    this.apiService.createService(request).subscribe({
      next: () => {
        this.success.set(true);
        setTimeout(() => this.router.navigate(['/']), 1200);
      },
      error: (err) => {
        this.submitting.set(false);
        this.error.set(err?.error?.error || 'Failed to create service.');
      },
    });
  }

  goBack() {
    this.router.navigate(['/']);
  }
}
