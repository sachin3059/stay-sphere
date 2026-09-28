#!/usr/bin/env python3
"""Generate and optionally apply StaySphere demo-seed.sql (schema-correct)."""
from __future__ import annotations

import pathlib
import subprocess
import sys
import uuid

HOST_ID = "32c73785-933e-4309-9bc4-e491b3376838"
CLOUD = "ddup7omgb"

ROOT = pathlib.Path(__file__).resolve().parent

# (id, slug, title, description, city, address, lat, lon, price, guests, beds, baths, type, days_ago)
PROPERTIES = [
    (
        "3f2a9c14-7b8e-4d21-9a05-1c6e0d3f4a01",
        "goa-villa-01",
        "Sunlit Beachfront Villa with Private Pool in North Goa",
        "Wake up to ocean breezes at this sunlit beachfront villa in North Goa. Featuring a private swimming pool, a shaded BBQ deck, and four spacious en-suite bedrooms, it is built for families and friends who want the sand just steps away. Anjuna''s cafes, flea markets, and sunset points are a short stroll from the gate.",
        "Goa",
        "House 12, Anjuna Beach Road, Anjuna, North Goa 403509",
        15.5735,
        73.7395,
        9000,
        8,
        4,
        4,
        "VILLA",
        2,
    ),
    (
        "8b1d6e27-2f4a-4c93-b6d1-0a2e7f5c1b02",
        "goa-studio-02",
        "Cozy Beach Studio Steps from Calangute Sands",
        "A bright, breezy studio just a two-minute walk from Calangute beach. Compact and thoughtfully furnished with a kitchenette, fast WiFi, and a cozy queen bed, it is perfect for couples and solo travellers who want an affordable base near the sand, shacks, and nightlife.",
        "Goa",
        "Flat 4, Naika Vaddo, Calangute, North Goa 403516",
        15.5439,
        73.7553,
        3200,
        2,
        1,
        1,
        "STUDIO",
        28,
    ),
    (
        "c4e7a913-5d8b-42f6-8e0a-3b9c1d6f2a03",
        "mumbai-apt-03",
        "Sea-View Apartment in Bandra with City Skyline",
        "Enjoy sweeping sea and skyline views from this stylish two-bedroom apartment in Bandra West. Floor-to-ceiling windows, a dedicated workspace, and a breezy balcony make it ideal for business trips or a weekend in the city. Cafes, promenades, and the Bandstand are minutes away.",
        "Mumbai",
        "1402 Sea Crest, Carter Road, Bandra West, Mumbai 400050",
        19.0596,
        72.8236,
        6500,
        4,
        2,
        2,
        "APARTMENT",
        15,
    ),
    (
        "1a9f3c58-6e2d-4b74-9c31-5f8a0e7d3b04",
        "pune-apt-04",
        "Modern 2BHK Near Hinjewadi IT Park",
        "A modern, move-in-ready 2BHK minutes from the Hinjewadi IT parks. Comes with reliable power backup, a full kitchen, and a quiet work-friendly setup, making it a smart pick for relocating professionals and project teams. Malls, gyms, and eateries are close by.",
        "Pune",
        "B-703, Blue Ridge Township, Hinjewadi Phase 1, Pune 411057",
        18.5913,
        73.7389,
        2800,
        4,
        2,
        2,
        "APARTMENT",
        7,
    ),
    (
        "7d2b8e41-3a9c-4f16-8b52-6c1e9a0f4d05",
        "blr-house-05",
        "Garden House Retreat in Whitefield, Bengaluru",
        "A peaceful three-bedroom house wrapped in a leafy private garden in Whitefield. Pet-friendly and family-sized, with covered parking, a laundry, and plenty of natural light, it offers a calm retreat while staying close to tech parks, restaurants, and metro links.",
        "Bengaluru",
        "27 Palm Meadows, Whitefield, Bengaluru 560066",
        12.9698,
        77.75,
        5200,
        6,
        3,
        3,
        "HOUSE",
        21,
    ),
    (
        "9e5c1a73-8b4d-42a9-b7e6-0f3c2d1a6b06",
        "manali-cabin-06",
        "Pinewood Mountain Cabin in Old Manali",
        "A snug pinewood cabin tucked into the slopes of Old Manali. Warm up by the fireplace after a day on the trails, cook a hot meal in the full kitchen, and soak in uninterrupted mountain views from the deck. Cafes and the river are a short walk downhill.",
        "Manali",
        "Cottage 3, Old Manali Road, Manali 175131",
        32.257,
        77.181,
        4200,
        4,
        2,
        2,
        "CABIN",
        30,
    ),
    (
        "2c8a4f19-7d3b-4e56-9a1c-8b0e5f2d3c07",
        "jaipur-house-07",
        "Royal Heritage Haveli Stay in Pink City Jaipur",
        "Live like royalty in this restored heritage haveli in the heart of the Pink City. Hand-painted arches, a central courtyard, and three comfortable bedrooms blend old-world charm with modern comforts. The City Palace, Hawa Mahal, and bustling bazaars are within easy reach.",
        "Jaipur",
        "Haveli No. 7, Gangori Bazaar, Pink City, Jaipur 302002",
        26.9258,
        75.8237,
        5800,
        6,
        3,
        3,
        "HOUSE",
        11,
    ),
    (
        "6f1e9b34-2a7c-48d5-b3e9-1c4a0d6f5e08",
        "udaipur-villa-08",
        "Lakeside Villa Overlooking Pichola, Udaipur",
        "A serene lakeside villa with postcard views of Lake Pichola and the Aravalli hills. Lounge by the private pool, dine on the terrace at golden hour, and explore the City of Lakes just minutes away. Three elegant bedrooms make it perfect for families or a special getaway.",
        "Udaipur",
        "Villa 9, Lake Pichola Road, Hanuman Ghat, Udaipur 313001",
        24.576,
        73.68,
        8500,
        6,
        3,
        3,
        "VILLA",
        4,
    ),
    (
        "4b7d2c86-9e1a-43f2-8c67-5a0b3e9d1c09",
        "rishikesh-cottage-09",
        "Riverside Cottage by the Ganges in Rishikesh",
        "A tranquil riverside cottage set steps from the Ganges near Laxman Jhula. Fall asleep to the sound of flowing water, sip morning chai on the deck, and reach yoga studios, cafes, and rafting points on foot. A restful base for a spiritual or adventure escape.",
        "Rishikesh",
        "Riverside Cottage 5, Laxman Jhula Road, Rishikesh 249302",
        30.1287,
        78.32,
        3800,
        3,
        1,
        1,
        "COTTAGE",
        18,
    ),
    (
        "a83c5e17-4b9d-4126-9f8a-2e1c6d0b3a10",
        "coorg-estate-10",
        "Coffee Estate Bungalow Amid Coorg Hills",
        "Stay amid rolling coffee plantations in this cosy estate bungalow in the Coorg hills. Misty mornings, a crackling fireplace, and long garden walks await, along with a full kitchen and covered parking. Waterfalls, spice trails, and viewpoints are a short drive away.",
        "Coorg",
        "Estate Bungalow, Galibeedu Road, Madikeri, Coorg 571201",
        12.4009,
        75.705,
        6200,
        6,
        3,
        3,
        "HOUSE",
        25,
    ),
    (
        "5d9b3a62-1c8e-4f47-8a2d-7b0e4c1f9d11",
        "delhi-studio-11",
        "Executive Business Studio in Cyber City Gurugram",
        "A sleek executive studio in the heart of Cyber City, Gurugram. Designed for the business traveller with a fast WiFi work desk, power backup, and a compact kitchen, it puts offices, metro, and dining hubs right at your doorstep. Check in and get straight to work.",
        "Gurugram",
        "Studio 1108, DLF Cyber City, Phase 2, Gurugram 122002",
        28.4949,
        77.088,
        3500,
        2,
        1,
        1,
        "STUDIO",
        9,
    ),
    (
        "e17c4a95-3b6d-42e8-9c14-0a5f2b8d6c12",
        "kochi-apt-12",
        "Backwaters-View Apartment on Kochi Marine Drive",
        "A calm two-bedroom apartment overlooking the Kochi backwaters along Marine Drive. Watch ferries drift by from the balcony, cook local catch in the full kitchen, and explore Fort Kochi, cafes, and the promenade nearby. A comfortable base for exploring coastal Kerala.",
        "Kochi",
        "6C Marine Drive Residency, Ernakulam, Kochi 682031",
        9.976,
        76.276,
        3600,
        4,
        2,
        2,
        "APARTMENT",
        14,
    ),
]

AMENITIES: dict[str, list[str]] = {
    "3f2a9c14-7b8e-4d21-9a05-1c6e0d3f4a01": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "Pool",
        "TV",
        "Washer",
        "BBQ Grill",
        "Beach Access",
        "Balcony",
    ],
    "8b1d6e27-2f4a-4c93-b6d1-0a2e7f5c1b02": [
        "WiFi",
        "Kitchen",
        "AC",
        "TV",
        "Refrigerator",
        "Beach Access",
    ],
    "c4e7a913-5d8b-42f6-8e0a-3b9c1d6f2a03": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "TV",
        "Washer",
        "Workspace",
        "Balcony",
    ],
    "1a9f3c58-6e2d-4b74-9c31-5f8a0e7d3b04": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "TV",
        "Workspace",
        "Power Backup",
    ],
    "7d2b8e41-3a9c-4f16-8b52-6c1e9a0f4d05": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "TV",
        "Washer",
        "Garden",
        "Workspace",
        "Pet Friendly",
    ],
    "9e5c1a73-8b4d-42a9-b7e6-0f3c2d1a6b06": [
        "WiFi",
        "Kitchen",
        "Heater",
        "Parking",
        "TV",
        "Fireplace",
        "Mountain View",
        "Hot Water",
    ],
    "2c8a4f19-7d3b-4e56-9a1c-8b0e5f2d3c07": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "TV",
        "Washer",
        "Garden",
        "Power Backup",
    ],
    "6f1e9b34-2a7c-48d5-b3e9-1c4a0d6f5e08": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "Pool",
        "TV",
        "Washer",
        "Lake View",
        "Balcony",
    ],
    "4b7d2c86-9e1a-43f2-8c67-5a0b3e9d1c09": [
        "WiFi",
        "Kitchen",
        "Hot Water",
        "Parking",
        "River View",
        "TV",
    ],
    "a83c5e17-4b9d-4126-9f8a-2e1c6d0b3a10": [
        "WiFi",
        "Kitchen",
        "Parking",
        "TV",
        "Fireplace",
        "Garden",
        "Mountain View",
        "Coffee Maker",
        "Power Backup",
    ],
    "5d9b3a62-1c8e-4f47-8a2d-7b0e4c1f9d11": [
        "WiFi",
        "Kitchen",
        "AC",
        "TV",
        "Workspace",
        "Power Backup",
        "Coffee Maker",
    ],
    "e17c4a95-3b6d-42e8-9c14-0a5f2b8d6c12": [
        "WiFi",
        "Kitchen",
        "AC",
        "Parking",
        "TV",
        "Washer",
        "Balcony",
    ],
}

PRICING = [
    (0, 2, 1.20, "DATE '2026-12-20'", "DATE '2027-01-05'", 1.50),
    (1, 1, 1.20, "NULL", "NULL", "NULL"),
    (2, 1, 1.20, "NULL", "NULL", "NULL"),
    (3, 1, 1.00, "NULL", "NULL", "NULL"),
    (4, 2, 1.00, "NULL", "NULL", "NULL"),
    (5, 2, 1.00, "DATE '2026-12-15'", "DATE '2027-01-10'", 1.50),
    (6, 1, 1.20, "NULL", "NULL", "NULL"),
    (7, 2, 1.20, "DATE '2026-10-15'", "DATE '2026-11-15'", 1.50),
    (8, 1, 1.00, "NULL", "NULL", "NULL"),
    (9, 2, 1.20, "NULL", "NULL", "NULL"),
    (10, 1, 1.00, "NULL", "NULL", "NULL"),
    (11, 1, 1.00, "NULL", "NULL", "NULL"),
]


def image_url(slug: str, n: int) -> str:
    return (
        f"https://res.cloudinary.com/{CLOUD}/image/upload/"
        f"w_1200,q_auto,f_auto/staysphere/seed/{slug}/photo{n}.jpg"
    )


def build_sql() -> str:
    ids = [p[0] for p in PROPERTIES]
    id_list = ", ".join(f"'{i}'" for i in ids)
    lines = [
        "-- StaySphere demo seed (generated)",
        "BEGIN;",
        f"DELETE FROM property_images WHERE property_id IN ({id_list});",
        f"DELETE FROM property_amenities WHERE property_id IN ({id_list});",
        f"DELETE FROM pricing_rules WHERE property_id IN ({id_list});",
        f"DELETE FROM properties WHERE id IN ({id_list});",
        "",
        "INSERT INTO properties",
        "  (id, host_id, title, description, city, country, address,",
        "   latitude, longitude, price_per_night, max_guests, bedrooms, bathrooms,",
        "   property_type, status, created_at)",
        "VALUES",
    ]
    prop_vals = []
    for row in PROPERTIES:
        (
            pid,
            _slug,
            title,
            desc,
            city,
            address,
            lat,
            lon,
            price,
            guests,
            beds,
            baths,
            ptype,
            days_ago,
        ) = row
        prop_vals.append(
            f"  ('{pid}', '{HOST_ID}', '{title}', '{desc}', '{city}', 'India', "
            f"'{address}', {lat}, {lon}, {price}, {guests}, {beds}, {baths}, "
            f"'{ptype}', 'ACTIVE', NOW() - INTERVAL '{days_ago} days')"
        )
    lines.append(",\n".join(prop_vals) + ";")
    lines.append("")
    lines.append("INSERT INTO property_amenities (property_id, amenity) VALUES")
    am_lines = []
    for pid, items in AMENITIES.items():
        for a in items:
            am_lines.append(f"  ('{pid}', '{a}')")
    lines.append(",\n".join(am_lines) + ";")
    lines.append("")
    lines.append(
        "INSERT INTO pricing_rules"
        "\n  (id, property_id, base_price, minimum_stay, weekend_multiplier,"
        "\n   peak_season_start, peak_season_end, peak_season_multiplier,"
        "\n   long_stay_discount, long_stay_threshold_nights, status)"
        "\nVALUES"
    )
    pr_lines = []
    for pidx, mn, wk, ps, pe, pm in PRICING:
        pid = PROPERTIES[pidx][0]
        price = PROPERTIES[pidx][8]
        peak_mult = pm if pm != "NULL" else "1.00"
        pr_lines.append(
            f"  ('{uuid.uuid4()}', '{pid}', {price}, {mn}, {wk}, {ps}, {pe}, "
            f"{peak_mult}, 1.00, 7, 'ACTIVE')"
        )
    lines.append(",\n".join(pr_lines) + ";")
    lines.append("")
    lines.append("INSERT INTO property_images (property_id, image_url) VALUES")
    img_lines = []
    for row in PROPERTIES:
        pid, slug = row[0], row[1]
        for n in range(1, 6):
            img_lines.append(f"  ('{pid}', '{image_url(slug, n)}')")
    lines.append(",\n".join(img_lines) + ";")
    lines.append("COMMIT;")
    return "\n".join(lines) + "\n"


def main() -> None:
    sql = build_sql()
    out = ROOT / "demo-seed.sql"
    out.write_text(sql, encoding="utf-8")
    print(f"Wrote {out}")

    if "--apply" in sys.argv:
        subprocess.run(
            [
                "docker",
                "exec",
                "-i",
                "staysphere-postgres",
                "psql",
                "-U",
                "staysphere",
                "-d",
                "staysphere",
            ],
            input=sql.encode("utf-8"),
            check=True,
        )
        print("Applied demo-seed.sql to Postgres.")


if __name__ == "__main__":
    main()
