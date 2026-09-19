import {
  fetchOlaAutocomplete,
  hasOlaMapsKey,
  reverseGeocodeOla,
  type MapCoordinates,
  type OlaAutocompleteSuggestion,
} from "@/features/maps/olaMaps";
import { leafletMarkerIcon } from "@/components/maps/leafletMarkerIcon";
import "leaflet/dist/leaflet.css";
import { useCallback, useEffect, useMemo, useState } from "react";
import {
  MapContainer,
  Marker,
  TileLayer,
  useMap,
  useMapEvents,
} from "react-leaflet";

const DEFAULT_CENTER: MapCoordinates = {
  latitude: 18.5362,
  longitude: 73.8938,
};

export type LocationPickerValue = MapCoordinates & {
  addressLine?: string;
  city?: string;
  country?: string;
};

type Props = {
  value: LocationPickerValue;
  onChange: (next: LocationPickerValue) => void;
  disabled?: boolean;
};

function MapRecenter({ center }: { center: [number, number] }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center, map.getZoom());
  }, [center, map]);
  return null;
}

function MapClickSelect({
  onPick,
  disabled,
}: {
  onPick: (coords: MapCoordinates) => void;
  disabled?: boolean;
}) {
  useMapEvents({
    click(e) {
      if (disabled) return;
      onPick({ latitude: e.latlng.lat, longitude: e.latlng.lng });
    },
  });
  return null;
}

export function LocationPicker({ value, onChange, disabled }: Props) {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<OlaAutocompleteSuggestion[]>(
    [],
  );
  const [searchError, setSearchError] = useState<string | null>(null);
  const [searching, setSearching] = useState(false);

  const center = useMemo(
    (): [number, number] => [
      value.latitude ?? DEFAULT_CENTER.latitude,
      value.longitude ?? DEFAULT_CENTER.longitude,
    ],
    [value.latitude, value.longitude],
  );

  const applyCoords = useCallback(
    async (coords: MapCoordinates, addressLine?: string) => {
      const parsed = await reverseGeocodeOla(coords);
      onChange({
        latitude: coords.latitude,
        longitude: coords.longitude,
        addressLine: addressLine ?? parsed.formattedAddress ?? value.addressLine,
        city: parsed.city ?? value.city,
        country: parsed.country ?? value.country,
      });
    },
    [onChange, value.addressLine, value.city, value.country],
  );

  useEffect(() => {
    if (!hasOlaMapsKey() || query.trim().length < 2) {
      setSuggestions([]);
      return;
    }
    const handle = window.setTimeout(async () => {
      setSearching(true);
      setSearchError(null);
      try {
        const items = await fetchOlaAutocomplete(query, {
          latitude: center[0],
          longitude: center[1],
        });
        setSuggestions(items);
      } catch {
        setSearchError("Could not search addresses.");
        setSuggestions([]);
      } finally {
        setSearching(false);
      }
    }, 400);
    return () => window.clearTimeout(handle);
  }, [query, center]);

  if (!hasOlaMapsKey()) {
    return (
      <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-900">
        Set <code className="text-ink">VITE_OLA_MAPS_API_KEY</code> in{" "}
        <code className="text-ink">frontend/.env</code> to pick a location on the
        map.
      </p>
    );
  }

  return (
    <div className="space-y-3">
      <div>
        <label className="mb-1.5 block text-sm font-medium text-stone-700">
          Search location
        </label>
        <input
          type="search"
          className="w-full rounded-xl border border-border px-4 py-2.5 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
          placeholder="Start typing an address or place…"
          value={query}
          disabled={disabled}
          onChange={(e) => setQuery(e.target.value)}
          autoComplete="off"
        />
        {searching && (
          <p className="mt-1 text-xs text-muted">Searching…</p>
        )}
        {searchError && (
          <p className="mt-1 text-xs text-red-600">{searchError}</p>
        )}
        {suggestions.length > 0 && (
          <ul
            className="mt-2 max-h-48 overflow-y-auto rounded-xl border border-border bg-white shadow-sm"
            role="listbox"
          >
            {suggestions.map((s) => (
              <li key={s.id}>
                <button
                  type="button"
                  className="w-full px-3 py-2 text-left text-sm hover:bg-stone-50"
                  onClick={() => {
                    setQuery(s.label);
                    setSuggestions([]);
                    void applyCoords(
                      {
                        latitude: s.latitude,
                        longitude: s.longitude,
                      },
                      s.label,
                    );
                  }}
                >
                  {s.label}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="overflow-hidden rounded-2xl border border-border">
        <MapContainer
          center={center}
          zoom={14}
          className="h-64 w-full z-0"
          scrollWheelZoom={!disabled}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          <MapRecenter center={center} />
          <MapClickSelect
            disabled={disabled}
            onPick={(coords) => void applyCoords(coords)}
          />
          <Marker
            position={center}
            icon={leafletMarkerIcon}
            draggable={!disabled}
            eventHandlers={{
              dragend: (e) => {
                const { lat, lng } = e.target.getLatLng();
                void applyCoords({ latitude: lat, longitude: lng });
              },
            }}
          />
        </MapContainer>
      </div>
      <p className="text-xs text-muted">
        Click the map or drag the pin. Coordinates:{" "}
        {value.latitude.toFixed(5)}, {value.longitude.toFixed(5)}
      </p>
    </div>
  );
}
