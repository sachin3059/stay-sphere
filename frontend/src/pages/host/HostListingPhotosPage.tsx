import { PropertyPhotoPicker } from "@/components/properties/PropertyPhotoPicker";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import {
  deletePropertyImage,
  fetchPropertyById,
  uploadPropertyImages,
} from "@/features/properties/api";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

export function HostListingPhotosPage() {
  const { id: propertyId } = useParams<{ id: string }>();
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [files, setFiles] = useState<File[]>([]);
  const [error, setError] = useState<string | null>(null);

  const { data: property, isLoading } = useQuery({
    queryKey: ["property", propertyId],
    queryFn: () => fetchPropertyById(propertyId!),
    enabled: Boolean(propertyId),
  });

  const removeImage = useMutation({
    mutationFn: (url: string) =>
      deletePropertyImage(accessToken, propertyId!, url),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["property", propertyId] });
      queryClient.invalidateQueries({ queryKey: ["host-properties"] });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not remove photo.",
      );
    },
  });

  const upload = useMutation({
    mutationFn: () =>
      uploadPropertyImages(accessToken, propertyId!, files),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["property", propertyId] });
      queryClient.invalidateQueries({ queryKey: ["host-properties"] });
      navigate(`/properties/${propertyId}`, { replace: true });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not upload photos.",
      );
    },
  });

  if (!propertyId) return null;

  if (isLoading || !property) {
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-muted">Loading…</div>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-10 sm:px-6">
      <Link
        to="/host/listings"
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← My listings
      </Link>
      <h1 className="mt-4 font-display text-2xl font-semibold">
        Photos for {property.title}
      </h1>

      {property.imageUrls && property.imageUrls.length > 0 && (
        <Card className="mt-6 p-4">
          <p className="text-sm font-medium text-ink">Current photos</p>
          <ul className="mt-3 grid grid-cols-3 gap-2">
            {property.imageUrls.map((src) => (
              <li key={src} className="relative aspect-square overflow-hidden rounded-lg">
                <img src={src} alt="" className="h-full w-full object-cover" />
                <button
                  type="button"
                  className="absolute right-1 top-1 rounded-md bg-black/60 px-2 py-0.5 text-xs text-white hover:bg-black/80 disabled:opacity-50"
                  disabled={removeImage.isPending}
                  onClick={() => removeImage.mutate(src)}
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>
        </Card>
      )}

      <Card className="mt-6 p-6">
        <PropertyPhotoPicker
          files={files}
          onChange={setFiles}
          disabled={upload.isPending}
        />
        {error && (
          <p className="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
            {error}
          </p>
        )}
        <Button
          type="button"
          className="mt-6 w-full"
          disabled={files.length === 0 || upload.isPending}
          onClick={() => {
            setError(null);
            upload.mutate();
          }}
        >
          {upload.isPending ? "Uploading…" : "Upload photos"}
        </Button>
      </Card>
    </div>
  );
}
