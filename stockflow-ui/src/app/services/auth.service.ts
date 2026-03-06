import { Injectable } from '@angular/core';

export interface UserSession {
  userId: number;
  username: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private session: UserSession | null = null;

  constructor() {
    const stored = localStorage.getItem('sf_session');
    if (stored) {
      this.session = JSON.parse(stored);
    }
  }

  login(session: UserSession): void {
    this.session = session;
    localStorage.setItem('sf_session', JSON.stringify(session));
  }

  logout(): void {
    this.session = null;
    localStorage.removeItem('sf_session');
  }

  isLoggedIn(): boolean {
    return this.session !== null;
  }

  getSession(): UserSession | null {
    return this.session;
  }

  getUserId(): number {
    return this.session?.userId ?? 0;
  }

  getToken(): string {
    return this.session?.token ?? '';
  }
}
