import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/Button";
import { verifyEmailRequest } from "@/features/auth/api";
import { applyAuthResponse } from "@/features/auth/session";
import { ApiError } from "@/lib/api/types";
import { useMutation } from "@tanstack/react-query";
import { useEffect } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const navigate = useNavigate();

  const verify = useMutation({
    mutationFn: () => verifyEmailRequest(token),
    onSuccess: (data) => {
      applyAuthResponse(data);
      navigate("/", { replace: true });
    },
  });

  useEffect(() => {
    if (token) {
      verify.mutate();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  if (!token) {
    return (
      <AuthShell
        title="Invalid link"
        subtitle="This verification link is missing a token."
        footer={
          <Link to="/check-email" className="font-medium text-brand-700 hover:underline">
            Request a new link
          </Link>
        }
      >
        <p className="text-sm text-muted">Use the full link from your email.</p>
      </AuthShell>
    );
  }

  if (verify.isPending || verify.isIdle) {
    return (
      <AuthShell title="Verifying email" subtitle="Please wait…">
        <p className="text-sm text-muted" role="status">Confirming your address…</p>
      </AuthShell>
    );
  }

  if (verify.isError) {
    const message =
      verify.error instanceof ApiError
        ? verify.error.message
        : "Verification failed.";
    return (
      <AuthShell
        title="Verification failed"
        subtitle={message}
        footer={
          <Link to="/check-email" className="font-medium text-brand-700 hover:underline">
            Resend verification email
          </Link>
        }
      >
        <Button type="button" className="w-full" onClick={() => verify.mutate()}>
          Try again
        </Button>
      </AuthShell>
    );
  }

  return null;
}
