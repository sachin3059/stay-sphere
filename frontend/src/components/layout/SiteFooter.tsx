import { env } from "@/config/env";
import { Link } from "react-router-dom";

const columns = [
  {
    title: "Support",
    links: [
      { label: "How it works", to: "/how-it-works" },
      { label: "Explore stays", to: "/explore" },
    ],
  },
  {
    title: "Hosting",
    links: [
      { label: "List your space", to: "/host/listings/new" },
      { label: "Host dashboard", to: "/host/listings" },
      { label: "Reservations", to: "/host/reservations" },
    ],
  },
  {
    title: "StaySphere",
    links: [
      { label: "Sign in", to: "/login" },
      { label: "Create account", to: "/register" },
      { label: "My trips", to: "/bookings/my" },
    ],
  },
];

export function SiteFooter() {
  return (
    <footer className="mt-auto border-t border-stone-200 bg-surface-muted">
      <div className="page-container grid gap-10 py-12 sm:grid-cols-2 lg:grid-cols-4">
        <div className="lg:col-span-1">
          <p className="text-lg font-bold text-brand-600">{env.appName}</p>
          <p className="mt-3 max-w-xs text-sm leading-relaxed text-muted">
            Book unique homes and experiences. Secure payments, real-time
            availability, and host tools built in.
          </p>
        </div>
        {columns.map((col) => (
          <div key={col.title}>
            <p className="text-xs font-bold uppercase tracking-wider text-ink">
              {col.title}
            </p>
            <ul className="mt-4 space-y-3">
              {col.links.map((link) => (
                <li key={link.to}>
                  <Link
                    to={link.to}
                    className="text-sm text-muted transition-colors hover:text-ink hover:underline"
                  >
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
      <div className="border-t border-stone-200">
        <div className="page-container flex flex-col gap-2 py-6 text-sm text-muted sm:flex-row sm:items-center sm:justify-between">
          <p>© {new Date().getFullYear()} {env.appName}, Inc.</p>
          <p className="text-xs text-stone-400">Privacy · Terms · Sitemap</p>
        </div>
      </div>
    </footer>
  );
}
