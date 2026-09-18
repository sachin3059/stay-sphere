import { Card } from "@/components/ui/Card";

const steps = [
  {
    step: "01",
    title: "Register & become a host",
    detail: "Guests sign up via /api/auth. Hosts call become-host, then create listings and pricing rules.",
  },
  {
    step: "02",
    title: "Book available dates",
    detail: "Booking service checks availability, applies server-side pricing, and holds inventory with Redis locks.",
  },
  {
    step: "03",
    title: "Pay with Stripe",
    detail: "Payment service creates a PaymentIntent; on success, Kafka drives booking confirmation.",
  },
];

export function HowItWorksPage() {
  return (
    <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6 sm:py-16">
      <h1 className="font-display text-3xl font-semibold text-ink">
        How StaySphere works
      </h1>
      <p className="mt-3 max-w-2xl text-muted">
        A portfolio-grade split: React talks only to the API gateway on port
        8080; services stay decoupled behind it.
      </p>
      <ol className="mt-10 space-y-4">
        {steps.map((item) => (
          <li key={item.step}>
            <Card className="flex flex-col gap-4 p-6 sm:flex-row sm:items-start sm:gap-6">
              <span className="font-display text-3xl font-semibold text-brand-200">
                {item.step}
              </span>
              <div>
                <h2 className="font-display text-xl font-semibold text-ink">
                  {item.title}
                </h2>
                <p className="mt-2 text-sm leading-relaxed text-muted">
                  {item.detail}
                </p>
              </div>
            </Card>
          </li>
        ))}
      </ol>
    </div>
  );
}
