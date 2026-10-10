import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { forgotPasswordRequest } from "@/features/auth/api";
import { ApiError } from "@/lib/api/types";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [fieldError, setFieldError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);

  const forgot = useMutation({
    mutationFn: forgotPasswordRequest,
    onSuccess: () => setSent(true),
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
    if (!email.trim()) {
      setFieldError("Email is required.");
      return;
    }
    forgot.mutate({ email: email.trim() });
  }

  return (
    <AuthShell
      title="Forgot password"
      subtitle="We will email you a link to reset your password."
      footer={
        <>
          Remembered it?{" "}
          <Link to="/login" className="font-medium text-brand-700 hover:underline">
            Sign in
          </Link>
        </>
      }
    >
      {sent ? (
        <p className="rounded-lg bg-brand-50 px-3 py-2 text-sm text-brand-900" role="status">
          If an account exists for that email, we sent reset instructions. Check your inbox
          (or auth-service logs in local dev when SendGrid is not configured).
        </p>
      ) : (
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
          {fieldError && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
              {fieldError}
            </p>
          )}
          <Button type="submit" className="w-full" disabled={forgot.isPending}>
            {forgot.isPending ? "Sending…" : "Send reset link"}
          </Button>
        </form>
      )}
    </AuthShell>
  );
}
