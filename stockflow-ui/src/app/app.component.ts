import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  template: `
    <div class="min-h-screen bg-gray-50">
      <nav *ngIf="auth.isLoggedIn()" class="bg-primary text-white shadow-lg">
        <div class="max-w-7xl mx-auto px-4">
          <div class="flex items-center justify-between h-16">
            <div class="flex items-center space-x-1">
              <span class="text-xl font-bold tracking-tight">StockFlow</span>
            </div>
            <div class="flex items-center space-x-4">
              <a routerLink="/dashboard" routerLinkActive="bg-accent rounded px-3 py-1"
                 class="px-3 py-1 rounded hover:bg-accent transition">Dashboard</a>
              <a routerLink="/portfolio" routerLinkActive="bg-accent rounded px-3 py-1"
                 class="px-3 py-1 rounded hover:bg-accent transition">Portfolio</a>
              <a routerLink="/market" routerLinkActive="bg-accent rounded px-3 py-1"
                 class="px-3 py-1 rounded hover:bg-accent transition">Market</a>
              <a routerLink="/profile" routerLinkActive="bg-accent rounded px-3 py-1"
                 class="px-3 py-1 rounded hover:bg-accent transition">Profile</a>
              <button (click)="logout()" class="ml-4 px-3 py-1 bg-red-500 rounded hover:bg-red-600 transition">
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>
      <main class="max-w-7xl mx-auto px-4 py-6">
        <router-outlet></router-outlet>
      </main>
    </div>
  `
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
