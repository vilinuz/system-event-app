import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './services/auth.interceptor';

/**
 * Global application configuration.
 * <p>
 * Architecture Note:
 * We use `provideZonelessChangeDetection()` here to completely opt-out of zone.js.
 * This modern Angular 21 approach significantly reduces bundle size, improves performance,
 * and forces us to use Signals for all reactivity, leading to more predictable state management.
 * </p>
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
  ]
};
