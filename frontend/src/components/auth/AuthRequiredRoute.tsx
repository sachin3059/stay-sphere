import { useAuthStore } from "@/store/authStore";
import type { ReactNode } from "react";
import { Link, useLocation } from "react-router-dom";
import { Button } from "../ui/Button";
import { Card } from "../ui/Card";

export function AuthRequiredRoute({ children }: { children: ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const location = useLocation();

  if (!accessToken) {
    return (
      <div className="mx-auto max-w-md px-4 py-16">
        <Card className="p-8 text-center">
          <p className="text-muted">Sign in to book stays and manage trips.</p>
          <Link
            to="/login"
            state={{ from: location.pathname }}
            className="mt-4 inline-block"
          >
            <Button>Sign in</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return children;
}
