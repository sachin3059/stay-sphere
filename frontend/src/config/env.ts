function optionalEnv(value: string | undefined): string | undefined {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}

export const env = {
  apiUrl: import.meta.env.VITE_API_URL?.trim() ?? "http://localhost:8080",
  appName: "StaySphere",
  googleClientId: optionalEnv(import.meta.env.VITE_GOOGLE_CLIENT_ID),
  githubClientId: optionalEnv(import.meta.env.VITE_GITHUB_CLIENT_ID),
} as const;
