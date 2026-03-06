import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="flex items-center justify-center min-h-[80vh]">
      <div class="bg-white rounded-xl shadow-lg p-8 w-full max-w-md">
        <h1 class="text-3xl font-bold text-primary text-center mb-2">StockFlow</h1>
        <p class="text-gray-500 text-center mb-6">Portfolio Management Platform</p>

        <div class="flex mb-6 border-b">
          <button (click)="isLogin = true"
                  [class.border-b-2]="isLogin" [class.border-accent]="isLogin" [class.text-accent]="isLogin"
                  class="flex-1 pb-2 text-center font-medium transition">Login</button>
          <button (click)="isLogin = false"
                  [class.border-b-2]="!isLogin" [class.border-accent]="!isLogin" [class.text-accent]="!isLogin"
                  class="flex-1 pb-2 text-center font-medium transition">Register</button>
        </div>

        <div *ngIf="error" class="bg-red-50 text-red-600 p-3 rounded mb-4 text-sm">{{ error }}</div>
        <div *ngIf="success" class="bg-green-50 text-green-600 p-3 rounded mb-4 text-sm">{{ success }}</div>

        <form (ngSubmit)="onSubmit()">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Username</label>
            <input [(ngModel)]="username" name="username" required
                   class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-accent focus:border-accent outline-none" />
          </div>

          <div *ngIf="!isLogin" class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input [(ngModel)]="email" name="email" type="email" required
                   class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-accent focus:border-accent outline-none" />
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input [(ngModel)]="password" name="password" type="password" required
                   class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-accent focus:border-accent outline-none" />
          </div>

          <button type="submit" [disabled]="loading"
                  class="w-full bg-accent text-white py-2 rounded-lg hover:bg-blue-600 transition font-medium disabled:opacity-50">
            {{ loading ? 'Please wait...' : (isLogin ? 'Login' : 'Register') }}
          </button>
        </form>
      </div>
    </div>
  `
})
export class LoginComponent {
  isLogin = true;
  username = '';
  email = '';
  password = '';
  error = '';
  success = '';
  loading = false;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit() {
    this.error = '';
    this.success = '';
    this.loading = true;

    if (this.isLogin) {
      this.api.login({ username: this.username, password: this.password }).subscribe({
        next: (res) => {
          this.auth.login({ userId: res.userId, username: res.username, token: res.token });
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.error = this.extractError(err, 'Login failed');
          this.loading = false;
        }
      });
    } else {
      this.api.register({ username: this.username, email: this.email, password: this.password }).subscribe({
        next: () => {
          this.success = 'Registration successful! Please log in.';
          this.isLogin = true;
          this.loading = false;
        },
        error: (err) => {
          this.error = this.extractError(err, 'Registration failed');
          this.loading = false;
        }
      });
    }
  }

  private extractError(err: any, fallback: string): string {
    const body = err.error;
    if (!body) return fallback;
    if (typeof body === 'string') return body;
    if (body.message) return body.message;
    if (body.errors) {
      return body.errors.map((e: any) => e.defaultMessage || e.message).join('. ');
    }
    return fallback;
  }
}
