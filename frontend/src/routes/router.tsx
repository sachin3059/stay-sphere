import { AppLayout } from "@/components/layout/AppLayout";
import { HomePage } from "@/pages/HomePage";
import { HowItWorksPage } from "@/pages/HowItWorksPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { PlaceholderAuthPage } from "@/pages/PlaceholderAuthPage";
import { createBrowserRouter } from "react-router-dom";

export const router = createBrowserRouter([
  {
    element: <AppLayout />,
    children: [
      { path: "/", element: <HomePage /> },
      { path: "/how-it-works", element: <HowItWorksPage /> },
      { path: "/login", element: <PlaceholderAuthPage mode="login" /> },
      { path: "/register", element: <PlaceholderAuthPage mode="register" /> },
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);
