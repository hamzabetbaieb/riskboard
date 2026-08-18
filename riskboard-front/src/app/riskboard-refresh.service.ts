import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RiskboardRefreshService {
  private readonly riskLimitsUpdatedSubject = new Subject<void>();

  readonly riskLimitsUpdated$ = this.riskLimitsUpdatedSubject.asObservable();

  notifyRiskLimitsUpdated(): void {
    this.riskLimitsUpdatedSubject.next();
  }
}
