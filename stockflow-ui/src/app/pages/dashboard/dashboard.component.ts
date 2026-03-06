import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService, StockResponse, PortfolioResponse } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { HighchartsChartModule } from 'highcharts-angular';
import * as Highcharts from 'highcharts';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, HighchartsChartModule],
  template: `
    <h1 class="text-2xl font-bold text-primary mb-6">Dashboard</h1>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <div class="bg-white rounded-xl shadow p-4">
        <highcharts-chart
          *ngIf="stockChartReady"
          [Highcharts]="Highcharts"
          [options]="stockChartOptions"
          style="width: 100%; height: 400px; display: block;">
        </highcharts-chart>
      </div>
      <div class="bg-white rounded-xl shadow p-4">
        <highcharts-chart
          *ngIf="pieChartReady"
          [Highcharts]="Highcharts"
          [options]="pieChartOptions"
          style="width: 100%; height: 400px; display: block;">
        </highcharts-chart>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow p-6">
      <h3 class="text-lg font-bold text-primary mb-4">Market Overview</h3>
      <table class="w-full">
        <thead>
          <tr class="border-b-2 border-gray-200">
            <th class="text-left py-2 px-3">Symbol</th>
            <th class="text-left py-2 px-3">Company</th>
            <th class="text-right py-2 px-3">Price</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let stock of stocks" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="py-2 px-3 font-semibold">{{ stock.symbol }}</td>
            <td class="py-2 px-3">{{ stock.companyName }}</td>
            <td class="py-2 px-3 text-right">{{ '$' + stock.currentPrice.toFixed(2) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  Highcharts: typeof Highcharts = Highcharts;
  stocks: StockResponse[] = [];
  portfolio: PortfolioResponse | null = null;

  stockChartOptions: Highcharts.Options = {};
  pieChartOptions: Highcharts.Options = {};
  stockChartReady = false;
  pieChartReady = false;

  constructor(private api: ApiService, private auth: AuthService, private router: Router) {}

  ngOnInit() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadData();
  }

  private loadData() {
    this.api.getStocks().subscribe(stocks => {
      this.stocks = stocks;
      this.buildStockChart();
      this.api.getPortfolio(this.auth.getUserId()).subscribe({
        next: (portfolio) => {
          this.portfolio = portfolio;
          this.buildPieChart();
        },
        error: () => this.buildPieChart()
      });
    });
  }

  private buildStockChart() {
    this.stockChartOptions = {
      chart: { type: 'bar' },
      title: { text: 'Stock Prices' },
      xAxis: { categories: this.stocks.map(s => s.symbol) },
      yAxis: { title: { text: 'Price (USD)' } },
      series: [{ name: 'Current Price', data: this.stocks.map(s => s.currentPrice), type: 'bar', colorByPoint: true }],
      credits: { enabled: false }
    };
    this.stockChartReady = true;
  }

  private buildPieChart() {
    const holdingsMap = new Map<string, number>();
    if (this.portfolio?.trades) {
      for (const trade of this.portfolio.trades) {
        const current = holdingsMap.get(trade.stockSymbol) || 0;
        if (trade.tradeType === 'BUY') {
          holdingsMap.set(trade.stockSymbol, current + trade.quantity);
        } else {
          holdingsMap.set(trade.stockSymbol, current - trade.quantity);
        }
      }
    }
    const holdingsData: { name: string; y: number }[] = Array.from(holdingsMap.entries())
      .filter(([_, qty]) => qty > 0)
      .map(([symbol, qty]) => ({ name: symbol, y: qty }));

    if (holdingsData.length === 0) {
      holdingsData.push({ name: 'No holdings', y: 1 });
    }

    this.pieChartOptions = {
      chart: { type: 'pie' },
      title: { text: 'Holdings Distribution' },
      series: [{ name: 'Shares', data: holdingsData, type: 'pie' }],
      credits: { enabled: false }
    };
    this.pieChartReady = true;
  }
}
