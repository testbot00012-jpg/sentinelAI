import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        background: "#050816",
        card: "#0F172A",
        primary: "#00E5FF",
        secondary: "#7C3AED",
        success: "#00FF88",
        warning: "#FFB020",
        danger: "#FF4D4D",
      },
      fontFamily: {
        sans: ["var(--font-sans)", "Inter", "sans-serif"],
      },
      boxShadow: {
        neon: "0 0 15px rgba(0, 229, 255, 0.4)",
        glow: "0 0 25px rgba(124, 58, 237, 0.25)",
      }
    },
  },
  plugins: [],
};
export default config;
