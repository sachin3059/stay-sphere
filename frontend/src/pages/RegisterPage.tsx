import { AuthShell } from "@/components/auth/AuthShell";
import { SocialAuthButtons } from "@/components/auth/SocialAuthButtons";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { registerRequest } from "@/features/auth/api";
import { applyAuthResponse } from "@/features/auth/session";
import { ApiError } from "@/lib/api/types";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export function RegisterPage() {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fieldError, setFieldError] = useState<string | null>(null);

  const register = useMutation({
    mutationFn: registerRequest,
    onSuccess: (data) => {
      applyAuthResponse(data);
      navigate("/", { replace: true });
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
    if (!fullName.trim() || !email.trim() || password.length < 8) {
      setFieldError("Use your name, a valid email, and a password of at least 8 characters.");
      return;
    }
    register.mutate({
      fullName: fullName.trim(),
      email: email.trim(),
      password,
    });
  }

  return (
    <AuthShell
      title="Create your account"
      subtitle="You’ll start as a guest. Upgrade to host anytime to list a property."
      footer={
        <>
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-brand-700 hover:underline">
            Sign in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Full name"
          autoComplete="name"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder="Jane Doe"
          required
        />
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
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          minLength={8}
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
          disabled={register.isPending}
        >
          {register.isPending ? "Creating account…" : "Create account"}
        </Button>
      </form>
      <SocialAuthButtons redirectTo="/" />
    </AuthShell>
  );
}
