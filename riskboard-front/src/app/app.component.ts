import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CsvUploadComponent } from './components/csv-upload/csv-upload.component';
import { DerogationFormComponent } from './components/derogation-form/derogation-form.component';
import { DerogationApprovalComponent } from './components/derogation-approval/derogation-approval.component';

type Screen = 'dashboard' | 'upload' | 'create-derogation' | 'validate-derogation';
@Component({
  selector: 'app-root',
  imports: [CommonModule, DashboardComponent, CsvUploadComponent, DerogationFormComponent, DerogationApprovalComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  screen: Screen = 'dashboard';

  onScreenChange(screen: Screen): void {
    this.screen = screen;
  }

  onImportCompleted(): void {
    this.screen = 'dashboard';
  }
}
