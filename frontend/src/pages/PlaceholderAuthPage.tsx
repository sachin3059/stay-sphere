import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Link } from "react-router-dom";

type Props = {
  mode: "login" | "register";
};

export function PlaceholderAuthPage({ mode }: Props) {
  const isLogin = mode === "login";
  return (
    <div className="mx-auto flex max-w-md flex-col px-4 py-16 sm:px-6">
      <Card className="p-8 text-center">
        <h1 className="font-display text-2xl font-semibold">
          {isLogin ? "Sign in" : "Create account"}
        </h1>
        <p className="mt-3 text-sm text-muted">
          Auth screens are <strong>Step 2</strong> of the frontend build. The
          API client and session store are already wired in{" "}
          <code className="rounded bg-stone-100 px-1 text-xs">src/lib</code>{" "}
          and{" "}
          <code className="rounded bg-stone-100 px-1 text-xs">src/store</code>.
        </p>
        <Link to="/" className="mt-6 inline-block">
          <Button variant="outline">Back to home</Button>
        </Link>
      </Card>
    </div>
  );
}
