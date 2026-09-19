import { fetchMyNotifications } from "@/features/notifications/api";
import {
  isNotificationUnread,
  useNotificationsLastSeen,
} from "@/features/notifications/seen";
import { useAuthStore } from "@/store/authStore";
import { useQuery } from "@tanstack/react-query";
import { Bell } from "lucide-react";
import { Link } from "react-router-dom";

const POLL_MS = 60_000;

export function NotificationBell() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const lastSeen = useNotificationsLastSeen();

  const { data } = useQuery({
    queryKey: ["my-notifications"],
    queryFn: () => fetchMyNotifications(accessToken!),
    enabled: Boolean(accessToken),
    refetchInterval: POLL_MS,
    refetchOnWindowFocus: true,
  });

  if (!accessToken) {
    return null;
  }

  const unreadCount =
    data?.filter((n) => isNotificationUnread(n.createdAt, lastSeen)).length ??
    0;

  return (
    <Link
      to="/notifications"
      className="relative flex h-10 w-10 items-center justify-center rounded-xl border border-border bg-white text-stone-600 transition-colors hover:border-brand-300 hover:text-ink"
      aria-label={
        unreadCount > 0
          ? `Notifications, ${unreadCount} unread`
          : "Notifications"
      }
    >
      <Bell className="h-4 w-4" aria-hidden />
      {unreadCount > 0 && (
        <span
          className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-brand-600 px-1 text-[10px] font-semibold text-white"
          aria-hidden
        >
          {unreadCount > 9 ? "9+" : unreadCount}
        </span>
      )}
    </Link>
  );
}
