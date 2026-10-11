# Property media — S3 presigned uploads

Listing photos and videos upload **directly to S3** from the browser. **property-service** only issues presigned URLs and saves public URLs in Postgres.

## Flow

1. `POST /api/properties/{id}/images/presign` (HOST, Bearer JWT) — body: `{ "files": [{ "fileName", "contentType", "sizeBytes" }] }`
2. Browser `PUT` each file to `uploadUrl` with header `Content-Type` matching the presign response
3. `POST /api/properties/{id}/images/confirm` — body: `{ "objectKeys": ["properties/..."] }`

## AWS setup (one-time)

1. Create an S3 bucket in **ap-south-1** (or your `AWS_REGION`).
2. IAM user or role for **property-service** with at least:
   - `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject` on `arn:aws:s3:::YOUR_BUCKET/properties/*` (`GetObject` also covers S3 HeadObject)
3. Set in repo-root `.env`:
   - `AWS_S3_BUCKET`, `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
4. **CORS** on the bucket (for browser `PUT`):

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["PUT", "GET", "HEAD"],
    "AllowedOrigins": [
      "http://localhost:5173",
      "http://localhost:3000",
      "https://your-production-domain.com"
    ],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

5. **Public read** for guests viewing listings (pick one):
   - Bucket policy allowing `s3:GetObject` on `properties/*` for anonymous read, **or**
   - CloudFront distribution + set `STAYSPHERE_S3_PUBLIC_BASE_URL=https://dxxxx.cloudfront.net`

## Limits (env)

| Variable | Default |
|----------|---------|
| `STAYSPHERE_S3_MAX_IMAGE_BYTES` | 10 MB |
| `STAYSPHERE_S3_MAX_VIDEO_BYTES` | 500 MB |
| `STAYSPHERE_S3_PRESIGN_EXPIRY_MINUTES` | 15 |

Supported types: JPEG, PNG, WebP, GIF, MP4, WebM, QuickTime (`.mov`).

## Demo seed data

`scripts/demo-seed.sql` may still reference legacy Cloudinary URLs for sample listings. New host uploads use S3 URLs from your bucket.
