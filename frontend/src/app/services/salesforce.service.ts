import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ValidationRule } from '../models/validation-rule.model';

@Injectable({
  providedIn: 'root'
})
export class SalesforceService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getAuthUrl(): Observable<{authUrl: string}> {
    return this.http.get<{authUrl: string}>(`${this.apiUrl}/auth/url`);
  }

  exchangeCode(code: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/auth/token?code=${code}`, {});
  }

  getValidationRules(accessToken: string, instanceUrl: string): Observable<ValidationRule[]> {
    const headers = new HttpHeaders()
      .set('X-Access-Token', accessToken)
      .set('X-Instance-Url', instanceUrl);
    return this.http.get<ValidationRule[]>(`${this.apiUrl}/validation-rules`, { headers });
  }

  deployChanges(accessToken: string, instanceUrl: string, rules: any[]): Observable<any> {
    const headers = new HttpHeaders()
      .set('X-Access-Token', accessToken)
      .set('X-Instance-Url', instanceUrl)
      .set('Content-Type', 'application/json');
    return this.http.post(`${this.apiUrl}/validation-rules/deploy`, { rules }, { headers });
  }
}
