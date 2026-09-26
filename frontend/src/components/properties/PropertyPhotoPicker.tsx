import { useEffect, useMemo } from "react";

type Props = {
  files: File[];
  onChange: (files: File[]) => void;
  disabled?: boolean;
};

export function PropertyPhotoPicker({ files, onChange, disabled }: Props) {
  const previews = useMemo(
    () =>
      files.map((file) => ({
        file,
        url: URL.createObjectURL(file),
      })),
    [files],
  );

  useEffect(() => {
    return () => {
      previews.forEach((p) => URL.revokeObjectURL(p.url));
    };
  }, [previews]);

  function handleSelect(e: React.ChangeEvent<HTMLInputElement>) {
    const picked = Array.from(e.target.files ?? []);
    if (picked.length === 0) return;
    onChange([...files, ...picked]);
    e.target.value = "";
  }

  function removeAt(index: number) {
    onChange(files.filter((_, i) => i !== index));
  }

  return (
    <div>
      <label className="mb-1.5 block text-sm font-medium text-stone-700">
        Photos
      </label>
      <p className="mb-3 text-xs text-muted">
        JPG or PNG, up to several images. First photo is the cover on Explore.
      </p>
      <input
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple
        disabled={disabled}
        onChange={handleSelect}
        className="block w-full text-sm text-stone-600 file:mr-4 file:rounded-lg file:border-0 file:bg-brand-50 file:px-4 file:py-2 file:text-sm file:font-medium file:text-brand-800 hover:file:bg-brand-100"
      />
      {previews.length > 0 && (
        <ul className="mt-4 grid grid-cols-3 gap-2 sm:grid-cols-4">
          {previews.map((p, index) => (
            <li key={p.url} className="relative aspect-square overflow-hidden rounded-lg bg-stone-100">
              <img
                src={p.url}
                alt=""
                className="h-full w-full object-cover"
              />
              {!disabled && (
                <button
                  type="button"
                  onClick={() => removeAt(index)}
                  className="absolute right-1 top-1 rounded-md bg-black/60 px-1.5 py-0.5 text-xs text-white hover:bg-black/80"
                >
                  Remove
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
