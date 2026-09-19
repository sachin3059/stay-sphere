import { becomeHostRequest, logoutRequest } from "@/features/auth/api";
import { applyAuthResponse } from "@/features/auth/session";
import { useAuthStore } from "@/store/authStore";
import { useMutation } from "@tanstack/react-query";
import {
  Building2,
  ChevronDown,
  CalendarDays,
  LogOut,
  Plus,
  Home,
  Luggage,
  ListOrdered,
} from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export function UserMenu() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const accessToken = useAuthStore((s) => s.accessToken);
  const refreshToken = useAuthStore((s) => s.refreshToken);
  const clearSession = useAuthStore((s) => s.clearSession);
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function onDocClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, []);

  const becomeHost = useMutation({
    mutationFn: () => becomeHostRequest(accessToken!),
    onSuccess: (data) => {
      applyAuthResponse(data);
      setOpen(false);
    },
  });

  const logout = useMutation({
    mutationFn: async () => {
      if (refreshToken) {
        try {
          await logoutRequest(refreshToken);
        } catch {
          /* clear local session even if API fails */
        }
      }
      clearSession();
      setOpen(false);
      navigate("/", { replace: true });
    },
  });

  if (!user || !accessToken) {
    return null;
  }

  const roleLabel = user.role === "HOST" ? "Host" : "Guest";

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="flex items-center gap-2 rounded-xl border border-border bg-white px-3 py-2 text-sm font-medium text-ink transition-colors hover:border-brand-300"
        aria-expanded={open}
        aria-haspopup="menu"
      >
        <span className="hidden max-w-[8rem] truncate sm:inline">
          {user.fullName.split(" ")[0]}
        </span>
        <span className="rounded-md bg-stone-100 px-1.5 py-0.5 text-xs text-stone-600">
          {roleLabel}
        </span>
        <ChevronDown className="h-4 w-4 text-stone-400" />
      </button>

      {open && (
        <div
          role="menu"
          className="absolute right-0 z-50 mt-2 w-56 rounded-xl border border-border bg-white py-1 shadow-lg shadow-stone-900/10"
        >
          <div className="border-b border-border px-4 py-3">
            <p className="truncate text-sm font-medium text-ink">{user.fullName}</p>
            <p className="truncate text-xs text-muted">{user.email}</p>
          </div>

          <Link
            to="/bookings/my"
            role="menuitem"
            className="flex items-center gap-2 px-4 py-2.5 text-sm text-ink hover:bg-stone-50"
            onClick={() => setOpen(false)}
          >
            <Luggage className="h-4 w-4 text-stone-500" />
            My trips
          </Link>
          <Link
            to="/waitlist/my"
            role="menuitem"
            className="flex items-center gap-2 px-4 py-2.5 text-sm text-ink hover:bg-stone-50"
            onClick={() => setOpen(false)}
          >
            <ListOrdered className="h-4 w-4 text-stone-500" />
            My waitlist
          </Link>

          {user.role === "GUEST" && (
            <button
              type="button"
              role="menuitem"
              className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-ink hover:bg-stone-50 disabled:opacity-50"
              disabled={becomeHost.isPending}
              onClick={() => becomeHost.mutate()}
            >
              <Building2 className="h-4 w-4 text-brand-600" />
              {becomeHost.isPending ? "Upgrading…" : "Become a host"}
            </button>
          )}

          {user.role === "HOST" && (
            <>
              <Link
                to="/host/listings/new"
                role="menuitem"
                className="flex items-center gap-2 px-4 py-2.5 text-sm text-ink hover:bg-stone-50"
                onClick={() => setOpen(false)}
              >
                <Plus className="h-4 w-4 text-brand-600" />
                List a property
              </Link>
              <Link
                to="/host/listings"
                role="menuitem"
                className="flex items-center gap-2 px-4 py-2.5 text-sm text-ink hover:bg-stone-50"
                onClick={() => setOpen(false)}
              >
                <Home className="h-4 w-4 text-stone-500" />
                My listings
              </Link>
              <Link
                to="/host/reservations"
                role="menuitem"
                className="flex items-center gap-2 px-4 py-2.5 text-sm text-ink hover:bg-stone-50"
                onClick={() => setOpen(false)}
              >
                <CalendarDays className="h-4 w-4 text-stone-500" />
                Reservations
              </Link>
            </>
          )}

          <button
            type="button"
            role="menuitem"
            className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-red-700 hover:bg-red-50"
            onClick={() => logout.mutate()}
            disabled={logout.isPending}
          >
            <LogOut className="h-4 w-4" />
            Sign out
          </button>
        </div>
      )}

      {becomeHost.isError && (
        <p className="absolute right-0 top-full mt-1 text-xs text-red-600">
          Could not upgrade account.
        </p>
      )}
    </div>
  );
}
