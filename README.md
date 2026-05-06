# Salesforce Validation Manager

A complete end-to-end application built with **Angular 17** (frontend) and **Spring Boot 3** (backend) that connects to Salesforce via OAuth 2.0 and uses the Salesforce Tooling API to manage Account Validation Rules.

## Features

- 🔐 OAuth 2.0 login with Salesforce
- 📋 Fetch all Validation Rules from your org
- 🔄 Toggle individual rules Active/Inactive
- ✅ Enable All / ⛔ Disable All buttons
- 🚀 Deploy changes back to Salesforce in real-time
- 🎨 Clean, responsive UI

---

## Prerequisites

1. **Java 17+**
2. **Node.js 18+** and **npm**
3. **Maven**
4. **Angular CLI** (`npm install -g @angular/cli`)
5. A **Salesforce Developer Org** — [signup here](https://developer.salesforce.com/signup)

---

## Step 1: Salesforce Setup

### 1.1 Create Validation Rules
Go to **Setup → Object Manager → Account → Validation Rules → New** and create these 5 rules:

| Rule Name | Formula | Error Message | Active |
|-----------|---------|---------------|--------|
| `VR_Account_Name_Required` | `ISBLANK( Name )` | Account Name cannot be blank | ✅ |
| `VR_Account_Phone_Required` | `ISBLANK( Phone )` | Phone number is required | ✅ |
| `VR_Account_Revenue_Positive` | `AnnualRevenue < 0` | Annual Revenue must be positive | ✅ |
| `VR_Account_BillingState_Req` | `ISBLANK( BillingState )` | Billing State is mandatory | ✅ |
| `VR_Account_Industry_Req` | `ISBLANK( TEXT(Industry) )` | Please select an Industry | ✅ |

### 1.2 Create Connected App
1. Go to **Setup → App Manager → New Connected App**
2. Fill in:
   - **Connected App Name**: `ValidationRuleManager`
   - **API Name**: `ValidationRuleManager`
   - **Contact Email**: your email
3. Check **Enable OAuth Settings**:
   - **Callback URL** (for local): `http://localhost:4200/#/auth/callback`
   - **Selected OAuth Scopes**: `Access and manage your data (api)` and `Perform requests on your behalf at any time (refresh_token, offline_access)`
4. Save → Click **Manage Consumer Details** → Copy **Consumer Key** and **Consumer Secret**
5. Go to **Manage Connected Apps → Edit Policies**:
   - **Permitted Users**: `All users may self-authorize`
   - **IP Relaxation**: `Relax IP restrictions`

---

## Step 2: Local Development

### 2.1 Configure Backend
Open `backend/src/main/resources/application.properties` and replace:
```properties
salesforce.client-id=YOUR_CONSUMER_KEY
salesforce.client-secret=YOUR_CONSUMER_SECRET
```

### 2.2 Run Backend
```bash
cd backend
mvn spring-boot:run
```
Backend runs at `http://localhost:8080`

### 2.3 Run Frontend
```bash
cd frontend
npm install
ng serve
```
Frontend runs at `http://localhost:4200`

### 2.4 Test
1. Open `http://localhost:4200`
2. Click **Login with Salesforce**
3. Authorize the app
4. View, toggle, and deploy validation rules

---

## Step 3: Deploy to Production (Single JAR)

### Option A: Heroku (Free Tier Available)

1. **Install Heroku CLI** and login:
```bash
heroku login
```

2. **Create Heroku app**:
```bash
cd backend
heroku create your-app-name
```

3. **Set environment variables**:
```bash
heroku config:set SALESFORCE_CLIENT_ID=your_consumer_key
heroku config:set SALESFORCE_CLIENT_SECRET=your_consumer_secret
heroku config:set SALESFORCE_REDIRECT_URI=https://your-app-name.herokuapp.com/#/auth/callback
```

4. **Update `application.properties`** to use env vars:
```properties
salesforce.client-id=${SALESFORCE_CLIENT_ID}
salesforce.client-secret=${SALESFORCE_CLIENT_SECRET}
salesforce.redirect-uri=${SALESFORCE_REDIRECT_URI}
```

5. **Build Angular and copy to Spring Boot**:
```bash
cd ../frontend
npm install
ng build --configuration production
cp -r dist/frontend/browser/* ../backend/src/main/resources/static/
```

6. **Add `Procfile` to backend**:
```
web: java -Dserver.port=$PORT -jar target/validation-manager-1.0.0.jar
```

7. **Deploy**:
```bash
cd ../backend
git init
git add .
git commit -m "Initial deploy"
heroku git:remote -a your-app-name
git push heroku main
```

8. **Update Connected App Callback URL** in Salesforce to:
```
https://your-app-name.herokuapp.com/#/auth/callback
```

---

### Option B: Railway (Easiest)

1. Go to [railway.app](https://railway.app) and create an account
2. Click **New Project → Deploy from GitHub repo**
3. Push your code to GitHub first
4. Add environment variables in Railway dashboard:
   - `SALESFORCE_CLIENT_ID`
   - `SALESFORCE_CLIENT_SECRET`
   - `SALESFORCE_REDIRECT_URI` (your Railway domain + `/#/auth/callback`)
5. Railway auto-detects the `pom.xml` and deploys

---

### Option C: Render

1. Go to [render.com](https://render.com)
2. **New Web Service → Build from GitHub**
3. Select your repo
4. Set:
   - **Build Command**: `mvn clean package`
   - **Start Command**: `java -jar target/validation-manager-1.0.0.jar`
5. Add environment variables in Render dashboard
6. Deploy

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/auth/url` | Get Salesforce OAuth URL |
| POST | `/api/auth/token?code={code}` | Exchange auth code for token |
| GET | `/api/validation-rules` | List all validation rules |
| POST | `/api/validation-rules/deploy` | Deploy rule changes |

---

## Project Structure

```
salesforce-validation-manager/
├── backend/              # Spring Boot 3 + Java 17
│   ├── pom.xml
│   └── src/main/java/com/salesforce/manager/
│       ├── config/       # CORS + Salesforce config
│       ├── controller/   # REST APIs
│       ├── model/        # DTOs
│       └── service/      # Salesforce Tooling API logic
│
└── frontend/             # Angular 17
    └── src/app/
        ├── components/
        │   ├── login/
        │   ├── auth-callback/
        │   └── validation-rules/
        ├── models/
        └── services/
```

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `CORS error` | Make sure backend is running and `WebConfig.java` allows `localhost:4200` |
| `invalid_client_id` | Double-check Consumer Key in `application.properties` |
| `redirect_uri_mismatch` | Ensure callback URL in Connected App matches exactly |
| `No validation rules found` | Create rules in Salesforce Setup first |
| `Deploy failed` | Check that your user has "Customize Application" permission |

---

## License

MIT
