const SETTINGS_STORAGE_KEY = 'biblov_app_settings_v1';

const VALID_THEMES = ['futuristic', 'modern', 'dark'];
const VALID_PROFILE_VISIBILITY = ['public', 'connections'];

export const DEFAULT_APP_SETTINGS = {
  version: 1,
  appearance: {
    theme: 'dark',
    compactMode: false,
    reducedMotion: false,
  },
  language: {
    locale: 'en',
  },
  notifications: {
    messageAlerts: true,
    matchAlerts: true,
    emailUpdates: true,
  },
  privacy: {
    profileVisibility: 'public',
    discoveryVisible: true,
    readReceipts: true,
    showOnlineStatus: true,
  },
};

function cloneDefaults() {
  return JSON.parse(JSON.stringify(DEFAULT_APP_SETTINGS));
}

function normalizeBoolean(value, fallbackValue) {
  return typeof value === 'boolean' ? value : fallbackValue;
}

function normalizeTheme(themeValue) {
  return VALID_THEMES.includes(themeValue)
    ? themeValue
    : DEFAULT_APP_SETTINGS.appearance.theme;
}

function normalizeProfileVisibility(value) {
  return VALID_PROFILE_VISIBILITY.includes(value)
    ? value
    : DEFAULT_APP_SETTINGS.privacy.profileVisibility;
}

function canUseBrowserApis() {
  return typeof window !== 'undefined' && typeof document !== 'undefined';
}

function canUseStorage() {
  return canUseBrowserApis() && typeof window.localStorage !== 'undefined';
}

function applyRuntimeAttributes(settings) {
  if (!canUseBrowserApis()) {
    return;
  }

  document.documentElement.dataset.theme = settings.appearance.theme;
  document.documentElement.dataset.compact = settings.appearance.compactMode
    ? 'true'
    : 'false';
  document.documentElement.dataset.reducedMotion = settings.appearance.reducedMotion
    ? 'true'
    : 'false';
}

export function mergeWithDefaults(partialSettings) {
  const merged = cloneDefaults();

  if (!partialSettings || typeof partialSettings !== 'object') {
    return merged;
  }

  const source = partialSettings;
  merged.version = 1;

  merged.appearance.theme = normalizeTheme(source.appearance?.theme);
  merged.appearance.compactMode = normalizeBoolean(
    source.appearance?.compactMode,
    merged.appearance.compactMode
  );
  merged.appearance.reducedMotion = normalizeBoolean(
    source.appearance?.reducedMotion,
    merged.appearance.reducedMotion
  );

  if (typeof source.language?.locale === 'string' && source.language.locale.trim()) {
    merged.language.locale = source.language.locale.trim();
  }

  merged.notifications.messageAlerts = normalizeBoolean(
    source.notifications?.messageAlerts,
    merged.notifications.messageAlerts
  );
  merged.notifications.matchAlerts = normalizeBoolean(
    source.notifications?.matchAlerts,
    merged.notifications.matchAlerts
  );
  merged.notifications.emailUpdates = normalizeBoolean(
    source.notifications?.emailUpdates,
    merged.notifications.emailUpdates
  );

  merged.privacy.profileVisibility = normalizeProfileVisibility(
    source.privacy?.profileVisibility
  );
  merged.privacy.discoveryVisible = normalizeBoolean(
    source.privacy?.discoveryVisible,
    merged.privacy.discoveryVisible
  );
  merged.privacy.readReceipts = normalizeBoolean(
    source.privacy?.readReceipts,
    merged.privacy.readReceipts
  );
  merged.privacy.showOnlineStatus = normalizeBoolean(
    source.privacy?.showOnlineStatus,
    merged.privacy.showOnlineStatus
  );

  return merged;
}

export function loadAppSettings() {
  if (!canUseStorage()) {
    return cloneDefaults();
  }

  try {
    const rawValue = window.localStorage.getItem(SETTINGS_STORAGE_KEY);
    if (!rawValue) {
      return cloneDefaults();
    }

    const parsedValue = JSON.parse(rawValue);
    return mergeWithDefaults(parsedValue);
  } catch (error) {
    window.localStorage.removeItem(SETTINGS_STORAGE_KEY);
    return cloneDefaults();
  }
}

export function applyTheme(themeName) {
  if (!canUseBrowserApis()) {
    return;
  }

  const normalizedTheme = normalizeTheme(themeName);
  document.documentElement.dataset.theme = normalizedTheme;
}

export function saveAppSettings(settings) {
  const mergedSettings = mergeWithDefaults(settings);

  if (canUseStorage()) {
    window.localStorage.setItem(
      SETTINGS_STORAGE_KEY,
      JSON.stringify(mergedSettings)
    );
  }

  applyRuntimeAttributes(mergedSettings);
  return mergedSettings;
}

export function resetAppSettings() {
  const defaults = cloneDefaults();
  return saveAppSettings(defaults);
}
