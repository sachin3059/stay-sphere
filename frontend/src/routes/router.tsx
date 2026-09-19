import { AuthRequiredRoute } from "@/components/auth/AuthRequiredRoute";
import { GuestOnlyRoute } from "@/components/auth/GuestOnlyRoute";
import { HostOnlyRoute } from "@/components/auth/HostOnlyRoute";
import { AppLayout } from "@/components/layout/AppLayout";
import { BookPropertyPage } from "@/pages/BookPropertyPage";
import { BookingCompletePage } from "@/pages/BookingCompletePage";
import { BookingPayPage } from "@/pages/BookingPayPage";
import { ExplorePage } from "@/pages/ExplorePage";
import { MyBookingsPage } from "@/pages/MyBookingsPage";
import { HomePage } from "@/pages/HomePage";
import { HowItWorksPage } from "@/pages/HowItWorksPage";
import { HostListingAvailabilityPage } from "@/pages/host/HostListingAvailabilityPage";
import { HostListingPhotosPage } from "@/pages/host/HostListingPhotosPage";
import { HostListingsPage } from "@/pages/host/HostListingsPage";
import { HostReservationsPage } from "@/pages/host/HostReservationsPage";
import { NewListingPage } from "@/pages/host/NewListingPage";
import { PropertyDetailPage } from "@/pages/PropertyDetailPage";
import { LoginPage } from "@/pages/LoginPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { RegisterPage } from "@/pages/RegisterPage";
import { createBrowserRouter } from "react-router-dom";

export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [
      { path: "/", element: <HomePage /> },
      { path: "/explore", element: <ExplorePage /> },
      { path: "/properties/:id", element: <PropertyDetailPage /> },
      {
        path: "/properties/:id/book",
        element: (
          <AuthRequiredRoute>
            <BookPropertyPage />
          </AuthRequiredRoute>
        ),
      },
      {
        path: "/bookings/my",
        element: (
          <AuthRequiredRoute>
            <MyBookingsPage />
          </AuthRequiredRoute>
        ),
      },
      {
        path: "/bookings/:bookingId/pay",
        element: (
          <AuthRequiredRoute>
            <BookingPayPage />
          </AuthRequiredRoute>
        ),
      },
      { path: "/bookings/complete", element: <BookingCompletePage /> },
      {
        path: "/host/listings",
        element: (
          <HostOnlyRoute>
            <HostListingsPage />
          </HostOnlyRoute>
        ),
      },
      {
        path: "/host/reservations",
        element: (
          <HostOnlyRoute>
            <HostReservationsPage />
          </HostOnlyRoute>
        ),
      },
      {
        path: "/host/listings/new",
        element: (
          <HostOnlyRoute>
            <NewListingPage />
          </HostOnlyRoute>
        ),
      },
      {
        path: "/host/listings/:id/photos",
        element: (
          <HostOnlyRoute>
            <HostListingPhotosPage />
          </HostOnlyRoute>
        ),
      },
      {
        path: "/host/listings/:id/availability",
        element: (
          <HostOnlyRoute>
            <HostListingAvailabilityPage />
          </HostOnlyRoute>
        ),
      },
      { path: "/how-it-works", element: <HowItWorksPage /> },
      {
        path: "/login",
        element: (
          <GuestOnlyRoute>
            <LoginPage />
          </GuestOnlyRoute>
        ),
      },
      {
        path: "/register",
        element: (
          <GuestOnlyRoute>
            <RegisterPage />
          </GuestOnlyRoute>
        ),
      },
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);
