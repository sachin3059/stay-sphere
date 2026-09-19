type Props = {
  latitude?: number;
  longitude?: number;
  label: string;
};

export function PropertyLocationMap({ latitude, longitude, label }: Props) {
  if (latitude == null || longitude == null) {
    return null;
  }

  const mapsUrl = `https://www.google.com/maps?q=${latitude},${longitude}`;
  const staticMapUrl = `https://staticmap.openstreetmap.de/staticmap.php?center=${latitude},${longitude}&zoom=13&size=800x280&markers=${latitude},${longitude},red`;

  return (
    <section className="mt-10">
      <h2 className="font-display text-xl font-semibold">Location</h2>
      <p className="mt-1 text-sm text-muted">{label}</p>
      <a
        href={mapsUrl}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-4 block overflow-hidden rounded-2xl border border-border"
      >
        <img
          src={staticMapUrl}
          alt={`Map near ${label}`}
          className="h-auto w-full bg-stone-100"
          loading="lazy"
        />
      </a>
      <p className="mt-2 text-sm text-brand-700">
        <a
          href={mapsUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="hover:underline"
        >
          Open in Google Maps
        </a>
      </p>
    </section>
  );
}
