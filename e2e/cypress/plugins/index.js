/// <reference types="cypress" />
// ***********************************************************
// Cypress plugins.
//
// CHANGE vs the original: added a `log` task so specs (e.g. the UC-05 axe audit) can print
// human-readable violation lines to the terminal/CI job output, not just the browser console.
// Also registers a Firefox-family browser Cypress cannot auto-detect (Zen/Firefox fork).
// ***********************************************************

const fs = require('fs');
const { execFileSync } = require('child_process');

// Register a Firefox-family browser that Cypress cannot auto-detect (e.g. Zen, a Firefox fork,
// or a Firefox at a non-standard path). Override the path with FIREFOX_BIN.
function firefoxFamilyBrowser() {
  const candidates = [
    process.env.FIREFOX_BIN,
    '/Applications/Zen.app/Contents/MacOS/zen',
    '/Applications/Firefox.app/Contents/MacOS/firefox'
  ].filter(Boolean);

  const binPath = candidates.find((p) => {
    try {
      return fs.existsSync(p);
    } catch (e) {
      return false;
    }
  });
  if (!binPath) {
    return null;
  }

  let versionOutput = '0.0';
  try {
    versionOutput = execFileSync(binPath, ['--version'], { encoding: 'utf8' }).trim();
  } catch (e) {
    // Fall back to a placeholder version if the binary cannot report one.
  }
  const versionMatch = versionOutput.match(/(\d+\.\d+(\.\d+)?[a-z]?)/i);
  const version = versionMatch ? versionMatch[1] : '0.0';
  const majorVersion = Number.parseInt(version, 10) || 0;

  return {
    name: 'firefox',
    channel: 'stable',
    family: 'firefox',
    displayName: 'Firefox (custom)',
    version,
    majorVersion,
    path: binPath
  };
}

/**
 * @type {Cypress.PluginConfig}
 */
module.exports = (on, config) => {
  on('task', {
    log(message) {
      // eslint-disable-next-line no-console
      console.log(message);
      return null;
    }
  });
  on('before:browser:launch', (browser, launchOptions) => {
    if (browser.family === 'chromium' && browser.name !== 'electron' && Array.isArray(launchOptions.args)) {
      launchOptions.args.push('--no-sandbox');
      launchOptions.args.push('--disable-gpu');
      launchOptions.args.push('--disable-dev-shm-usage');
    }
    return launchOptions;
  });

  const firefox = firefoxFamilyBrowser();
  if (firefox && !(config.browsers || []).some((b) => b.name === 'firefox')) {
    config.browsers = (config.browsers || []).concat(firefox);
  }

  return config;
};
