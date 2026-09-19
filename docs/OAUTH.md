# Social sign-in (Google & GitHub)

StaySphere issues the **same JWT** as email login after OAuth.

## Environment

Repo-root `.env` (auth-service):

```env
GOOGLE_CLIENT_ID=....apps.googleusercontent.com
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

Frontend `frontend/.env`:

```env
VITE_GOOGLE_CLIENT_ID=....apps.googleusercontent.com
VITE_GITHUB_CLIENT_ID=...
```

Restart **auth-service** and the Vite dev server after changes.

## Google

1. [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Credentials → **OAuth client ID** (Web).
2. **Authorized JavaScript origins:** `http://localhost:5173`
3. Copy **Client ID** into `GOOGLE_CLIENT_ID` and `VITE_GOOGLE_CLIENT_ID`.

API: `POST /api/auth/oauth/google` with `{ "idToken": "<GIS credential>" }`.

## GitHub

1. GitHub → Settings → Developer settings → **OAuth Apps** → New.
2. **Authorization callback URL:** `http://localhost:5173/auth/github/callback`
3. Copy **Client ID** and generate **Client secret** → `.env` as above.

Flow: browser redirects to GitHub → callback page → `POST /api/auth/oauth/github` with `{ "code": "..." }`.

## Account linking

If the OAuth email matches an existing **email/password** user, the Google/GitHub id is **linked** to that account. Password login still works when a password is set.

OAuth-only users have no password; they must use social sign-in.

## Not implemented

- Forgot password / email verification (Phase 17.6)
- ChatGPT sign-in
