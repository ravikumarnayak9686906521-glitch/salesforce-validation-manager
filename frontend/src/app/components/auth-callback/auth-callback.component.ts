import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { SalesforceService } from '../../services/salesforce.service';

@Component({
  selector: 'app-auth-callback',
  template: `
    <div class="callback-container">
      <div class="spinner"></div>
      <h2>Authenticating...</h2>
      <p>Please wait while we connect to your Salesforce org.</p>
    </div>
  `,
  styles: [`
    .callback-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100vh;
      background: #f3f3f3;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }
    .spinner {
      width: 60px;
      height: 60px;
      border: 6px solid #ddd;
      border-top-color: #0176d3;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 1.5rem;
    }
    h2 { color: #032d60; margin-bottom: 0.5rem; }
    p { color: #666; }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class AuthCallbackComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private salesforceService: SalesforceService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      if (code) {
        this.salesforceService.exchangeCode(code).subscribe({
          next: (response) => {
            localStorage.setItem('sf_access_token', response.accessToken);
            localStorage.setItem('sf_instance_url', response.instanceUrl);
            this.router.navigate(['/validation-rules']);
          },
          error: (err) => {
            console.error('Authentication failed', err);
            alert('Failed to authenticate with Salesforce. Please try again.');
            this.router.navigate(['/login']);
          }
        });
      } else {
        this.router.navigate(['/login']);
      }
    });
  }
}
