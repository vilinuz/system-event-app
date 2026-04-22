import { Component, signal, computed, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GraphqlService, EventItem } from '../../services/graphql.service';
import { ApiService, ServiceItem } from '../../services/api.service';

/**
 * Event log view component.
 * <p>
 * Architecture Note:
 * This component demonstrates the dual-API approach by fetching its data via GraphQL.
 * Unlike the REST-based dashboard, this component requests exactly the fields it needs
 * (including nested `service` data) in a single query. It also uses Signals for state
 * and `computed` for client-side filtering.
 * </p>
 */
@Component({
  selector: 'app-event-log',
  templateUrl: './event-log.component.html',
  styleUrl: './event-log.component.css',
})
export class EventLogComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly graphqlService = inject(GraphqlService);
  private readonly apiService = inject(ApiService);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  readonly service = signal<ServiceItem | null>(null);
  readonly events = signal<EventItem[]>([]);
  readonly loading = signal(true);
  readonly severityFilter = signal('ALL');

  readonly filteredEvents = computed(() => {
    const filter = this.severityFilter();
    if (filter === 'ALL') return this.events();
    return this.events().filter(e => e.severity === filter);
  });

  readonly severityCounts = computed((): Record<string, number> => {
    const evts = this.events();
    return {
      ALL: evts.length,
      INFO: evts.filter(e => e.severity === 'INFO').length,
      WARN: evts.filter(e => e.severity === 'WARN').length,
      ERROR: evts.filter(e => e.severity === 'ERROR').length,
      CRITICAL: evts.filter(e => e.severity === 'CRITICAL').length,
    };
  });

  private serviceId = '';

  ngOnInit() {
    this.serviceId = this.route.snapshot.paramMap.get('serviceId') || '';
    this.loadService();
    this.loadEvents();
    this.refreshInterval = setInterval(() => this.loadEvents(), 10000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) clearInterval(this.refreshInterval);
  }

  loadService() {
    this.apiService.getService(this.serviceId).subscribe({
      next: (s) => this.service.set(s),
      error: () => {},
    });
  }

  loadEvents() {
    this.graphqlService.getEventsByServiceId(this.serviceId).subscribe({
      next: (data) => {
        this.events.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  setSeverityFilter(severity: string) {
    this.severityFilter.set(severity);
  }

  goBack() {
    this.router.navigate(['/']);
  }

  formatTimestamp(iso: string): string {
    const d = new Date(iso);
    return d.toLocaleString('en-US', {
      month: 'short', day: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    });
  }

  badgeClass(severity: string): string {
    return 'badge badge-' + severity.toLowerCase();
  }
}
