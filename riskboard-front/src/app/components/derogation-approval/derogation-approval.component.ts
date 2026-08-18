import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { DerogationResponse, RiskboardApiService } from '../../riskboard-api.service';

@Component({
  selector: 'app-derogation-approval',
  imports: [CommonModule, DatePipe, DecimalPipe],
  templateUrl: './derogation-approval.component.html',
  styleUrl: './derogation-approval.component.css'
})
export class DerogationApprovalComponent implements OnInit {
  pendingDerogations: DerogationResponse[] = [];
  actionMessage = '';
  isLoading = false;
  loadError = '';

  constructor(
    private readonly api: RiskboardApiService,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    void this.loadPendingDerogations();
  }

  approve(id: number): void {
    this.loadError = '';
    this.api.approveDerogation(id).subscribe({
      next: () => {
        this.actionMessage = 'Demande validée.';
        void this.loadPendingDerogations();
      },
      error: (error: unknown) => {
        this.actionMessage = '';
        this.loadError = this.resolveActionErrorMessage(error);
      }
    });
  }

  reject(id: number): void {
    this.loadError = '';
    this.api.rejectDerogation(id).subscribe({
      next: () => {
        this.actionMessage = 'Demande rejetée.';
        void this.loadPendingDerogations();
      },
      error: (error: unknown) => {
        this.actionMessage = '';
        this.loadError = this.resolveActionErrorMessage(error);
      }
    });
  }

  private async loadPendingDerogations(): Promise<void> {
    this.isLoading = true;
    this.loadError = '';
    try {
      this.pendingDerogations = await firstValueFrom(this.api.getPendingDerogations());
    } catch {
      this.loadError = 'Impossible de charger les demandes de dérogation.';
    } finally {
      this.isLoading = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  private resolveActionErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Impossible de joindre le backend. Vérifiez que le serveur est démarré et accessible.';
      }
      if (error.status >= 500) {
        return `Erreur serveur (${error.status}) lors de la mise à jour de la demande.`;
      }
      return `Échec de la mise à jour de la demande (HTTP ${error.status}).`;
    }
    return 'Impossible de mettre à jour la demande de dérogation.';
  }
}
