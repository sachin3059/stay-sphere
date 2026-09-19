import { useSyncExternalStore } from "react";

const STORAGE_KEY = "staysphere_notifications_last_seen_at";
const SEEN_EVENT = "staysphere:notifications-seen";

export function getNotificationsLastSeenAt(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

export function markNotificationsSeenAt(iso: string) {
  try {
    localStorage.setItem(STORAGE_KEY, iso);
    window.dispatchEvent(
      new CustomEvent(SEEN_EVENT, { detail: iso }),
    );
  } catch {
    /* ignore quota / private mode */
  }
}

function subscribeNotificationsSeen(onStoreChange: () => void) {
  const onSeen = () => onStoreChange();
  window.addEventListener(SEEN_EVENT, onSeen);
  window.addEventListener("storage", onSeen);
  return () => {
    window.removeEventListener(SEEN_EVENT, onSeen);
    window.removeEventListener("storage", onSeen);
  };
}

/** Reactive last-seen timestamp (updates bell + inbox when markNotificationsSeenAt runs). */
export function useNotificationsLastSeen(): string | null {
  return useSyncExternalStore(
    subscribeNotificationsSeen,
    getNotificationsLastSeenAt,
    () => null,
  );
}

export function isNotificationUnread(
  createdAt: string,
  lastSeenAt: string | null,
): boolean {
  if (!lastSeenAt) return true;
  const created = Date.parse(createdAt);
  const seen = Date.parse(lastSeenAt);
  if (Number.isNaN(created) || Number.isNaN(seen)) {
    return createdAt > lastSeenAt;
  }
  return created > seen;
}
