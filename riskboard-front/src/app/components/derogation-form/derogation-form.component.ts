import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { firstValueFrom, map, Observable, of, switchMap, timer } from 'rxjs';
import { CounterpartyOption, CreateDerogationPayload, LimitType, RiskboardApiService } from '../../riskboard-api.service';

@Component({
  selector: 'app-derogation-form',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './derogation-form.component.html',
  styleUrl: './derogation-form.component.css'
})
export class DerogationFormComponent implements OnInit {
  readonly limitTypes: LimitType[] = ['CREDIT', 'MARKET', 'LIQUIDITY'];
  counterparties: CounterpartyOption[] = [];
  submitError = '';
  submitSuccess = '';
  amountValidationMessage = '';
  isLoadingCounterparties = false;
  counterpartiesError = '';

  readonly derogationForm;

  constructor(
    private readonly api: RiskboardApiService,
    private readonly formBuilder: FormBuilder,
    private readonly changeDetectorRef: ChangeDetectorRef
  ) {
    this.derogationForm = this.formBuilder.group({
      counterpartyId: [null as number | null, Validators.required],
      limitType: [null as LimitType | null, Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(0.0001)]],
      reason: ['', [Validators.required, Validators.minLength(20)]],
      requestedBy: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    void this.loadCounterparties();
    this.derogationForm.controls.amount.addAsyncValidators(this.amountAsyncValidator());
    this.derogationForm.controls.counterpartyId.valueChanges.subscribe(() => this.triggerAmountValidation());
    this.derogationForm.controls.limitType.valueChanges.subscribe(() => this.triggerAmountValidation());
  }

  submitDerogation(): void {
    this.submitError = '';
    this.submitSuccess = '';
    if (this.derogationForm.invalid) {
      this.derogationForm.markAllAsTouched();
      return;
    }
    const payload = this.derogationForm.getRawValue() as CreateDerogationPayload;
    this.api.createDerogation(payload).subscribe({
      next: () => {
        this.submitSuccess = 'Demande de dérogation créée avec succès.';
        this.derogationForm.reset();
        this.amountValidationMessage = '';
      },
      error: (error: unknown) => {
        this.submitError = this.resolveSubmitErrorMessage(error);
      }
    });
  }

  get amountControl(): AbstractControl {
    return this.derogationForm.controls.amount;
  }

  private async loadCounterparties(): Promise<void> {
    this.isLoadingCounterparties = true;
    this.counterpartiesError = '';
    try {
      this.counterparties = await firstValueFrom(this.api.getCounterparties());
    } catch {
      this.counterpartiesError = 'Impossible de charger les contreparties. Vérifiez que le backend est démarré.';
    } finally {
      this.isLoadingCounterparties = false;
      this.changeDetectorRef.detectChanges();
    }
  }

  private triggerAmountValidation(): void {
    if (this.derogationForm.controls.amount.value !== null) {
      this.derogationForm.controls.amount.updateValueAndValidity();
    }
  }

  private amountAsyncValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const counterpartyId = this.derogationForm.controls.counterpartyId.value;
      const limitType = this.derogationForm.controls.limitType.value;
      const amount = control.value as number | null;

      this.amountValidationMessage = '';
      if (!counterpartyId || !limitType || amount === null || amount <= 0) {
        return of(null);
      }

      return timer(250).pipe(
        switchMap(() => this.api.validateAmount(counterpartyId, limitType, amount)),
        map((result) => {
          const rawMessage = result.message ?? '';
          if (result.valid) {
            this.amountValidationMessage = '';
            return null;
          }
          this.amountValidationMessage = this.translateBackendValidationMessage(rawMessage);
          if (rawMessage.includes('No risk limit')) {
            return { riskLimitMissing: true };
          }
          return { amountExceeded: true };
        })
      );
    };
  }

  private resolveSubmitErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 0) {
        return 'Impossible de joindre le backend. Vérifiez que le serveur est démarré et accessible.';
      }
      if (error.status >= 500) {
        return `Erreur serveur (${error.status}) lors de la création de la demande.`;
      }

      const backendMessage = (error.error as { message?: string } | null)?.message;
      if (backendMessage) {
        return this.translateBackendValidationMessage(backendMessage);
      }
      return `Échec de création de la demande (HTTP ${error.status}).`;
    }
    return 'Impossible de créer la demande de dérogation.';
  }

  private translateBackendValidationMessage(message: string): string {
    const normalized = message.trim();
    if (normalized === 'Counterparty not found') {
      return 'Contrepartie introuvable.';
    }
    if (normalized === 'No risk limit found for selected counterparty and risk type'
      || normalized === 'No risk limit for selected counterparty and risk type') {
      return 'Aucune limite de risque trouvée pour la contrepartie et le type sélectionnés.';
    }
    if (normalized === 'Requested amount exceeds 150% of maximum limit'
      || normalized === 'Amount exceeds 150% of max amount') {
      return 'Le montant demandé dépasse 150 % de la limite maximale.';
    }
    if (normalized === 'counterpartyId, limitType and amount are required') {
      return 'counterpartyId, limitType et amount sont obligatoires.';
    }
    if (normalized === 'Amount is valid') {
      return 'Montant valide.';
    }
    return message;
  }
}
