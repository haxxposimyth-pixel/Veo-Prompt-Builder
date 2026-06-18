# VEO Doc Builder Backend Service

Production-ready Express + TypeScript backend service power-housing the VEO 3 Documentary AI prompt engine. Deploys natively to Google Cloud Run, utilizing GCP Application Default Credentials to call Vertex AI securely.

---

## Technical Stack & Security

- **Framework**: Node.js + Express + TypeScript
- **Model Client**: `@google-cloud/vertexai` (Gemini 2.5 Pro / Flash)
- **Authentications**: `firebase-admin` supporting Firebase App Check validation and Firebase Bearer authentication
- **Hardening**: `helmet` headers, body size limitations, and IP-scoped request rate limits (`express-rate-limit`)

---

## Deployment to Google Cloud Run

To deploy this backend service to Google Cloud Run under your GCP project:

### 1. Enable Vertex AI & Artifact Registry
Ensure Google Cloud APIs are active:
\`\`\`bash
gcloud services enable run.googleapis.com \
                       artifactregistry.googleapis.com \
                       aiplatform.googleapis.com
\`\`\`

### 2. Configure Service Account Permissions
The service account assigned to your Google Cloud Run instance (default is the Compute Engine Service Account) requires permissions to query Vertex AI. Grant the **Vertex AI User** (\`roles/aiplatform.user\`) role:

\`\`\`bash
PROJECT_ID=$(gcloud config get-value project)
PROJECT_NUM=$(gcloud projects list --filter="project_id:$PROJECT_ID" --format="value(project_number)")

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$PROJECT_NUM-compute@developer.gserviceaccount.com" \
    --role="roles/aiplatform.user"
\`\`\`

### 3. Deploy the Container
Run the build command from inside the \`/backend\` directory:

\`\`\`bash
gcloud run deploy veo-backend \
    --source . \
    --region us-central1 \
    --platform managed \
    --allow-unauthenticated \
    --set-env-vars="GCP_PROJECT=$PROJECT_ID,GCP_LOCATION=us-central1,MODEL_PRO=gemini-2.5-pro,MODEL_FLASH=gemini-2.5-flash,ALLOWED_ORIGIN=*"
\`\`\`

---

## Local Development Setup

### 1. Launch Dependencies
\`\`\`bash
npm install
\`\`\`

### 2. Configure Local Settings
Create a \`.env\` file inside the \`/backend\` folder:
\`\`\`env
PORT=8080
GCP_PROJECT=your-gcp-project-id
GCP_LOCATION=us-central1
MODEL_PRO=gemini-2.5-pro
MODEL_FLASH=gemini-2.5-flash
ALLOWED_ORIGIN=*
\`\`\`

### 3. Authenticate locally with Google Cloud
Ensure your local terminal has Application Default Credentials set up:
\`\`\`bash
gcloud auth application-default login
\`\`\`

### 4. Run the Dev Server
\`\`\`bash
npm run dev
\`\`\`
The server should launch securely on \`http://localhost:8080\`.
