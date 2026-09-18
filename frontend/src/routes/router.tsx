import { GuestOnlyRoute } from "@/components/auth/GuestOnlyRoute";
import { HostOnlyRoute } from "@/components/auth/HostOnlyRoute";
import { AppLayout } from "@/components/layout/AppLayout";
import { ExplorePage } from "@/pages/ExplorePage";
import { HomePage } from "@/pages/HomePage";
import { HowItWorksPage } from "@/pages/HowItWorksPage";
import { HostListingsPage } from "@/pages/host/HostListingsPage";
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
        path: "/host/listings",
        element: (
          <HostOnlyRoute>
            <HostListingsPage />
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
