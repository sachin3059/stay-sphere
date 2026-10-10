import { resendVerificationRequest } from "@/features/auth/api";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link } from "react-router-dom";

export function EmailVerificationBanner() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);
  const [message, setMessage] = useState<string | null>(null);

  const resend = useMutation({
    mutationFn: () => resendVerificationRequest(accessToken!),
    onSuccess: () =>
      setMessage("Verification email sent. Check your inbox or auth-service logs in dev."),
    onError: (err: unknown) => {
      setMessage(
        err instanceof ApiError ? err.message : "Could not resend verification email.",
      );
    },
  });

  if (!user || (user.emailVerified ?? true)) {
    return null;
  }

  return (
    <div
      className="border-b border-amber-200 bg-amber-50 px-4 py-2 text-center text-sm text-amber-950"
      role="status"
    >
      Verify your email to book stays.{" "}
      <Link to="/check-email" className="font-medium underline">
        Instructions
      </Link>
      {" · "}
      <button
        type="button"
        className="font-medium underline"
        disabled={resend.isPending || !accessToken}
        onClick={() => {
          setMessage(null);
          resend.mutate();
        }}
      >
        {resend.isPending ? "Sending…" : "Resend email"}
      </button>
      {message && <span className="ml-2 text-amber-800">— {message}</span>}
    </div>
  );
}
