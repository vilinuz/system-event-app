import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ServiceItem {
  id: string;
  name: string;
  environment: 'DEV' | 'STAGING' | 'PROD';
  owner: string;
  status: 'HEALTHY' | 'DEGRADED' | 'DOWN';
  createdAt: string;
  updatedAt: string;
}

export interface CreateServiceRequest {
  name: string;
  environment: 'DEV' | 'STAGING' | 'PROD';
  owner: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8081/api';

  getServices(): Observable<ServiceItem[]> {
    return this.http.get<ServiceItem[]>(`${this.baseUrl}/services`);
  }

  getService(id: string): Observable<ServiceItem> {
    return this.http.get<ServiceItem>(`${this.baseUrl}/services/${id}`);
  }

  createService(request: CreateServiceRequest): Observable<ServiceItem> {
    return this.http.post<ServiceItem>(`${this.baseUrl}/services`, request);
  }

  deleteService(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/services/${id}`);
  }
}
