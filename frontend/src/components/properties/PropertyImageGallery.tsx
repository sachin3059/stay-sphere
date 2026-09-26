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

  if (images.length === 0) {
    return (
      <div className="flex aspect-[2/1] items-center justify-center rounded-xl bg-stone-100 text-stone-500">
        {propertyTypeLabel ?? "Stay"}
      </div>
    );
  }

  if (images.length === 1) {
    return (
      <div className="overflow-hidden rounded-xl">
        <img
          src={images[0]}
          alt={title}
          className="aspect-[2/1] w-full object-cover"
        />
      </div>
    );
  }

  const main = images[activeIndex] ?? images[0];
  const side = images.filter((_, i) => i !== activeIndex).slice(0, 4);

  return (
    <div className="grid h-[min(55vh,420px)] grid-cols-1 gap-2 overflow-hidden rounded-xl sm:grid-cols-4 sm:grid-rows-2">
      <div className="relative col-span-1 row-span-2 overflow-hidden sm:col-span-2">
        <img
          src={main}
          alt={`${title} — main`}
          className="h-full w-full object-cover"
        />
      </div>
      {side.map((src, index) => {
        const originalIndex = images.indexOf(src);
        return (
          <button
            key={`${src}-${index}`}
            type="button"
            className="relative hidden overflow-hidden sm:block"
            onClick={() => setActiveIndex(originalIndex)}
            aria-label={`Show photo ${originalIndex + 1}`}
          >
            <img
              src={src}
              alt=""
              className="h-full w-full object-cover transition hover:opacity-90"
            />
            {index === side.length - 1 && images.length > 5 && (
              <span className="absolute inset-0 flex items-center justify-center bg-black/40 text-sm font-semibold text-white">
                +{images.length - 5} photos
              </span>
            )}
          </button>
        );
      })}
      <ul className="flex gap-2 overflow-x-auto sm:hidden">
        {images.map((src, index) => (
          <li key={src} className="shrink-0">
            <button
              type="button"
              onClick={() => setActiveIndex(index)}
              className={`block h-16 w-16 overflow-hidden rounded-lg border-2 ${
                index === activeIndex ? "border-ink" : "border-transparent"
              }`}
            >
              <img src={src} alt="" className="h-full w-full object-cover" />
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
