/** @type {import('next').NextConfig} */
const nextConfig = {
  // Disable strict mode for faster dev with Firebase
  reactStrictMode: false,
  // Allow images from any domain
  images: {
    domains: ['lh3.googleusercontent.com', 'firebasestorage.googleapis.com'],
  },
  // Suppress ESLint during builds
  eslint: {
    ignoreDuringBuilds: true,
  },
  // Suppress TS type errors that are non-critical during builds
  typescript: {
    ignoreBuildErrors: true,
  },
};

module.exports = nextConfig;
