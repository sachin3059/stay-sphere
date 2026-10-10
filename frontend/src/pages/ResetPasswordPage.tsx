import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { resetPasswordRequest } from "@/features/auth/api";
import { ApiError } from "@/lib/api/types";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const navigate = useNavigate();
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [fieldError, setFieldError] = useState<string | null>(null);

  const reset = useMutation({
    mutationFn: resetPasswordRequest,
    onSuccess: () => navigate("/login", { replace: true }),
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
    if (!token) {
      setFieldError("Missing reset token. Use the link from your email.");
      return;
    }
    if (password.length < 8) {
      setFieldError("Password must be at least 8 characters.");
      return;
    }
    if (password !== confirm) {
      setFieldError("Passwords do not match.");
      return;
    }
    reset.mutate({ token, newPassword: password });
  }

  if (!token) {
    return (
      <AuthShell
        title="Invalid link"
        subtitle="This reset link is missing a token."
        footer={
          <Link to="/forgot-password" className="font-medium text-brand-700 hover:underline">
            Request a new link
          </Link>
        }
      >
        <p className="text-sm text-muted">
          Open the full link from your email, or request a new password reset.
        </p>
      </AuthShell>
    );
  }

  return (
    <AuthShell
      title="Choose a new password"
      subtitle="Enter a new password for your account."
      footer={
        <Link to="/login" className="font-medium text-brand-700 hover:underline">
          Back to sign in
        </Link>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="New password"
          type="password"
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Input
          label="Confirm password"
          type="password"
          autoComplete="new-password"
          value={confirm}
          onChange={(e) => setConfirm(e.target.value)}
          required
        />
        {fieldError && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
            {fieldError}
          </p>
        )}
        <Button type="submit" className="w-full" disabled={reset.isPending}>
          {reset.isPending ? "Updating…" : "Update password"}
        </Button>
      </form>
    </AuthShell>
  );
}
