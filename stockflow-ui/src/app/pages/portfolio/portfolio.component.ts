import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService, PortfolioResponse, StockResponse } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { HighchartsChartModule } from 'highcharts-angular';
import * as Highcharts from 'highcharts';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, FormsModule, HighchartsChartModule],
  template: `
    <h1 class="text-2xl font-bold text-primary mb-6">Portfolio</h1>

    <!-- Trade Modal -->
    <div *ngIf="showTradeModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
        <h2 class="text-xl font-bold mb-4">{{ tradeType }} Stock</h2>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Stock Symbol</label>
          <select [(ngModel)]="tradeSymbol" data-testid="trade-symbol" class="w-full px-3 py-2 border rounded-lg outline-none">
            <option *ngFor="let s of stocks" [value]="s.symbol">{{ s.symbol }} - {{ '$' + s.currentPrice.toFixed(2) }}</option>
          </select>
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-1">Quantity</label>
          <input [(ngModel)]="tradeQuantity" type="number" min="1" data-testid="trade-quantity"
                 class="w-full px-3 py-2 border rounded-lg outline-none" />
        </div>
        <div class="flex space-x-3">
          <button (click)="executeTrade()" [disabled]="tradeLoading" data-testid="btn-confirm-trade"
                  class="flex-1 bg-accent text-white py-2 rounded-lg hover:bg-blue-600 transition disabled:opacity-50">
            {{ tradeLoading ? 'Processing...' : 'Confirm' }}
          </button>
          <button (click)="showTradeModal = false"
                  class="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg hover:bg-gray-300 transition">
            Cancel
          </button>
        </div>
        <div *ngIf="tradeError" class="mt-3 text-red-500 text-sm">{{ tradeError }}</div>
      </div>
    </div>

    <!-- Action Buttons -->
    <div class="flex space-x-3 mb-6">
      <button (click)="openTrade('BUY')" data-testid="btn-buy" class="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition font-medium">
        Buy Stock
      </button>
      <button (click)="openTrade('SELL')" data-testid="btn-sell" class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition font-medium">
        Sell Stock
      </button>
    </div>

    <div *ngIf="portfolio" class="bg-white rounded-xl shadow p-4 mb-6">
      <p class="text-gray-500 text-sm">Total Portfolio Value</p>
      <p class="text-3xl font-bold text-primary">{{ '$' + portfolio.totalValue.toFixed(2) }}</p>
    </div>

    <!-- Holdings Chart -->
    <div *ngIf="holdingsChartReady" class="bg-white rounded-xl shadow p-4 mb-6">
      <highcharts-chart
        [Highcharts]="Highcharts"
        [options]="holdingsChartOptions"
        style="width: 100%; height: 400px; display: block;">
      </highcharts-chart>
    </div>

    <!-- Trade History -->
    <div *ngIf="portfolio?.trades?.length" class="bg-white rounded-xl shadow p-6" data-testid="trade-history">
      <h3 class="text-lg font-bold text-primary mb-4">Trade History</h3>
      <table class="w-full" data-testid="trade-history-table">
        <thead>
          <tr class="border-b-2 border-gray-200">
            <th class="text-left py-2 px-3">Symbol</th>
            <th class="text-left py-2 px-3">Type</th>
            <th class="text-right py-2 px-3">Qty</th>
            <th class="text-right py-2 px-3">Price</th>
            <th class="text-right py-2 px-3">Date</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let trade of portfolio!.trades" class="border-b border-gray-100">
            <td class="py-2 px-3 font-semibold">{{ trade.stockSymbol }}</td>
            <td class="py-2 px-3 font-semibold" [class.text-green-500]="trade.tradeType === 'BUY'" [class.text-red-500]="trade.tradeType === 'SELL'">{{ trade.tradeType }}</td>
            <td class="py-2 px-3 text-right">{{ trade.quantity }}</td>
            <td class="py-2 px-3 text-right">{{ '$' + trade.priceAtTrade.toFixed(2) }}</td>
            <td class="py-2 px-3 text-right">{{ trade.tradedAt | date:'shortDate' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class PortfolioComponent implements OnInit, OnDestroy {
  Highcharts: typeof Highcharts = Highcharts;
  portfolio: PortfolioResponse | null = null;
  stocks: StockResponse[] = [];

  holdingsChartOptions: Highcharts.Options = {};
  holdingsChartReady = false;

  showTradeModal = false;
  tradeType: 'BUY' | 'SELL' = 'BUY';
  tradeSymbol = '';
  tradeQuantity = 1;
  tradeLoading = false;
  tradeError = '';
  private priceSub?: Subscription;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadData();
    this.priceSub = this.api.streamPrices().subscribe(tick => {
      const stock = this.stocks.find(s => s.symbol === tick.symbol);
      if (stock) {
        stock.currentPrice = tick.price;
      }
    });
  }

  ngOnDestroy() {
    this.priceSub?.unsubscribe();
  }

  private loadData() {
    this.api.getStocks().subscribe(stocks => {
      this.stocks = stocks;
      if (stocks.length > 0) this.tradeSymbol = stocks[0].symbol;
    });

    this.api.getPortfolio(this.auth.getUserId()).subscribe({
      next: (portfolio) => {
        this.portfolio = portfolio;
        this.buildHoldingsChart();
      },
      error: () => {}
    });
  }

  openTrade(type: 'BUY' | 'SELL') {
    this.tradeType = type;
    this.tradeError = '';
    this.showTradeModal = true;
  }

  executeTrade() {
    this.tradeLoading = true;
    this.tradeError = '';
    const stock = this.stocks.find(s => s.symbol === this.tradeSymbol);
    if (!stock) return;

    const req = {
      userId: this.auth.getUserId(),
      stockSymbol: this.tradeSymbol,
      quantity: this.tradeQuantity,
      price: stock.currentPrice
    };

    const call = this.tradeType === 'BUY' ? this.api.buyStock(req) : this.api.sellStock(req);
    call.subscribe({
      next: () => {
        this.showTradeModal = false;
        this.tradeLoading = false;
        this.holdingsChartReady = false;
        this.loadData();
      },
      error: (err) => {
        this.tradeError = err.error?.message || 'Trade failed';
        this.tradeLoading = false;
      }
    });
  }

  private buildHoldingsChart() {
    if (!this.portfolio?.trades?.length) return;

    const holdingsMap = new Map<string, number>();
    for (const trade of this.portfolio.trades) {
      const current = holdingsMap.get(trade.stockSymbol) || 0;
      if (trade.tradeType === 'BUY') {
        holdingsMap.set(trade.stockSymbol, current + trade.quantity);
      } else {
        holdingsMap.set(trade.stockSymbol, current - trade.quantity);
      }
    }

    const holdingSymbols = Array.from(holdingsMap.keys()).filter(k => (holdingsMap.get(k) || 0) > 0);
    const holdingValues = holdingSymbols.map(k => holdingsMap.get(k) || 0);

    this.holdingsChartOptions = {
      chart: { type: 'column' },
      title: { text: 'Current Holdings' },
      xAxis: { categories: holdingSymbols },
      yAxis: { title: { text: 'Shares' } },
      series: [{ name: 'Shares', data: holdingValues, type: 'column', colorByPoint: true }],
      credits: { enabled: false }
    };
    this.holdingsChartReady = true;
  }
}
