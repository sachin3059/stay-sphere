# Maps & listing location (Ola Maps)

Hosts pick a real **latitude / longitude** when creating or editing a listing. Guests see a map on the property detail page.

## Setup

1. Create **Ola Maps** credentials on [Krutrim Cloud](https://cloud.olakrutrim.com) (not Models → API keys).
2. **Allowed domains:** `localhost`, `localhost:5173`, `127.0.0.1`, plus your production domain.
3. In `frontend/.env`:

   ```env
   VITE_OLA_MAPS_API_KEY=your_api_key
   ```

4. Restart Vite (`npm run dev`).

For Docker production build, set `VITE_OLA_MAPS_API_KEY` in repo `.env` (compose passes it as a build arg).

## Behaviour

| Screen | What happens |
|--------|----------------|
| **New / edit listing** | Search (Ola Autocomplete), click map, drag pin → reverse geocode updates address fields when possible |
| **Property detail** | Interactive **Leaflet** map (OpenStreetMap tiles) centered on saved coordinates |

Interactive map tiles use **OpenStreetMap** (no extra key). Search and static preview use **Ola** APIs:

- `GET https://api.olamaps.io/places/v1/autocomplete`
- `GET https://api.olamaps.io/places/v1/reverse-geocode`
- Static map center endpoint (see `olaStaticMapUrl` in frontend)

## Troubleshooting

- **Search fails (403/401):** Check API key, allowed domains, and that credentials are **Ola Maps** credentials.
- **No map on form:** `VITE_OLA_MAPS_API_KEY` missing or dev server not restarted.
- **CORS errors:** Domain must be whitelisted in Ola console.
