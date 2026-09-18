import { env } from "@/config/env";
import { useAuthStore } from "@/store/authStore";
import { Home, LogIn, UserPlus } from "lucide-react";
import { Link, NavLink } from "react-router-dom";
import { Button } from "../ui/Button";

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `text-sm font-medium transition-colors ${
    isActive ? "text-brand-700" : "text-stone-600 hover:text-ink"
  }`;

export function SiteHeader() {
  const user = useAuthStore((s) => s.user);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated());

  return (
    <header className="sticky top-0 z-50 border-b border-border/80 bg-surface/90 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4 sm:px-6">
        <Link
          to="/"
          className="flex items-center gap-2 font-display text-lg font-semibold tracking-tight text-ink"
        >
          <span
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-600 text-white"
            aria-hidden
          >
            <Home className="h-4 w-4" />
          </span>
          {env.appName}
        </Link>

        <nav className="hidden items-center gap-8 md:flex" aria-label="Main">
          <NavLink to="/" className={navLinkClass} end>
            Explore
          </NavLink>
          <NavLink to="/how-it-works" className={navLinkClass}>
            How it works
          </NavLink>
        </nav>

        <div className="flex items-center gap-2">
          {isAuthenticated && user ? (
            <span className="hidden text-sm text-muted sm:inline">
              Hi, {user.fullName.split(" ")[0]}
            </span>
          ) : (
            <>
              <Link to="/login">
                <Button variant="ghost" size="sm" className="hidden sm:inline-flex">
                  <LogIn className="h-4 w-4" />
                  Sign in
                </Button>
              </Link>
              <Link to="/register">
                <Button variant="primary" size="sm">
                  <UserPlus className="h-4 w-4" />
                  Get started
                </Button>
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
