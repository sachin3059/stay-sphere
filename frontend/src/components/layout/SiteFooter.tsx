import { env } from "@/config/env";

export function SiteFooter() {
  return (
    <footer className="border-t border-border bg-white">
      <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-10 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <p className="text-sm text-muted">
          © {new Date().getFullYear()} {env.appName}. Microservices demo — auth,
          bookings & Stripe payments.
        </p>
        <p className="text-xs text-stone-400">
          API gateway · React · Spring Boot
        </p>
      </div>
    </footer>
  );
}
