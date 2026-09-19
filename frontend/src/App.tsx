import { env } from "@/config/env";
import { useProactiveTokenRefresh } from "@/hooks/useProactiveTokenRefresh";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { RouterProvider } from "react-router-dom";
import { router } from "./routes/router";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function AppProviders() {
  useProactiveTokenRefresh();
  return <RouterProvider router={router} />;
}

function OAuthProviders({ children }: { children: React.ReactNode }) {
  if (env.googleClientId) {
    return (
      <GoogleOAuthProvider clientId={env.googleClientId}>
        {children}
      </GoogleOAuthProvider>
    );
  }
  return children;
}

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <OAuthProviders>
        <AppProviders />
      </OAuthProviders>
    </QueryClientProvider>
  );
}
