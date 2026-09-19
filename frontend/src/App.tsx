import { useProactiveTokenRefresh } from "@/hooks/useProactiveTokenRefresh";
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

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppProviders />
    </QueryClientProvider>
  );
}
