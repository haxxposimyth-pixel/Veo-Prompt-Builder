import { VertexAI, HarmCategory, HarmBlockThreshold } from '@google-cloud/vertexai';

const location = process.env.VERTEX_LOCATION || process.env.GCP_LOCATION || 'us-central1';
const modelPro = process.env.MODEL_PRO || 'gemini-2.5-pro';
const modelFlash = process.env.MODEL_FLASH || 'gemini-2.5-flash';

// Instantiate the Vertex AI client using Service Account JSON Key or Default Credentials
let vertexAI: VertexAI;
let project = process.env.GCP_PROJECT || 'my-gcp-project';

if (process.env.GOOGLE_APPLICATION_CREDENTIALS_JSON) {
  try {
    const creds = JSON.parse(process.env.GOOGLE_APPLICATION_CREDENTIALS_JSON);
    project = creds.project_id;
    vertexAI = new VertexAI({
      project: creds.project_id,
      location: location,
      googleAuthOptions: { credentials: creds }
    });
  } catch (error) {
    console.error('Failed to parse GOOGLE_APPLICATION_CREDENTIALS_JSON, falling back to ADC:', error);
    vertexAI = new VertexAI({ project, location });
  }
} else {
  vertexAI = new VertexAI({ project, location });
}

// Requirement 1: STARTUP DIAGNOSTICS on boot
console.log(`[STARTUP DIAGNOSTICS] Resolved Project: ${project}`);
console.log(`[STARTUP DIAGNOSTICS] Location: ${location}`);
console.log(`[STARTUP DIAGNOSTICS] ModelPro: ${modelPro}`);
console.log(`[STARTUP DIAGNOSTICS] ModelFlash: ${modelFlash}`);

function isRetryableError(error: any): boolean {
  if (!error) return false;
  const status = error.status || error.statusCode || (error.response && error.response.status);
  if (status === 429 || status === 503) {
    return true;
  }
  const msg = String(error.message || '').toLowerCase();
  if (
    msg.includes('429') ||
    msg.includes('503') ||
    msg.includes('rate limit') ||
    msg.includes('quota') ||
    msg.includes('limit exceeded') ||
    msg.includes('too many requests')
  ) {
    return true;
  }
  return false;
}

export async function generateContent(
  systemInstruction: string,
  userPrompt: string,
  usePro: boolean
): Promise<string> {
  const activeModel = usePro ? modelPro : modelFlash;
  const maxAttempts = 3;
  const delays = [1000, 2000, 4000];

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      const generativeModel = vertexAI.getGenerativeModel({
        model: activeModel,
        generationConfig: {
          temperature: 0.7,
          topP: 0.95,
          maxOutputTokens: 8192
        },
        systemInstruction: {
          parts: [{ text: systemInstruction }]
        },
        // Requirement 3: Permissive safety settings to avoid safety blocks on real people / finance
        safetySettings: [
          {
            category: HarmCategory.HARM_CATEGORY_HATE_SPEECH,
            threshold: HarmBlockThreshold.BLOCK_ONLY_HIGH,
          },
          {
            category: HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
            threshold: HarmBlockThreshold.BLOCK_ONLY_HIGH,
          },
          {
            category: HarmCategory.HARM_CATEGORY_HARASSMENT,
            threshold: HarmBlockThreshold.BLOCK_ONLY_HIGH,
          },
          {
            category: HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT,
            threshold: HarmBlockThreshold.BLOCK_ONLY_HIGH,
          },
          {
            category: HarmCategory.HARM_CATEGORY_UNSPECIFIED,
            threshold: HarmBlockThreshold.BLOCK_ONLY_HIGH,
          }
        ]
      });

      const responseResult = await generativeModel.generateContent({
        contents: [{ role: 'user', parts: [{ text: userPrompt }] }]
      });

      const response = await responseResult.response;
      
      // Requirement 2: EMPTY-CANDIDATES VISIBILITY checking candidates null/empty
      if (!response.candidates || response.candidates.length === 0) {
        const blockReason = response.promptFeedback?.blockReason || 'UNKNOWN_REASON';
        const finishReason = 'EMPTY_CANDIDATES';
        const safetyRatings: any[] = [];
        
        const details = {
          promptFeedback: {
            blockReason: blockReason
          },
          candidate: {
            finishReason: finishReason,
            safetyRatings: safetyRatings
          }
        };
        const errorJson = JSON.stringify(details);
        console.warn(`[VERTEX_WARNING] Empty candidates received from Vertex AI. Safety Details:`, errorJson);
        return errorJson;
      }

      const compiledText = response.candidates[0].content.parts[0].text;
      if (!compiledText) {
        throw new Error('Vertex AI returned empty content text parts.');
      }

      return compiledText;
    } catch (error: any) {
      const isRetryable = isRetryableError(error);
      const isLastAttempt = attempt === maxAttempts;

      if (isRetryable && !isLastAttempt) {
        const delayMs = delays[attempt - 1];
        console.warn(`Vertex AI rate limit or unavailable (${error.status || error.message}). Retrying attempt ${attempt + 1}/${maxAttempts} in ${delayMs}ms...`);
        await new Promise((resolve) => setTimeout(resolve, delayMs));
      } else {
        console.error('Error contacting Vertex AI Model:', error);
        throw error;
      }
    }
  }
  throw new Error('Vertex AI generation failed after all retry attempts.');
}

export { project, location, modelPro, modelFlash };
