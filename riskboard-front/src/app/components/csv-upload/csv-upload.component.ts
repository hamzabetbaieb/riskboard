import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, output } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { RiskboardApiService } from '../../riskboard-api.service';
import { RiskboardRefreshService } from '../../riskboard-refresh.service';

@Component({
  selector: 'app-csv-upload',
  imports: [CommonModule],
  templateUrl: './csv-upload.component.html',
  styleUrl: './csv-upload.component.css'
})
export class CsvUploadComponent {
  readonly importCompleted = output<void>();

  importMessage = '';
  importError = '';
  selectedFile: File | null = null;
  isImporting = false;
  showNoFileSelectedError = false;

  constructor(
    private readonly api: RiskboardApiService,
    private readonly refreshService: RiskboardRefreshService
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length > 0 ? input.files[0] : null;
    if (this.selectedFile) {
      this.showNoFileSelectedError = false;
    }
  }

  async importCsv(): Promise<void> {
    if (this.isImporting) {
      return;
    }
    this.importMessage = '';
    this.importError = '';
    this.showNoFileSelectedError = false;
    if (!this.selectedFile) {
      this.showNoFileSelectedError = true;
      return;
    }

    this.isImporting = true;
    try {
      const summary = await firstValueFrom(
        this.api.importCsv(this.selectedFile)
      );

      this.importMessage = `Import terminé : ${summary.successfulRows} ligne(s) en succès, ${summary.errorRows} en erreur.`;
      if (summary.errors.length > 0) {
        const firstError = summary.errors[0];
        this.importMessage += ` Première erreur ligne ${firstError.lineNumber} : ${firstError.errorMessage}`;
      }
      this.refreshService.notifyRiskLimitsUpdated();
      this.importCompleted.emit();
    } catch (error: unknown) {
      this.importError = this.resolveImportErrorMessage(error);
    } finally {
      this.isImporting = false;
    }
  }

  private resolveImportErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Impossible de joindre le backend. Vérifiez que le serveur est démarré et accessible.';
      }
      if (error.status >= 500) {
        return `Erreur serveur (${error.status}) lors de l'import CSV.`;
      }

      const backendMessage = (error.error as { message?: string } | null)?.message;
      if (backendMessage) {
        return this.translateBackendMessage(backendMessage);
      }
      return `Échec de l'import CSV (HTTP ${error.status}).`;
    }
    return "Une erreur inattendue est survenue pendant l'import CSV.";
  }

  private translateBackendMessage(message: string): string {
    if (message.startsWith('Cannot read CSV file:')) {
      return "Impossible de lire le fichier CSV envoyé.";
    }
    return message;
  }
}
