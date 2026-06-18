import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';
import { verifyAppCheckAndAuth } from './auth';
import { assembleInstruction, PromptAssemblyInputs } from './promptEngine';
import { generateContent } from './vertex';

// Configuration
dotenv.config();

const app = express();
const port = process.env.PORT || 8080;
const allowedOrigin = process.env.ALLOWED_ORIGIN || '*';

// Hardening
app.use(helmet());
app.use(express.json({ limit: '1mb' })); // Input sanitation of request body sizes

// CORS Restriction
app.use(
  cors({
    origin: allowedOrigin === '*' ? true : allowedOrigin,
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type', 'X-Firebase-AppCheck', 'Authorization']
  })
);

// Rate Limiting (30 requests per 5 minutes per IP)
const limiter = rateLimit({
  windowMs: 5 * 60 * 1000,
  max: 30,
  message: { error: 'Too many generation requests from this IP. Please try again after 5 minutes.' },
  standardHeaders: true,
  legacyHeaders: false
});
app.use('/generate', limiter);

// ==========================================
// ENDPOINTS
// ==========================================

// GET /health
app.get('/health', (req: Request, res: Response) => {
  res.status(200).json({ status: 'ok', timestamp: new Date().toISOString() });
});

// POST /generate
app.post('/generate', verifyAppCheckAndAuth, async (req: Request, res: Response) => {
  const startTime = Date.now();
  const {
    mode,
    niche,
    customNiche,
    videoStyle,
    customStyleDescription,
    topic,
    aspectRatio,
    language,
    phase,
    model,
    bible,
    blueprint
  } = req.body;

  // Validation
  if (!mode || !['analysis', 'phase', 'titles'].includes(mode)) {
    return res.status(400).json({ error: 'Invalid or missing field: mode' });
  }
  if (!niche || typeof niche !== 'string' || niche.length > 100) {
    return res.status(400).json({ error: 'Invalid or missing field: niche' });
  }
  if (!topic || typeof topic !== 'string' || topic.length > 2000) {
    return res.status(400).json({ error: 'Invalid or missing field: topic' });
  }
  if (!aspectRatio || !['16:9', '9:16'].includes(aspectRatio)) {
    return res.status(400).json({ error: 'Invalid or missing field: aspectRatio' });
  }
  if (!language || typeof language !== 'string' || language.length > 50) {
    return res.status(400).json({ error: 'Invalid or missing field: language' });
  }
  if (mode === 'phase' && (phase === undefined || phase === null || phase < 1 || phase > 10)) {
    return res.status(400).json({ error: 'A phase parameter between 1 and 10 is required in phase mode' });
  }

  try {
    const inputs: PromptAssemblyInputs = {
      mode,
      niche,
      customNiche: customNiche || null,
      videoStyle,
      customStyleDescription: customStyleDescription || null,
      topic,
      aspectRatio,
      language,
      phase: phase || null,
      bible: bible || null,
      blueprint: blueprint || null
    };

    // 1. Build prompt dynamically using promptEngine
    const { systemInstruction, userPrompt } = assembleInstruction(inputs);

    const isPro = model !== 'flash';

    // 2. Call generative model on Vertex AI
    const generatedText = await generateContent(systemInstruction, userPrompt, isPro);

    // 3. Structured Logging (do NOT output raw prompts to preservation logs in production)
    const latencyMs = Date.now() - startTime;
    console.log(
      JSON.stringify({
        event: 'GENERATION_SUCCESS',
        mode,
        niche,
        model: isPro ? 'gemini-2.5-pro' : 'gemini-2.5-flash',
        aspectRatio,
        language,
        phase,
        latencyMs,
        charCount: generatedText.length
      })
    );

    // 4. Return result
    return res.status(200).json({ text: generatedText });

  } catch (error: any) {
    const latencyMs = Date.now() - startTime;
    console.error(
      JSON.stringify({
        event: 'GENERATION_FAILURE',
        mode,
        latencyMs,
        error: error.message || error
      })
    );

    return res.status(502).json({
      error: 'Vertex AI Model generation failed. Check server logs.',
      details: error.message || error
    });
  }
});

// Catch-all 404 handler returning JSON
app.use((req: Request, res: Response) => {
  res.status(404).json({
    error: 'Not Found',
    details: `Cannot ${req.method} ${req.path}`
  });
});

// Global Express error-handling middleware returning JSON
app.use((err: any, req: Request, res: Response, next: NextFunction) => {
  console.error('Unhandled express error:', err);
  const statusCode = err.status || err.statusCode || 500;
  res.status(statusCode).json({
    error: err.name || 'InternalServerError',
    details: err.message || String(err)
  });
});

// Start Server
app.listen(port, () => {
  console.log(`VEO Server running on port ${port} with CORS allowed origin: ${allowedOrigin}`);
});
export default app;
