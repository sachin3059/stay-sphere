import { AuthShell } from "@/components/auth/AuthShell";
import { Button } from "@/components/ui/Button";
import { resendVerificationRequest } from "@/features/auth/api";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";

export function CheckEmailPage() {
  const navigate = useNavigate();
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);

  const resend = useMutation({
    mutationFn: () => resendVerificationRequest(accessToken!),
  });

  if (user?.emailVerified) {
    navigate("/", { replace: true });
    return null;
  }

  return (
    <AuthShell
      title="Check your email"
      subtitle={
        user
          ? `We sent a verification link to ${user.email}.`
          : "Sign in to resend a verification link."
      }
      footer={
        <Link to="/" className="font-medium text-brand-700 hover:underline">
          Continue browsing
        </Link>
      }
    >
      <p className="text-sm text-muted">
        Open the link in the email to verify your account. Until then, you can explore
        listings but cannot complete a booking.
      </p>
      {accessToken ? (
        <Button
          type="button"
          className="mt-4 w-full"
          disabled={resend.isPending}
          onClick={() => resend.mutate()}
        >
          {resend.isPending ? "Sending…" : "Resend verification email"}
        </Button>
      ) : (
        <Link
          to="/login"
          className="mt-4 inline-block text-sm font-medium text-brand-700 hover:underline"
        >
          Sign in to resend
        </Link>
      )}
      {resend.isError && (
        <p className="mt-3 text-sm text-red-700" role="alert">
          {resend.error instanceof ApiError
            ? resend.error.message
            : "Could not resend email."}
        </p>
      )}
      {resend.isSuccess && (
        <p className="mt-3 text-sm text-brand-800" role="status">
          Email sent (or logged in auth-service when SMTP is not configured).
        </p>
      )}
    </AuthShell>
  );
}
