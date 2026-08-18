import { CommonModule, DecimalPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom, Subscription } from 'rxjs';
import { LimitType, RiskLimitView, RiskboardApiService, SectorExposure } from '../../riskboard-api.service';
import { RiskboardRefreshService } from '../../riskboard-refresh.service';

type Direction = 'asc' | 'desc';
type SortField = 'counterpartyName' | 'limitType' | 'sector' | 'maxAmount' | 'usedAmount' | 'usageRate' | 'riskStatus';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule, DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  riskLimits: RiskLimitView[] = [];
  aggregatedRows: SectorExposure[] = [];
  filterName = '';
  selectedLimitView: 'DETAILED' | LimitType = 'DETAILED';
  isLoading = false;
  loadError = '';
  pageSize = 5;
  currentPage = 1;
  activeSort: { field: SortField; direction: Direction } | null = null;

  private refreshSubscription?: Subscription;

  constructor(
    private readonly api: RiskboardApiService,
    private readonly refreshService: RiskboardRefreshService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    void this.loadRiskLimits();
    this.refreshSubscription = this.refreshService.riskLimitsUpdated$.subscribe(() => {
      void this.loadRiskLimits();
    });
  }

  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
  }

  onViewModeChange(value: string): void {
    this.currentPage = 1;
    this.selectedLimitView = value as 'DETAILED' | LimitType;
    if (this.selectedLimitView !== 'DETAILED') {
      void this.loadAggregated(this.selectedLimitView);
    }
  }

  onFilterChange(): void {
    this.currentPage = 1;
  }

  sortBy(field: SortField): void {
    this.currentPage = 1;
    if (!this.activeSort || this.activeSort.field !== field) {
      this.activeSort = { field, direction: 'asc' };
      return;
    }
    this.activeSort = { field, direction: this.activeSort.direction === 'asc' ? 'desc' : 'asc' };
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  get paginatedRows(): RiskLimitView[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.detailedRows.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.detailedRows.length / this.pageSize));
  }

  private async loadRiskLimits(): Promise<void> {
    this.isLoading = true;
    this.loadError = '';
    try {
      this.riskLimits = await firstValueFrom(this.api.getRiskLimits());
      this.currentPage = 1;
      if (this.selectedLimitView !== 'DETAILED') {
        await this.loadAggregated(this.selectedLimitView, false);
      }
    } catch {
      this.loadError = 'Impossible de charger les limites de risque. Vérifiez que le backend est démarré.';
    } finally {
      this.isLoading = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  private async loadAggregated(limitType: LimitType, setLoading = true): Promise<void> {
    if (setLoading) {
      this.isLoading = true;
      this.loadError = '';
    }
    try {
      this.aggregatedRows = await firstValueFrom(this.api.getAggregated(limitType));
    } catch {
      this.loadError = 'Impossible de charger l’exposition agrégée.';
    } finally {
      if (setLoading) {
        this.isLoading = false;
      }
      this.changeDetectorRef.detectChanges();
    }
  }

  private get detailedRows(): RiskLimitView[] {
    const filter = this.normalizeForSearch(this.filterName);
    const filtered = this.riskLimits.filter((row) =>
      this.normalizeForSearch(row.counterpartyName).includes(filter)
    );

    const rows = [...filtered];
    if (this.activeSort) {
      rows.sort((a, b) => this.compare(a, b, this.activeSort!.field, this.activeSort!.direction));
    } else {
      rows.sort((a, b) => this.defaultCompare(a, b));
    }
    return rows;
  }

  private defaultCompare(a: RiskLimitView, b: RiskLimitView): number {
    return (
      this.compareValue(a.counterpartyName, b.counterpartyName, 'asc') ||
      this.compareValue(a.limitType, b.limitType, 'asc') ||
      this.compareValue(a.sector, b.sector, 'asc') ||
      this.compareValue(a.maxAmount, b.maxAmount, 'desc') ||
      this.compareValue(a.usedAmount, b.usedAmount, 'desc') ||
      this.compareValue(a.usageRate, b.usageRate, 'desc') ||
      this.compareValue(a.riskStatus, b.riskStatus, 'asc')
    );
  }

  private compare(a: RiskLimitView, b: RiskLimitView, field: SortField, direction: Direction): number {
    return this.compareValue(a[field], b[field], direction);
  }

  private compareValue(a: string | number, b: string | number, direction: Direction): number {
    const factor = direction === 'asc' ? 1 : -1;
    if (a < b) {
      return -1 * factor;
    }
    if (a > b) {
      return factor;
    }
    return 0;
  }

  private normalizeForSearch(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim()
      .toLocaleLowerCase();
  }
}
