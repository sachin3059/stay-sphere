import { Card } from "@/components/ui/Card";
import { useAuthStore } from "@/store/authStore";
import { Link } from "react-router-dom";

export function ProfilePage() {
  const user = useAuthStore((s) => s.user);

  if (!user) {
    return null;
  }

  const roleLabel =
    user.role === "HOST" ? "Host" : user.role === "ADMIN" ? "Admin" : "Guest";

  return (
    <div className="mx-auto max-w-lg px-4 py-10 sm:px-6">
      <h1 className="font-display text-3xl font-semibold">Your profile</h1>
      <p className="mt-2 text-muted">Account details from your session.</p>

      <Card className="mt-8 p-6">
        <dl className="space-y-4 text-sm">
          <div>
            <dt className="text-muted">Name</dt>
            <dd className="mt-1 font-medium text-ink">{user.fullName}</dd>
          </div>
          <div>
            <dt className="text-muted">Email</dt>
            <dd className="mt-1 font-medium text-ink">{user.email}</dd>
          </div>
          <div>
            <dt className="text-muted">Role</dt>
            <dd className="mt-1 font-medium text-ink">{roleLabel}</dd>
          </div>
        </dl>
        <p className="mt-6 text-xs text-muted">
          Email and name come from sign-in (email/password or social). Profile
          editing is not available yet.
        </p>
      </Card>

      <Link
        to="/"
        className="mt-6 inline-block text-sm font-medium text-brand-700 hover:underline"
      >
        ← Home
      </Link>
    </div>
  );
}
