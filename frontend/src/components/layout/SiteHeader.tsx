import { env } from "@/config/env";
import { useAuthStore } from "@/store/authStore";
import { Globe2, Menu } from "lucide-react";
import { Link, NavLink } from "react-router-dom";
import { NotificationBell } from "../notifications/NotificationBell";
import { UserMenu } from "./UserMenu";

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `text-sm font-semibold transition-colors ${
    isActive ? "text-ink underline decoration-2 underline-offset-8" : "text-stone-600 hover:text-ink"
  }`;

export function SiteHeader() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);

  const hostCta =
    user?.role === "HOST"
      ? { to: "/host/listings/new", label: "List your space" }
      : { to: accessToken ? "/how-it-works" : "/register", label: "Become a host" };

  return (
    <header className="sticky top-0 z-50 border-b border-border/80 bg-surface/95 backdrop-blur-md">
      <div className="page-container flex h-[4.25rem] items-center justify-between gap-4">
        <Link
          to="/"
          className="flex items-center gap-2 text-xl font-bold tracking-tight text-brand-600"
        >
          <span
            className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white"
            aria-hidden
          >
            S
          </span>
          <span className="hidden sm:inline">{env.appName}</span>
        </Link>

        <nav className="hidden items-center gap-8 md:flex" aria-label="Main">
          <NavLink to="/explore" className={navLinkClass}>
            Stays
          </NavLink>
          <NavLink to="/how-it-works" className={navLinkClass}>
            How it works
          </NavLink>
        </nav>

        <div className="flex items-center gap-2 sm:gap-3">
          <Link
            to={hostCta.to}
            className="hidden rounded-full px-4 py-2.5 text-sm font-semibold text-ink transition-colors hover:bg-stone-100 sm:inline"
          >
            {hostCta.label}
          </Link>

          {accessToken ? (
            <>
              <NavLink
                to="/bookings/my"
                className="hidden text-sm font-semibold text-stone-600 hover:text-ink lg:inline"
              >
                Trips
              </NavLink>
              <NotificationBell />
              <UserMenu />
            </>
          ) : (
            <div className="flex items-center rounded-full border border-stone-300 bg-white shadow-sm transition-shadow hover:shadow-md">
              <Link
                to="/login"
                className="hidden items-center gap-2 border-r border-stone-300 px-4 py-2.5 text-sm font-semibold text-ink sm:flex"
              >
                <Globe2 className="h-4 w-4 text-stone-600" aria-hidden />
                Sign in
              </Link>
              <Link
                to="/register"
                className="flex items-center gap-2 px-3 py-2.5 text-sm font-semibold text-ink sm:px-4"
              >
                <Menu className="h-4 w-4 sm:hidden" aria-hidden />
                <span className="hidden sm:inline">Sign up</span>
                <span className="sm:hidden">Join</span>
              </Link>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
