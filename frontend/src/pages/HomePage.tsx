import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { MapPin, Search, Shield, Sparkles } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const highlights = [
  {
    icon: Search,
    title: "Search that scales",
    text: "Postgres full-text search across cities and listings — no extra search cluster.",
  },
  {
    icon: Shield,
    title: "Book with confidence",
    text: "Redis-backed locks, overlap constraints, and idempotent bookings.",
  },
  {
    icon: Sparkles,
    title: "Stripe checkout",
    text: "Real PaymentIntents wired through the API gateway to confirm your stay.",
  },
];

export function HomePage() {
  const navigate = useNavigate();
  const [city, setCity] = useState("");
  const [guests, setGuests] = useState("2");

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    const qs = new URLSearchParams();
    if (city.trim()) qs.set("city", city.trim());
    if (guests) qs.set("guests", guests);
    const q = qs.toString();
    navigate(q ? `/explore?${q}` : "/explore");
  }

  return (
    <div>
      <section className="relative overflow-hidden border-b border-border bg-gradient-to-b from-brand-50/80 to-surface">
        <div
          className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_80%_60%_at_50%_-20%,rgba(20,184,166,0.15),transparent)]"
          aria-hidden
        />
        <div className="relative mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-24">
          <p className="mb-4 inline-flex items-center gap-2 rounded-full border border-brand-200 bg-white/80 px-3 py-1 text-xs font-medium text-brand-800">
            <MapPin className="h-3.5 w-3.5" />
            Full-stack marketplace demo
          </p>
          <h1 className="max-w-2xl font-display text-4xl font-semibold leading-tight tracking-tight text-ink sm:text-5xl">
            Find your next stay,
            <span className="text-brand-700"> booked end to end.</span>
          </h1>
          <p className="mt-4 max-w-xl text-base leading-relaxed text-muted sm:text-lg">
            StaySphere connects guests and hosts through a Spring microservices
            backend — with JWT auth, Kafka events, and Stripe payments behind a
            single gateway.
          </p>

          <Card className="mt-10 max-w-3xl p-4 sm:p-5">
            <p className="mb-3 text-xs font-medium uppercase tracking-wider text-muted">
              Search stays
            </p>
            <form
              onSubmit={handleSearch}
              className="flex flex-col gap-3 sm:flex-row sm:items-end"
            >
              <div className="flex-1">
                <Input
                  label="Where"
                  placeholder="City, e.g. Pune"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                />
              </div>
              <div className="flex-1">
                <Input
                  label="Guests"
                  type="number"
                  min={1}
                  placeholder="2"
                  value={guests}
                  onChange={(e) => setGuests(e.target.value)}
                />
              </div>
              <Button type="submit" className="w-full sm:w-auto">
                <Search className="h-4 w-4" />
                Search
              </Button>
            </form>
          </Card>

          <div className="mt-8 flex flex-wrap gap-3">
            <Link to="/register">
              <Button size="lg">Create account</Button>
            </Link>
            <Link to="/how-it-works">
              <Button variant="outline" size="lg">See the architecture</Button>
            </Link>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6">
        <h2 className="font-display text-2xl font-semibold text-ink">
          Built to show real engineering
        </h2>
        <p className="mt-2 max-w-2xl text-muted">
          This UI is being added step by step. Under the hood you already have
          Flyway migrations, transactional outbox, and a verified Stripe E2E
          script.
        </p>
        <ul className="mt-10 grid gap-6 sm:grid-cols-3">
          {highlights.map(({ icon: Icon, title, text }) => (
            <li key={title}>
              <Card className="h-full p-6">
                <span
                  className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700"
                  aria-hidden
                >
                  <Icon className="h-5 w-5" />
                </span>
                <h3 className="mt-4 font-display text-lg font-semibold">
                  {title}
                </h3>
                <p className="mt-2 text-sm leading-relaxed text-muted">
                  {text}
                </p>
              </Card>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
