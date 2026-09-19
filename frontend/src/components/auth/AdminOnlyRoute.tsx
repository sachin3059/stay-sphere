import { useAuthStore } from "@/store/authStore";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { Button } from "../ui/Button";
import { Card } from "../ui/Card";

export function AdminOnlyRoute({ children }: { children: ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);

  if (!accessToken) {
    return (
      <div className="mx-auto max-w-md px-4 py-16">
        <Card className="p-8 text-center">
          <p className="text-muted">Sign in to open admin tools.</p>
          <Link to="/login" className="mt-4 inline-block">
            <Button>Sign in</Button>
          </Link>
        </Card>
      </div>
    );
  }

  if (user?.role !== "ADMIN") {
    return (
      <div className="mx-auto max-w-md px-4 py-16">
        <Card className="p-8 text-center">
          <p className="font-medium text-ink">Admin access required</p>
          <p className="mt-2 text-sm text-muted">
            Your account does not have the ADMIN role.
          </p>
          <Link to="/" className="mt-6 inline-block">
            <Button variant="outline">Back home</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return children;
}
