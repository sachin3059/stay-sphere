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
          className="text-xl font-bold text-brand-600"
        >
          StaySphere
        </Link>
      </div>
      <Card className="border-stone-200 p-8 shadow-[0_6px_16px_rgba(0,0,0,0.06)]">
        <h1 className="text-2xl font-semibold tracking-tight text-ink">
          {title}
        </h1>
        <p className="mt-2 text-sm text-muted">{subtitle}</p>
        <div className="mt-8">{children}</div>
        {footer && (
          <div className="mt-6 border-t border-stone-200 pt-6 text-center text-sm text-muted">
            {footer}
          </div>
        )}
      </Card>
    </div>
  );
}
