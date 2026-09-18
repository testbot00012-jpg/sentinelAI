/**
 * Sentinel AI - Appium Mobile Test Suite Entry Point
 */
const { runMobileAppiumTests, SentinelMobileApp, CONFIG } = require('../appium-tests');

if (require.main === module) {
  runMobileAppiumTests().catch(console.error);
}

module.exports = {
  SentinelMobileApp,
  CONFIG,
  runMobileAppiumTests
};
