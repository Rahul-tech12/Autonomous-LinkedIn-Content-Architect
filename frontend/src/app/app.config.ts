import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http'; // <--- Import this

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
   provideHttpClient(withFetch()) , // <--- Add this line
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes)
  ]
};
