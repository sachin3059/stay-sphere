import { Card } from "@/components/ui/Card";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";

type Props = {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
};

export function AuthShell({ title, subtitle, children, footer }: Props) {
  return (
    <div className="mx-auto flex max-w-md flex-col px-4 py-12 sm:px-6 sm:py-16">
      <div className="mb-8 text-center">
        <Link
          to="/"
          className="font-display text-lg font-semibold text-brand-700"
        >
          StaySphere
        </Link>
      </div>
      <Card className="p-8">
        <h1 className="font-display text-2xl font-semibold text-ink">
          {title}
        </h1>
        <p className="mt-2 text-sm text-muted">{subtitle}</p>
        <div className="mt-8">{children}</div>
        {footer && (
          <div className="mt-6 border-t border-border pt-6 text-center text-sm text-muted">
            {footer}
          </div>
        )}
      </Card>
    </div>
  );
}
