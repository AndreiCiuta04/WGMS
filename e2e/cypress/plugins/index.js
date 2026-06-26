/// <reference types="cypress" />
// ***********************************************************
// Cypress plugins.
//
// CHANGE vs the original: added a `log` task so specs (e.g. the UC-05 axe audit) can print
// human-readable violation lines to the terminal/CI job output, not just the browser console.
// Everything else is unchanged.
// ***********************************************************

/**
 * @type {Cypress.PluginConfig}
 */
// eslint-disable-next-line no-unused-vars
module.exports = (on, config) => {
  on('before:browser:launch', (browser, launchOptions) => {
    if (browser.family === 'chromium' && browser.name !== 'electron' && Array.isArray(launchOptions.args)) {
      launchOptions.args.push('--no-sandbox');
      launchOptions.args.push('--disable-gpu');
      launchOptions.args.push('--disable-dev-shm-usage');
    }
    return launchOptions;
  });

  on('task', {
    log(message) {
      // eslint-disable-next-line no-console
      console.log(message);
      return null;
    }
  });
};
