import {
  fetchAdminListings,
  updateAdminListingStatus,
} from "@/features/admin/api";
import { useAuthStore } from "@/store/authStore";
import { ApiError } from "@/lib/api/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { useState } from "react";

const statuses = ["ACTIVE", "INACTIVE", "UNDER_REVIEW"] as const;

export function AdminListingsPage() {
  const token = useAuthStore((s) => s.accessToken)!;
  const queryClient = useQueryClient();
  const [message, setMessage] = useState<string | null>(null);

  const listings = useQuery({
    queryKey: ["admin-listings"],
    queryFn: () => fetchAdminListings(token),
  });

  const updateStatus = useMutation({
    mutationFn: ({
      propertyId,
      status,
    }: {
      propertyId: string;
      status: string;
    }) => updateAdminListingStatus(token, propertyId, status),
    onSuccess: () => {
      setMessage("Listing status updated.");
      queryClient.invalidateQueries({ queryKey: ["admin-listings"] });
    },
    onError: (err: unknown) => {
      setMessage(err instanceof ApiError ? err.message : "Update failed.");
    },
  });

  return (
    <div className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
      <Link to="/admin" className="text-sm font-medium text-brand-700 hover:underline">
        ← Admin home
      </Link>
      <h1 className="mt-4 font-display text-3xl font-semibold text-ink">Listings</h1>
      <p className="mt-2 text-sm text-muted">
        Only <strong>ACTIVE</strong> listings appear in guest search and Explore.
      </p>

      {message && (
        <p className="mt-4 rounded-lg bg-stone-100 px-3 py-2 text-sm text-ink" role="status">
          {message}
        </p>
      )}

      {listings.isLoading && <p className="mt-8 text-muted">Loading…</p>}
      {listings.error && (
        <p className="mt-8 text-sm text-red-600">Could not load listings.</p>
      )}

      {listings.data && (
        <div className="mt-8 overflow-x-auto rounded-xl border border-border">
          <table className="min-w-full text-left text-sm">
            <thead className="border-b border-border bg-stone-50 text-muted">
              <tr>
                <th className="px-4 py-3 font-medium">Title</th>
                <th className="px-4 py-3 font-medium">City</th>
                <th className="px-4 py-3 font-medium">Host</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">View</th>
              </tr>
            </thead>
            <tbody>
              {listings.data.map((p) => (
                <tr key={p.id} className="border-b border-border last:border-0">
                  <td className="px-4 py-3 font-medium text-ink">{p.title}</td>
                  <td className="px-4 py-3">{p.city}</td>
                  <td className="px-4 py-3 text-muted">{p.hostId}</td>
                  <td className="px-4 py-3">
                    <select
                      className="rounded-md border border-border bg-white px-2 py-1"
                      value={p.status}
                      disabled={updateStatus.isPending}
                      onChange={(e) =>
                        updateStatus.mutate({
                          propertyId: p.id,
                          status: e.target.value,
                        })
                      }
                    >
                      {statuses.map((s) => (
                        <option key={s} value={s}>{s}</option>
                      ))}
                    </select>
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      to={`/properties/${p.id}`}
                      className="font-medium text-brand-700 hover:underline"
                    >
                      Open
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
