import { useState } from "react";

type Props = {
  title: string;
  imageUrls?: string[];
  propertyTypeLabel?: string;
};

export function PropertyImageGallery({
  title,
  imageUrls,
  propertyTypeLabel,
}: Props) {
  const images = imageUrls?.filter(Boolean) ?? [];
  const [activeIndex, setActiveIndex] = useState(0);
  const active = images[activeIndex];

  if (images.length === 0) {
    return (
      <div className="flex aspect-[16/9] items-center justify-center rounded-2xl border border-border bg-gradient-to-br from-brand-100 to-stone-200 text-brand-800/50">
        {propertyTypeLabel ?? "Stay"}
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="overflow-hidden rounded-2xl border border-border">
        <img
          src={active}
          alt={`${title} — photo ${activeIndex + 1}`}
          className="aspect-[16/9] w-full object-cover"
        />
      </div>
      {images.length > 1 && (
        <ul className="grid grid-cols-4 gap-2 sm:grid-cols-6">
          {images.map((src, index) => (
            <li key={src}>
              <button
                type="button"
                onClick={() => setActiveIndex(index)}
                className={`block w-full overflow-hidden rounded-lg border-2 transition-colors ${
                  index === activeIndex
                    ? "border-brand-600"
                    : "border-transparent opacity-80 hover:opacity-100"
                }`}
                aria-label={`Show photo ${index + 1}`}
                aria-current={index === activeIndex}
              >
                <img
                  src={src}
                  alt=""
                  className="aspect-square w-full object-cover"
                />
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
