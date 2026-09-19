import { leafletMarkerIcon } from "@/components/maps/leafletMarkerIcon";
import "leaflet/dist/leaflet.css";
import { MapContainer, Marker, TileLayer } from "react-leaflet";

type Props = {
  latitude?: number;
  longitude?: number;
  label: string;
};

export function PropertyLocationMap({ latitude, longitude, label }: Props) {
  if (latitude == null || longitude == null) {
    return null;
  }

  const position: [number, number] = [latitude, longitude];
  const mapsUrl = `https://www.google.com/maps?q=${latitude},${longitude}`;

  return (
    <section className="mt-10">
      <h2 className="font-display text-xl font-semibold">Location</h2>
      <p className="mt-1 text-sm text-muted">{label}</p>
      <div className="mt-4 overflow-hidden rounded-2xl border border-border">
        <MapContainer
          center={position}
          zoom={15}
          className="h-72 w-full z-0"
          scrollWheelZoom={false}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <Marker position={position} icon={leafletMarkerIcon} />
        </MapContainer>
      </div>
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
