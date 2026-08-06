import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Backend 8070'te (application.yml server.port). Dev sunucusu 5173'te sabit:
// backend'in CorsConfig'i tam bu origin'e izin veriyor, port kayarsa CORS kirilir.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});