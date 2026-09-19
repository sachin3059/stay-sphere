import { env } from "@/config/env";

const OLA_BASE = "https://api.olamaps.io";

export type MapCoordinates = {
  latitude: number;
  longitude: number;
};

export type OlaAutocompleteSuggestion = {
  id: string;
  label: string;
  latitude: number;
  longitude: number;
};

type JsonRecord = Record<string, unknown>;

function asRecord(value: unknown): JsonRecord | null {
  return value && typeof value === "object" ? (value as JsonRecord) : null;
}

function readLatLng(geometry: unknown): MapCoordinates | null {
  const g = asRecord(geometry);
  const loc = g ? asRecord(g.location) : null;
  if (!loc) return null;
  const lat = Number(loc.lat);
  const lng = Number(loc.lng);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null;
  return { latitude: lat, longitude: lng };
}

function mapPrediction(item: JsonRecord, index: number): OlaAutocompleteSuggestion | null {
  const label =
    (typeof item.description === "string" && item.description) ||
    (typeof item.name === "string" && item.name) ||
    "";
  const coords =
    readLatLng(item.geometry) ??
    (() => {
      const lat = Number(item.lat ?? item.latitude);
      const lng = Number(item.lng ?? item.longitude);
      if (Number.isFinite(lat) && Number.isFinite(lng)) {
        return { latitude: lat, longitude: lng };
      }
      return null;
    })();
  if (!label || !coords) return null;
  const id =
    (typeof item.place_id === "string" && item.place_id) ||
    `${index}-${label}`;
  return { id, label, ...coords };
}

export function hasOlaMapsKey(): boolean {
  return Boolean(env.olaMapsApiKey);
}

export async function fetchOlaAutocomplete(
  input: string,
  near?: MapCoordinates,
): Promise<OlaAutocompleteSuggestion[]> {
  const apiKey = env.olaMapsApiKey;
  if (!apiKey || input.trim().length < 2) return [];

  const params = new URLSearchParams({
    api_key: apiKey,
    input: input.trim(),
  });
  if (near) {
    params.set("location", `${near.latitude},${near.longitude}`);
  }

  const res = await fetch(
    `${OLA_BASE}/places/v1/autocomplete?${params.toString()}`,
  );
  if (!res.ok) {
    throw new Error(`Address search failed (${res.status})`);
  }
  const data = (await res.json()) as unknown;
  const root = asRecord(data);
  const list =
    (Array.isArray(root?.predictions) && root.predictions) ||
    (Array.isArray(root?.results) && root.results) ||
    (Array.isArray(data) && data) ||
    [];

  return list
    .map((item, index) => mapPrediction(asRecord(item) ?? {}, index))
    .filter((x): x is OlaAutocompleteSuggestion => x !== null);
}

export type ParsedAddress = {
  formattedAddress?: string;
  city?: string;
  country?: string;
};

export async function reverseGeocodeOla(
  coords: MapCoordinates,
): Promise<ParsedAddress> {
  const apiKey = env.olaMapsApiKey;
  if (!apiKey) return {};

  const params = new URLSearchParams({
    api_key: apiKey,
    latlng: `${coords.latitude},${coords.longitude}`,
  });

  const res = await fetch(
    `${OLA_BASE}/places/v1/reverse-geocode?${params.toString()}`,
  );
  if (!res.ok) {
    return {};
  }
  const data = (await res.json()) as unknown;
  const root = asRecord(data);
  const result = asRecord(root?.result) ?? root;
  const formatted =
    (typeof result?.formatted_address === "string" &&
      result.formatted_address) ||
    (typeof root?.formatted_address === "string" && root.formatted_address) ||
    undefined;

  let city: string | undefined;
  let country: string | undefined;
  const components = result?.address_components;
  if (Array.isArray(components)) {
    for (const c of components) {
      const comp = asRecord(c);
      if (!comp) continue;
      const types = Array.isArray(comp.types) ? comp.types : [];
      const longName =
        typeof comp.long_name === "string" ? comp.long_name : undefined;
      if (!longName) continue;
      if (
        types.includes("locality") ||
        types.includes("administrative_area_level_2")
      ) {
        city = city ?? longName;
      }
      if (types.includes("country")) {
        country = longName;
      }
    }
  }

  return { formattedAddress: formatted, city, country };
}

/** Static map image for property detail (Ola). */
export function olaStaticMapUrl(
  coords: MapCoordinates,
  width = 800,
  height = 280,
): string | null {
  const apiKey = env.olaMapsApiKey;
  if (!apiKey) return null;

  const params = new URLSearchParams({ api_key: apiKey });
  const path = `/tiles/v1/staticmaps/default-light-standard/${coords.longitude}/${coords.latitude}/14/${width}x${height}.png`;
  return `${OLA_BASE}${path}?${params.toString()}`;
}
