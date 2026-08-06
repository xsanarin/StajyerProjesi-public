
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { App } from "./App";
import { AuthProvider } from "./auth/AuthContext";
import { TemaProvider } from "./ui/TemaProvider";
import { ToastProvider } from "./ui/ToastProvider";

/**
 * retry: 401/403'te tekrar denemek anlamsiz — token gecersizse ikinci istek de
 * ayni yaniti alir, ustelik interceptor kullaniciyi login'e atmis olur. Sadece
 * gercek ag/sunucu hatalarinda bir kez daha deniyoruz.
 */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (deneme, err) => {
        const durum = (err as { response?: { status?: number } })?.response?.status;
        if (durum === 401 || durum === 403 || durum === 404) {
          return false;
        }
        return deneme < 1;
      },
      refetchOnWindowFocus: false,
      staleTime: 30_000,
    },
  },
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <TemaProvider>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <ToastProvider>
            <BrowserRouter>
              <App />
            </BrowserRouter>
          </ToastProvider>
        </AuthProvider>
      </QueryClientProvider>
    </TemaProvider>
  </StrictMode>,
);