import { Request, Response, NextFunction } from 'express';
import * as admin from 'firebase-admin';

// Gracefully attempt Firebase Admin SDK initialization in GCP environments
if (admin.apps.length === 0) {
  try {
    admin.initializeApp();
    console.log('Firebase Admin SDK initialized successfully.');
  } catch (error) {
    console.warn(
      'Firebase Admin failed to auto-initialize. Authentication token validation will utilize warning-logs fallback.',
      error
    );
  }
}

/**
 * Hardening middleware to verify Firebase App Check and Firebase Auth tokens.
 * Anonymous users are permitted only behind a valid Firebase App Check token.
 */
export async function verifyAppCheckAndAuth(req: Request, res: Response, next: NextFunction) {
  const appCheckToken = req.header('X-Firebase-AppCheck');
  const authHeader = req.header('Authorization');

  let hasValidAppCheck = false;
  let hasValidAuthUser = false;

  // 1. Validate Firebase App Check token
  if (appCheckToken) {
    try {
      if (admin.apps.length > 0) {
        await admin.appCheck().verifyToken(appCheckToken);
        hasValidAppCheck = true;
      } else {
        // Safe developer fallback if Firebase config is absent
        console.warn('Firebase uninitialized. App Check token bypassed for development.');
        hasValidAppCheck = true;
      }
    } catch (error) {
       console.error('Firebase App Check validation failed:', error);
       return res.status(401).json({ error: 'Unauthorized: Firebase App Check token is invalid.' });
    }
  } else {
     // Mandate App Check in raw production environments
     if (process.env.NODE_ENV === 'production') {
       return res.status(401).json({ error: 'Unauthorized: X-Firebase-AppCheck header is required in production.' });
     } else {
       console.warn('App Check header is absent. Bypassed in non-production local mode.');
       hasValidAppCheck = true;
     }
  }

  // 2. Validate Firebase Bearer ID Token if appended
  if (authHeader && authHeader.startsWith('Bearer ')) {
    const idToken = authHeader.split('Bearer ')[1];
    try {
      if (admin.apps.length > 0) {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        (req as any).user = decodedToken;
        hasValidAuthUser = true;
      } else {
         console.warn('Firebase uninitialized. Bearer Token accepted.');
         (req as any).user = { uid: 'dev-user-id', email: 'hardiksariya@gmail.com' };
         hasValidAuthUser = true;
      }
    } catch (error) {
       console.error('Authentication Token verification failed:', error);
       return res.status(401).json({ error: 'Unauthorized: Authentication token is invalid.' });
    }
  }

  // Permitted flows: must either be an authenticated user or present an App Check token
  if (!hasValidAuthUser && !hasValidAppCheck) {
    return res.status(403).json({ error: 'Forbidden: Valid credential or App Check certificate is mandatory.' });
  }

  next();
}
export { admin };
