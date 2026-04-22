import { Component, signal, computed, inject, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService, ServiceItem } from '../../services/api.service';

/**
 * Dashboard view component.
 * <p>
 * Architecture Note:
 * This component is built exclusively using Angular Signals (`signal`, `computed`).
 * By using Signals instead of RxJS `BehaviorSubject`s or standard properties, we achieve
 * fine-grained reactivity that works perfectly with our Zoneless application config.
 * When `services` updates, all `computed` properties (like `healthyCount`, `filteredServices`)
 * automatically and efficiently recalculate without triggering a full component tree check.
 * </p>
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly apiService = inject(ApiService);
  private readonly router = inject(Router);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  readonly services = signal<ServiceItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly searchQuery = signal('');

  readonly filteredServices = computed(() => {
    const query = this.searchQuery().toLowerCase();
    if (!query) return this.services();
    return this.services().filter(s =>
      s.name.toLowerCase().includes(query) ||
      s.owner.toLowerCase().includes(query) ||
      s.environment.toLowerCase().includes(query)
    );
  });

  readonly totalServices = computed(() => this.services().length);
  readonly healthyCount = computed(() => this.services().filter(s => s.status === 'HEALTHY').length);
  readonly degradedCount = computed(() => this.services().filter(s => s.status === 'DEGRADED').length);
  readonly downCount = computed(() => this.services().filter(s => s.status === 'DOWN').length);

  ngOnInit() {
    this.loadServices();
    this.refreshInterval = setInterval(() => this.loadServices(), 15000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  loadServices() {
    this.apiService.getServices().subscribe({
      next: (data) => {
        this.services.set(data);
        this.loading.set(false);
        this.error.set(null);
      },
      error: (err) => {
        this.error.set('Failed to load services. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  onSearch(event: Event) {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  navigateToEvents(serviceId: string) {
    this.router.navigate(['/events', serviceId]);
  }

  navigateToAdd() {
    this.router.navigate(['/services/new']);
  }

  formatTime(isoString: string): string {
    return new Date(isoString).toLocaleTimeString('en-US', {
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    });
  }
}
