import { Button } from "@/components/ui/Button";
import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="mx-auto flex max-w-lg flex-col items-center px-4 py-24 text-center">
      <p className="text-6xl font-display font-semibold text-brand-200">404</p>
      <h1 className="mt-4 font-display text-2xl font-semibold">Page not found</h1>
      <p className="mt-2 text-sm text-muted">
        The page you’re looking for doesn’t exist or was moved.
      </p>
      <Link to="/" className="mt-8">
        <Button>Go home</Button>
      </Link>
    </div>
  );
}
