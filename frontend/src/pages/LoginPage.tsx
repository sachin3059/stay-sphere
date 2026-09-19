import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { loginRequest } from "@/features/auth/api";
import { applyAuthResponse } from "@/features/auth/session";
import { ApiError } from "@/lib/api/types";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo =
    (location.state as { from?: string } | null)?.from ?? "/";
  const sessionExpired =
    new URLSearchParams(location.search).get("session") === "expired";
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldError, setFieldError] = useState<string | null>(null);

  const login = useMutation({
    mutationFn: loginRequest,
    onSuccess: (data) => {
      applyAuthResponse(data);
      navigate(redirectTo, { replace: true });
    },
    onError: (err: unknown) => {
      if (err instanceof ApiError) {
        setFieldError(err.message);
      } else {
        setFieldError("Something went wrong. Try again.");
      }
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFieldError(null);
    if (!email.trim() || !password) {
      setFieldError("Email and password are required.");
      return;
    }
    login.mutate({ email: email.trim(), password });
  }

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to book stays or manage your listings."
      footer={
        <>
          New here?{" "}
          <Link to="/register" className="font-medium text-brand-700 hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      {sessionExpired && (
        <p className="mb-4 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-900" role="status">
          Your session expired. Please sign in again.
        </p>
      )}
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />
        <Input
          label="Password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        {fieldError && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
            {fieldError}
          </p>
        )}
        <Button
          type="submit"
          className="w-full"
          disabled={login.isPending}
        >
          {login.isPending ? "Signing in…" : "Sign in"}
        </Button>
      </form>
    </AuthShell>
  );
}
