import {
  fetchAdminUsers,
  updateAdminUserRole,
  type AdminUser,
} from "@/features/admin/api";
import { useAuthStore } from "@/store/authStore";
import { ApiError } from "@/lib/api/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { useState } from "react";

const roles: AdminUser["role"][] = ["GUEST", "HOST", "ADMIN"];

export function AdminUsersPage() {
  const token = useAuthStore((s) => s.accessToken)!;
  const queryClient = useQueryClient();
  const [message, setMessage] = useState<string | null>(null);

  const users = useQuery({
    queryKey: ["admin-users"],
    queryFn: () => fetchAdminUsers(token),
  });

  const updateRole = useMutation({
    mutationFn: ({
      userId,
      role,
    }: {
      userId: string;
      role: AdminUser["role"];
    }) => updateAdminUserRole(token, userId, role),
    onSuccess: () => {
      setMessage("Role updated. Affected users should sign in again for a new JWT.");
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
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
      <h1 className="mt-4 font-display text-3xl font-semibold text-ink">Users</h1>

      {message && (
        <p className="mt-4 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-900" role="status">
          {message}
        </p>
      )}

      {users.isLoading && <p className="mt-8 text-muted">Loading…</p>}
      {users.error && (
        <p className="mt-8 text-sm text-red-600">Could not load users.</p>
      )}

      {users.data && (
        <div className="mt-8 overflow-x-auto rounded-xl border border-border">
          <table className="min-w-full text-left text-sm">
            <thead className="border-b border-border bg-stone-50 text-muted">
              <tr>
                <th className="px-4 py-3 font-medium">Email</th>
                <th className="px-4 py-3 font-medium">Name</th>
                <th className="px-4 py-3 font-medium">Role</th>
                <th className="px-4 py-3 font-medium">Sign-in</th>
                <th className="px-4 py-3 font-medium">Joined</th>
              </tr>
            </thead>
            <tbody>
              {users.data.map((u) => (
                <tr key={u.id} className="border-b border-border last:border-0">
                  <td className="px-4 py-3">{u.email}</td>
                  <td className="px-4 py-3">{u.fullName}</td>
                  <td className="px-4 py-3">
                    <select
                      className="rounded-md border border-border bg-white px-2 py-1"
                      value={u.role}
                      disabled={updateRole.isPending}
                      onChange={(e) =>
                        updateRole.mutate({
                          userId: u.id,
                          role: e.target.value as AdminUser["role"],
                        })
                      }
                    >
                      {roles.map((r) => (
                        <option key={r} value={r}>{r}</option>
                      ))}
                    </select>
                  </td>
                  <td className="px-4 py-3 text-muted">
                    {u.socialOnly ? "Social" : "Email"}
                  </td>
                  <td className="px-4 py-3 text-muted">
                    {new Date(u.createdAt).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <p className="mt-6 text-xs text-muted">
        Bootstrap the first admin with{" "}
        <code className="text-ink">STAYSPHERE_BOOTSTRAP_ADMIN_EMAIL</code> in repo{" "}
        <code className="text-ink">.env</code> — see <code className="text-ink">docs/ADMIN.md</code>.
      </p>
    </div>
  );
}
