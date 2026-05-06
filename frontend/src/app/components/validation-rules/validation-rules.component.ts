import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SalesforceService } from '../../services/salesforce.service';
import { ValidationRule } from '../../models/validation-rule.model';

@Component({
  selector: 'app-validation-rules',
  templateUrl: './validation-rules.component.html',
  styleUrls: ['./validation-rules.component.css']
})
export class ValidationRulesComponent implements OnInit {
  rules: ValidationRule[] = [];
  loading = false;
  deploying = false;
  error: string | null = null;
  accessToken: string = '';
  instanceUrl: string = '';

  constructor(
    private salesforceService: SalesforceService,
    private router: Router
  ) {}

  ngOnInit() {
    this.accessToken = localStorage.getItem('sf_access_token') || '';
    this.instanceUrl = localStorage.getItem('sf_instance_url') || '';

    if (!this.accessToken) {
      this.router.navigate(['/login']);
      return;
    }

    this.loadRules();
  }

  loadRules() {
    this.loading = true;
    this.error = null;

    this.salesforceService.getValidationRules(this.accessToken, this.instanceUrl)
      .subscribe({
        next: (rules) => {
          this.rules = rules;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load validation rules. Session may have expired.';
          this.loading = false;
          console.error(err);
        }
      });
  }

  toggleRule(rule: ValidationRule) {
    rule.active = !rule.active;
  }

  enableAll() {
    this.rules.forEach(r => r.active = true);
  }

  disableAll() {
    this.rules.forEach(r => r.active = false);
  }

  deploy() {
    this.deploying = true;
    this.error = null;

    const payload = this.rules.map(r => ({
      id: r.id,
      fullName: r.fullName,
      active: r.active
    }));

    this.salesforceService.deployChanges(this.accessToken, this.instanceUrl, payload)
      .subscribe({
        next: (response) => {
          this.deploying = false;
          if (response.success) {
            alert('All changes deployed successfully to Salesforce!');
          } else {
            alert(`Deployment completed with errors. Failed rules: ${response.failedRules.join(', ')}`);
          }
        },
        error: (err) => {
          this.deploying = false;
          this.error = 'Deployment failed. Please check your connection and try again.';
          console.error(err);
        }
      });
  }

  logout() {
    localStorage.removeItem('sf_access_token');
    localStorage.removeItem('sf_instance_url');
    this.router.navigate(['/login']);
  }
}
