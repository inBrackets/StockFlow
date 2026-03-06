import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService, StockResponse, PriceHistoryResponse } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { HighchartsChartModule } from 'highcharts-angular';
import * as Highcharts from 'highcharts';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule, FormsModule, HighchartsChartModule],
  template: `
    <h1 class="text-2xl font-bold text-primary mb-6">Market</h1>

    <div class="mb-6">
      <label class="block text-sm font-medium text-gray-700 mb-1">Select Stock for Price History</label>
      <select [(ngModel)]="selectedSymbol" (ngModelChange)="onSymbolChange()" class="px-3 py-2 border rounded-lg outline-none">
        <option *ngFor="let s of stocks" [value]="s.symbol">{{ s.symbol }} - {{ s.companyName }}</option>
      </select>
    </div>

    <div *ngIf="priceChartReady" class="bg-white rounded-xl shadow p-4 mb-8">
      <highcharts-chart
        [Highcharts]="Highcharts"
        [options]="priceChartOptions"
        style="width: 100%; height: 400px; display: block;">
      </highcharts-chart>
    </div>

    <h2 class="text-xl font-bold text-primary mb-4">All Stocks</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div *ngFor="let stock of stocks" class="bg-white rounded-xl shadow p-4 hover:shadow-lg transition">
        <div class="flex justify-between items-start">
          <div>
            <p class="text-lg font-bold text-primary">{{ stock.symbol }}</p>
            <p class="text-sm text-gray-500">{{ stock.companyName }}</p>
          </div>
          <p class="text-xl font-bold text-accent">{{ '$' + stock.currentPrice.toFixed(2) }}</p>
        </div>
      </div>
    </div>
  `
})
export class MarketComponent implements OnInit {
  Highcharts: typeof Highcharts = Highcharts;
  stocks: StockResponse[] = [];
  selectedSymbol = '';
  priceHistory: PriceHistoryResponse[] = [];

  priceChartOptions: Highcharts.Options = {};
  priceChartReady = false;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.api.getStocks().subscribe(stocks => {
      this.stocks = stocks;
      if (stocks.length > 0) {
        this.selectedSymbol = stocks[0].symbol;
        this.loadHistory();
      }
    });
  }

  onSymbolChange() {
    this.priceChartReady = false;
    this.loadHistory();
  }

  private loadHistory() {
    this.api.getPriceHistory(this.selectedSymbol).subscribe(history => {
      this.priceHistory = history;
      this.buildPriceChart();
    });
  }

  private buildPriceChart() {
    const sorted = [...this.priceHistory].reverse();
    const categories = sorted.map(h => new Date(h.recordedAt).toLocaleTimeString());
    const prices = sorted.map(h => h.price);

    if (prices.length === 0) {
      const stock = this.stocks.find(s => s.symbol === this.selectedSymbol);
      if (stock) {
        categories.push('Now');
        prices.push(stock.currentPrice);
      }
    }

    this.priceChartOptions = {
      chart: { type: 'line' },
      title: { text: this.selectedSymbol + ' Price History' },
      xAxis: { categories, title: { text: 'Time' } },
      yAxis: { title: { text: 'Price (USD)' } },
      series: [{ name: this.selectedSymbol, data: prices, type: 'line', color: '#4a90d9' }],
      credits: { enabled: false }
    };
    this.priceChartReady = true;
  }
}
