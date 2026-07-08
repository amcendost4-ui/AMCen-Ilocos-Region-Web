/**
 * Configuration file to separate API and XAMPP settings
 * Choose one deployment mode below:
 */

// DEPLOYMENT MODES
const DEPLOYMENT_MODE = 'XAMPP'; // Options: 'API' or 'XAMPP'

// API Configuration (Node.js backend on port 3000)
const API_CONFIG = {
    base: 'http://localhost:3000/api',
    newsEndpoint: 'http://localhost:3000/api/news',
    startupInstructions: '⚠️ Node.js API Server Not Running',
    howToStart: 'Start the Node.js backend server on port 3000. Run: npm start',
};

// XAMPP Configuration (Apache + PHP on localhost)
const XAMPP_CONFIG = {
    base: 'http://localhost/dost-site/api',
    newsEndpoint: 'news.json',
    startupInstructions: '⚠️ Local news file not found',
    howToStart: 'Start Apache from XAMPP Control Panel and open this site through localhost so it can load news.json from the same folder.',
};

// Select active configuration based on deployment mode
const CONFIG = DEPLOYMENT_MODE === 'API' ? API_CONFIG : XAMPP_CONFIG;

// Export for use in HTML
window.APP_CONFIG = {
    API_URL: CONFIG.newsEndpoint,
    STARTUP_INSTRUCTIONS: CONFIG.startupInstructions,
    HOW_TO_START: CONFIG.howToStart,
    MODE: DEPLOYMENT_MODE,
};
