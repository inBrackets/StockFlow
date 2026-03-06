import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService, UserResponse } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="max-w-2xl mx-auto">
      <h1 class="text-2xl font-bold text-primary mb-6">Profile</h1>
      <div *ngIf="user" class="bg-white rounded-xl shadow p-6">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-sm text-gray-500">Username</p>
            <p class="text-lg font-medium">{{ user.username }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Email</p>
            <p class="text-lg font-medium">{{ user.email }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">User ID</p>
            <p class="text-lg font-medium">{{ user.id }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-500">Member Since</p>
            <p class="text-lg font-medium">{{ user.createdAt | date:'mediumDate' }}</p>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  user: UserResponse | null = null;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.api.getUser(this.auth.getUserId()).subscribe({
      next: (user) => this.user = user,
      error: () => this.router.navigate(['/login'])
    });
  }
}
