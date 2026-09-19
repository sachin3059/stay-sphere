import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { fetchMyNotifications } from "@/features/notifications/api";
import {
  notificationActionHref,
  notificationTypeLabel,
} from "@/features/notifications/notificationLinks";
import {
  isNotificationUnread,
  markNotificationsSeenAt,
  useNotificationsLastSeen,
} from "@/features/notifications/seen";
import type { AppNotification } from "@/features/notifications/types";
import { useAuthStore } from "@/store/authStore";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useRef } from "react";
import { Link } from "react-router-dom";

function typeBadgeClass(type: string) {
  if (type.startsWith("PAYMENT")) {
    return type.includes("FAILED")
      ? "bg-red-100 text-red-800"
      : "bg-green-100 text-green-800";
  }
  if (type === "BOOKING_CANCELLED") return "bg-stone-100 text-stone-700";
  if (type === "SLOT_OFFERED") return "bg-amber-100 text-amber-900";
  if (type === "BOOKING_CONFIRMED") return "bg-green-100 text-green-800";
  return "bg-brand-100 text-brand-900";
}

function previewMessage(message: string) {
  const line = message.split("\n").find((l) => l.trim().length > 0) ?? message;
  return line.length > 160 ? `${line.slice(0, 157)}…` : line;
}

function NotificationRow({
  item,
  unread,
  role,
}: {
  item: AppNotification;
  unread: boolean;
  role: string;
}) {
  const action = notificationActionHref(item, role);

  return (
    <Card
      className={`p-5 transition-colors ${unread ? "border-brand-200 bg-brand-50/40" : ""}`}
    >
      <div className="flex gap-3">
        {unread && (
          <span
            className="mt-2 h-2 w-2 shrink-0 rounded-full bg-brand-600"
            aria-hidden
          />
        )}
        <div className={`min-w-0 flex-1 ${unread ? "" : "pl-5"}`}>
          <div className="flex flex-wrap items-center gap-2">
            <span
              className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${typeBadgeClass(item.notificationType)}`}
            >
              {notificationTypeLabel(item.notificationType)}
            </span>
            {item.status === "FAILED" && (
              <span className="text-xs text-red-600">Email not delivered</span>
            )}
            <time
              className="text-xs text-muted"
              dateTime={item.createdAt}
            >
              {new Date(item.createdAt).toLocaleString()}
            </time>
          </div>
          <h2 className="mt-2 font-medium text-ink">{item.subject}</h2>
          <p className="mt-1 whitespace-pre-line text-sm text-muted">
            {previewMessage(item.message)}
          </p>
          {action && (
            <Link
              to={action.to}
              className="mt-3 inline-block text-sm font-medium text-brand-700 hover:underline"
            >
              {action.label}
            </Link>
          )}
        </div>
      </div>
    </Card>
  );
}

export function MyNotificationsPage() {
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const user = useAuthStore((s) => s.user);
  const lastSeen = useNotificationsLastSeen();
  const markedForLoadRef = useRef<string | null>(null);

  const { data, isLoading, error, refetch, isFetching } = useQuery({
    queryKey: ["my-notifications"],
    queryFn: () => fetchMyNotifications(accessToken),
    refetchOnWindowFocus: true,
  });

  useEffect(() => {
    if (isLoading || error) return;
    const loadKey = data?.map((n) => n.id).join(",") ?? "empty";
    if (markedForLoadRef.current === loadKey) return;
    markedForLoadRef.current = loadKey;
    markNotificationsSeenAt(new Date().toISOString());
  }, [data, isLoading, error]);

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="font-display text-3xl font-semibold">Notifications</h1>
          <p className="mt-2 text-muted">
            Booking, payment, and waitlist updates from your account.
          </p>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="sm"
          disabled={isFetching}
          onClick={() => refetch()}
        >
          {isFetching ? "Refreshing…" : "Refresh"}
        </Button>
      </div>

      {error && (
        <p className="mt-6 text-sm text-red-600">Could not load notifications.</p>
      )}

      {isLoading ? (
        <p className="mt-10 text-muted">Loading…</p>
      ) : data && data.length > 0 ? (
        <ul className="mt-8 space-y-4">
          {data.map((item) => (
            <li key={item.id}>
              <NotificationRow
                item={item}
                unread={isNotificationUnread(item.createdAt, lastSeen)}
                role={user?.role ?? "GUEST"}
              />
            </li>
          ))}
        </ul>
      ) : (
        <Card className="mt-10 p-8 text-center">
          <p className="text-muted">No notifications yet.</p>
          <p className="mt-2 text-sm text-muted">
            Complete a booking or join a waitlist to see updates here.
          </p>
          <Link
            to="/explore"
            className="mt-4 inline-block text-sm font-medium text-brand-700 hover:underline"
          >
            Explore stays
          </Link>
        </Card>
      )}
    </div>
  );
}
