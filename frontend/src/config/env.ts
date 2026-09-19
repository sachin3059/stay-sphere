function optionalEnv(value: string | undefined): string | undefined {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}

/** Dev: VITE_API_URL or localhost:8080. Production build: same origin + nginx /api proxy. */
function resolveApiUrl(): string {
  const fromEnv = optionalEnv(import.meta.env.VITE_API_URL);
  if (fromEnv) return fromEnv.replace(/\/$/, "");
  if (import.meta.env.PROD && typeof window !== "undefined") {
    return window.location.origin.replace(/\/$/, "");
  }
  return "http://localhost:8080";
}

export const env = {
  apiUrl: resolveApiUrl(),
  appName: "StaySphere",
  googleClientId: optionalEnv(import.meta.env.VITE_GOOGLE_CLIENT_ID),
  githubClientId: optionalEnv(import.meta.env.VITE_GITHUB_CLIENT_ID),
  olaMapsApiKey: optionalEnv(import.meta.env.VITE_OLA_MAPS_API_KEY),
} as const;
