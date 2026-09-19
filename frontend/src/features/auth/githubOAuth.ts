const RETURN_PATH_KEY = "staysphere.github.oauth.returnTo";
const STATE_KEY = "staysphere.github.oauth.state";
const STATE_OK_KEY = "staysphere.github.oauth.state.ok";

export const GITHUB_OAUTH_CALLBACK_PATH = "/auth/github/callback";

export function githubOAuthRedirectUri(): string {
  return `${window.location.origin}${GITHUB_OAUTH_CALLBACK_PATH}`;
}

/** Persist where to send the user after GitHub sign-in completes. */
export function stashGitHubOAuthReturnTo(path: string) {
  sessionStorage.setItem(RETURN_PATH_KEY, path.startsWith("/") ? path : "/");
}

export function consumeGitHubOAuthReturnTo(): string {
  const path = sessionStorage.getItem(RETURN_PATH_KEY);
  sessionStorage.removeItem(RETURN_PATH_KEY);
  if (path && path.startsWith("/")) {
    return path;
  }
  return "/";
}

function randomState(): string {
  const bytes = new Uint8Array(16);
  crypto.getRandomValues(bytes);
  return Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
}

export function beginGitHubOAuth(redirectTo: string): string {
  stashGitHubOAuthReturnTo(redirectTo);
  sessionStorage.removeItem(STATE_OK_KEY);
  const state = randomState();
  sessionStorage.setItem(STATE_KEY, state);
  return state;
}

export function createGitHubAuthorizeUrl(clientId: string, state: string): string {
  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: githubOAuthRedirectUri(),
    scope: "read:user user:email",
    state,
  });
  return `https://github.com/login/oauth/authorize?${params}`;
}

/**
 * Validates OAuth `state` once per redirect. Safe to call from re-renders and
 * React Strict Mode remounts after the first successful check.
 */
export function verifyGitHubOAuthState(returned: string | null): boolean {
  if (!returned) return false;
  if (sessionStorage.getItem(STATE_OK_KEY) === returned) {
    return true;
  }
  const expected = sessionStorage.getItem(STATE_KEY);
  if (!expected || expected !== returned) {
    return false;
  }
  sessionStorage.removeItem(STATE_KEY);
  sessionStorage.setItem(STATE_OK_KEY, returned);
  return true;
}
