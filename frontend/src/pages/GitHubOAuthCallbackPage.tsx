import { AuthShell } from "@/components/auth/AuthShell";
import { githubOAuthRequest } from "@/features/auth/api";
import {
  consumeGitHubOAuthReturnTo,
  verifyGitHubOAuthState,
} from "@/features/auth/githubOAuth";
import { applyAuthResponse } from "@/features/auth/session";
import { ApiError } from "@/lib/api/types";
import { useMutation } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

const EXCHANGE_CODE_KEY = "staysphere.github.oauth.exchangeCode";

export function GitHubOAuthCallbackPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const code = params.get("code");
  const oauthError = params.get("error");
  const state = params.get("state");
  const stateCheck = useRef<"pending" | "ok" | "bad">("pending");

  const exchange = useMutation({
    mutationFn: () => githubOAuthRequest(code!),
    onSuccess: (data) => {
      sessionStorage.removeItem(EXCHANGE_CODE_KEY);
      applyAuthResponse(data);
      const returnTo = consumeGitHubOAuthReturnTo();
      navigate(returnTo, { replace: true });
    },
    onError: () => {
      sessionStorage.removeItem(EXCHANGE_CODE_KEY);
    },
  });

  if (stateCheck.current === "pending" && code && !oauthError) {
    stateCheck.current = verifyGitHubOAuthState(state) ? "ok" : "bad";
  }

  const stateInvalid =
    Boolean(code) && !oauthError && stateCheck.current === "bad";

  useEffect(() => {
    if (oauthError || !code || stateInvalid) return;
    if (sessionStorage.getItem(EXCHANGE_CODE_KEY) === code) return;
    sessionStorage.setItem(EXCHANGE_CODE_KEY, code);
    exchange.mutate();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- run once per authorization code
  }, [code, oauthError, stateInvalid]);

  if (oauthError) {
    return (
      <AuthShell
        title="Sign-in cancelled"
        subtitle="You can try again or use email and password."
      >
        <p className="text-sm text-muted">GitHub did not complete authorization.</p>
        <Link
          to="/login"
          className="mt-6 inline-block font-medium text-brand-700 hover:underline"
        >
          Back to sign in
        </Link>
      </AuthShell>
    );
  }

  if (!code) {
    return (
      <AuthShell title="Sign-in failed" subtitle="No authorization code was returned.">
        <Link
          to="/login"
          className="inline-block font-medium text-brand-700 hover:underline"
        >
          Back to sign in
        </Link>
      </AuthShell>
    );
  }

  if (stateInvalid) {
    return (
      <AuthShell
        title="Sign-in failed"
        subtitle="This sign-in request could not be verified. Please start again."
      >
        <Link
          to="/login"
          className="inline-block font-medium text-brand-700 hover:underline"
        >
          Back to sign in
        </Link>
      </AuthShell>
    );
  }

  if (exchange.isError) {
    const message =
      exchange.error instanceof ApiError
        ? exchange.error.message
        : "GitHub sign-in failed.";
    return (
      <AuthShell title="Sign-in failed" subtitle={message}>
        <Link
          to="/login"
          className="inline-block font-medium text-brand-700 hover:underline"
        >
          Back to sign in
        </Link>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      title="Almost there"
      subtitle="Completing GitHub sign-in…"
    >
      <p className="text-center text-sm text-muted" role="status">
        Please wait.
      </p>
    </AuthShell>
  );
}
