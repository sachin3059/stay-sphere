import { Card } from "@/components/ui/Card";
import { env } from "@/config/env";
import { Link } from "react-router-dom";

const healthChecks = [
  { label: "Auth", path: "/api/auth/health" },
  { label: "Properties", path: "/api/properties/health" },
  { label: "Bookings", path: "/api/bookings/health" },
  { label: "Payments", path: "/api/payments/health" },
  { label: "Notifications", path: "/api/notifications/health" },
];

export function AdminDashboardPage() {
  const base = env.apiUrl.replace(/\/$/, "");

  return (
    <div className="mx-auto max-w-4xl px-4 py-10 sm:px-6">
      <h1 className="font-display text-3xl font-semibold text-ink">Admin</h1>
      <p className="mt-2 text-muted">
        Platform operator tools for users and listing moderation.
      </p>

      <div className="mt-10 grid gap-4 sm:grid-cols-2">
        <Link to="/admin/users">
          <Card className="p-6 transition hover:border-brand-300">
            <h2 className="font-semibold text-ink">Users</h2>
            <p className="mt-1 text-sm text-muted">
              View accounts and assign GUEST, HOST, or ADMIN roles.
            </p>
          </Card>
        </Link>
        <Link to="/admin/listings">
          <Card className="p-6 transition hover:border-brand-300">
            <h2 className="font-semibold text-ink">Listings</h2>
            <p className="mt-1 text-sm text-muted">
              Moderate status: active, inactive, or under review.
            </p>
          </Card>
        </Link>
      </div>

      <section className="mt-12">
        <h2 className="text-lg font-semibold text-ink">Service health (via gateway)</h2>
        <p className="mt-1 text-sm text-muted">
          Open in a new tab; no auth required for these health endpoints.
        </p>
        <ul className="mt-4 space-y-2">
          {healthChecks.map((item) => (
            <li key={item.path}>
              <a
                href={`${base}${item.path}`}
                target="_blank"
                rel="noreferrer"
                className="text-sm font-medium text-brand-700 hover:underline"
              >
                {item.label}
              </a>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
