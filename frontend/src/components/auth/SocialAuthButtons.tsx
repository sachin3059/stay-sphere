import { Button } from "@/components/ui/Button";
import { googleOAuthRequest } from "@/features/auth/api";
import {
  beginGitHubOAuth,
  createGitHubAuthorizeUrl,
} from "@/features/auth/githubOAuth";
import { applyAuthResponse } from "@/features/auth/session";
import { env } from "@/config/env";
import { ApiError } from "@/lib/api/types";
import { GoogleLogin, type CredentialResponse } from "@react-oauth/google";
import { Github } from "lucide-react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

type Props = {
  redirectTo: string;
};

export function SocialAuthButtons({ redirectTo }: Props) {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const google = useMutation({
    mutationFn: (idToken: string) => googleOAuthRequest(idToken),
    onSuccess: (data) => {
      applyAuthResponse(data);
      navigate(redirectTo, { replace: true });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Google sign-in failed.",
      );
    },
  });

  const hasGoogle = Boolean(env.googleClientId);
  const hasGitHub = Boolean(env.githubClientId);

  if (!hasGoogle && !hasGitHub) {
    return null;
  }

  function startGitHub() {
    if (!env.githubClientId) return;
    setError(null);
    const state = beginGitHubOAuth(redirectTo);
    window.location.href = createGitHubAuthorizeUrl(env.githubClientId, state);
  }

  const busy = google.isPending;

  return (
    <div className="mt-6 space-y-3">
      <div className="relative py-2">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-border" />
        </div>
        <p className="relative mx-auto w-fit bg-white px-2 text-xs text-muted">
          Or continue with
        </p>
      </div>

      {hasGitHub && (
        <Button
          type="button"
          variant="outline"
          className="w-full gap-2"
          onClick={startGitHub}
          disabled={busy}
        >
          <Github className="h-4 w-4 shrink-0" aria-hidden />
          Continue with GitHub
        </Button>
      )}

      {hasGoogle && (
        <div className="flex justify-center [&>div]:w-full">
          <GoogleLogin
            onSuccess={(res: CredentialResponse) => {
              if (res.credential) {
                google.mutate(res.credential);
              }
            }}
            onError={() => setError("Google sign-in was cancelled.")}
            theme="outline"
            size="large"
            width="100%"
            text="continue_with"
          />
        </div>
      )}

      {busy && (
        <p className="text-center text-sm text-muted">Signing in…</p>
      )}

      {error && (
        <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}
