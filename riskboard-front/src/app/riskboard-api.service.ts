import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type LimitType = 'CREDIT' | 'MARKET' | 'LIQUIDITY';
export type RiskStatus = 'GREEN' | 'ORANGE' | 'RED';
export type DerogationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface RiskLimitView {
  riskLimitId: number;
  counterpartyId: number;
  counterpartyName: string;
  sector: string;
  limitType: LimitType;
  maxAmount: number;
  usedAmount: number;
  usageRate: number;
  riskStatus: RiskStatus;
  currency: string;
}

export interface SectorExposure {
  limitType: LimitType;
  sector: string;
  totalUsedAmount: number;
}

export interface CsvImportError {
  lineNumber: number;
  rawLine: string;
  errorMessage: string;
}

export interface CsvImportSummary {
  successfulRows: number;
  errorRows: number;
  errors: CsvImportError[];
}

export interface CounterpartyOption {
  id: number;
  name: string;
}

export interface CreateDerogationPayload {
  counterpartyId: number;
  limitType: LimitType;
  amount: number;
  reason: string;
  requestedBy: string;
}

export interface DerogationResponse {
  id: number;
  counterpartyId: number;
  counterpartyName: string;
  limitType: LimitType;
  requestedBy: string;
  amount: number;
  reason: string;
  status: DerogationStatus;
  createdAt: string;
}

export interface AmountValidationResponse {
  valid: boolean;
  message: string;
  maxAllowedAmount: number | null;
}

@Injectable({ providedIn: 'root' })
export class RiskboardApiService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  getRiskLimits(): Observable<RiskLimitView[]> {
    return this.http.get<RiskLimitView[]>(`${this.baseUrl}/risk-limits`);
  }

  getAggregated(limitType: LimitType): Observable<SectorExposure[]> {
    return this.http.get<SectorExposure[]>(`${this.baseUrl}/risk-limits/aggregated`, {
      params: new HttpParams().set('limitType', limitType)
    });
  }

  importCsv(file: File): Observable<CsvImportSummary> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<CsvImportSummary>(`${this.baseUrl}/risk-limits/import`, formData);
  }

  getCounterparties(): Observable<CounterpartyOption[]> {
    return this.http.get<CounterpartyOption[]>(`${this.baseUrl}/counterparties`);
  }

  createDerogation(payload: CreateDerogationPayload): Observable<DerogationResponse> {
    return this.http.post<DerogationResponse>(`${this.baseUrl}/derogations`, payload);
  }

  getPendingDerogations(): Observable<DerogationResponse[]> {
    return this.http.get<DerogationResponse[]>(`${this.baseUrl}/derogations/pending`);
  }

  approveDerogation(id: number): Observable<DerogationResponse> {
    return this.http.post<DerogationResponse>(`${this.baseUrl}/derogations/${id}/approve`, {});
  }

  rejectDerogation(id: number): Observable<DerogationResponse> {
    return this.http.post<DerogationResponse>(`${this.baseUrl}/derogations/${id}/reject`, {});
  }

  validateAmount(counterpartyId: number, limitType: LimitType, amount: number): Observable<AmountValidationResponse> {
    const params = new HttpParams()
      .set('counterpartyId', counterpartyId)
      .set('limitType', limitType)
      .set('amount', amount);
    return this.http.get<AmountValidationResponse>(`${this.baseUrl}/derogations/validate-amount`, { params });
  }
}
