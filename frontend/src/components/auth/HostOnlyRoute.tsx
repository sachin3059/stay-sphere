import { useAuthStore } from "@/store/authStore";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { Button } from "../ui/Button";
import { Card } from "../ui/Card";

export function HostOnlyRoute({ children }: { children: ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);

  if (!accessToken) {
    return (
      <div className="mx-auto max-w-md px-4 py-16">
        <Card className="p-8 text-center">
          <p className="text-muted">Sign in to manage listings.</p>
          <Link to="/login" className="mt-4 inline-block">
            <Button>Sign in</Button>
          </Link>
        </Card>
      </div>
    );
  }

  if (user?.role !== "HOST") {
    return (
      <div className="mx-auto max-w-md px-4 py-16">
        <Card className="p-8 text-center">
          <p className="font-medium text-ink">Host account required</p>
          <p className="mt-2 text-sm text-muted">
            Use the menu → <strong>Become a host</strong>, then list your first
            property.
          </p>
          <Link to="/explore" className="mt-6 inline-block">
            <Button variant="outline">Back to explore</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return children;
}
