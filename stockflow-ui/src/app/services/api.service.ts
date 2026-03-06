import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  userId: number;
  username: string;
  token: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  createdAt: string;
}

export interface StockResponse {
  id: number;
  symbol: string;
  companyName: string;
  currentPrice: number;
}

export interface PriceHistoryResponse {
  price: number;
  recordedAt: string;
}

export interface TradeRequest {
  userId: number;
  stockSymbol: string;
  quantity: number;
  price: number;
}

export interface TradeResponse {
  id: number;
  stockSymbol: string;
  quantity: number;
  priceAtTrade: number;
  tradeType: string;
  tradedAt: string;
}

export interface PortfolioResponse {
  id: number;
  userId: number;
  totalValue: number;
  trades: TradeResponse[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private baseUrl = '/';

  constructor(private http: HttpClient) {}

  register(req: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.baseUrl}users/register`, req);
  }

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}users/login`, req);
  }

  getUser(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}users/${id}`);
  }

  getPortfolio(userId: number): Observable<PortfolioResponse> {
    return this.http.get<PortfolioResponse>(`${this.baseUrl}portfolio/${userId}`);
  }

  buyStock(req: TradeRequest): Observable<TradeResponse> {
    return this.http.post<TradeResponse>(`${this.baseUrl}portfolio/buy`, req);
  }

  sellStock(req: TradeRequest): Observable<TradeResponse> {
    return this.http.post<TradeResponse>(`${this.baseUrl}portfolio/sell`, req);
  }

  getStocks(): Observable<StockResponse[]> {
    return this.http.get<StockResponse[]>(`${this.baseUrl}market/stocks`);
  }

  getStock(symbol: string): Observable<StockResponse> {
    return this.http.get<StockResponse>(`${this.baseUrl}market/stocks/${symbol}`);
  }

  getPriceHistory(symbol: string): Observable<PriceHistoryResponse[]> {
    return this.http.get<PriceHistoryResponse[]>(`${this.baseUrl}market/stocks/${symbol}/history`);
  }
}
