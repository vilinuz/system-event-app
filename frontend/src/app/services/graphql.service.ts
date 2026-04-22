import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface EventItem {
  id: string;
  severity: 'INFO' | 'WARN' | 'ERROR' | 'CRITICAL';
  message: string;
  timestamp: string;
  service: {
    id: string;
    name: string;
    environment: string;
    owner: string;
    status: string;
  };
}

interface GraphQLResponse<T> {
  data: T;
}

@Injectable({ providedIn: 'root' })
export class GraphqlService {
  private readonly http = inject(HttpClient);
  private readonly graphqlUrl = 'http://localhost:8081/graphql';

  getEventsByServiceId(serviceId: string, severity?: string): Observable<EventItem[]> {
    const query = `
      query GetEvents($serviceId: ID, $severity: String) {
        events(serviceId: $serviceId, severity: $severity) {
          id
          severity
          message
          timestamp
          service {
            id
            name
            environment
            owner
            status
          }
        }
      }
    `;

    const variables: Record<string, string | undefined> = { serviceId };
    if (severity && severity !== 'ALL') {
      variables['severity'] = severity;
    }

    return this.http
      .post<GraphQLResponse<{ events: EventItem[] }>>(this.graphqlUrl, { query, variables })
      .pipe(map(res => res.data.events));
  }

  getEventsByServiceName(serviceName: string): Observable<EventItem[]> {
    const query = `
      query GetEventsByService($serviceName: String!) {
        eventsByService(serviceName: $serviceName) {
          id
          severity
          message
          timestamp
          service {
            id
            name
            environment
            owner
            status
          }
        }
      }
    `;

    return this.http
      .post<GraphQLResponse<{ eventsByService: EventItem[] }>>(this.graphqlUrl, {
        query,
        variables: { serviceName },
      })
      .pipe(map(res => res.data.eventsByService));
  }
}
