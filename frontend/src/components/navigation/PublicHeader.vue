<template>
  <header class="public-header">
    <div class="public-header-inner page-shell">
      <router-link to="/" class="brand">
        <img src="/favicon.ico" alt="Biblo logo" class="brand-logo" />
        <span class="brand-name">Biblo</span>
      </router-link>

      <nav class="public-nav" aria-label="Public navigation">
        <router-link to="/" class="nav-link">Home</router-link>
        <router-link to="/auth/login" class="nav-link">Login</router-link>
        <router-link to="/auth/register" class="nav-link primary-link">Sign Up</router-link>
      </nav>
    </div>
  </header>
</template>

<script>
export default {
  name: 'PublicHeader',
};
</script>

<style scoped>
.public-header {
  position: sticky;
  top: 0;
  z-index: 30;
  border-bottom: 1px solid var(--theme-divider);
  background: color-mix(in srgb, var(--theme-surface-elevated) 88%, transparent);
  backdrop-filter: blur(8px);
}

.public-header-inner {
  min-height: 66px;
  padding: 10px clamp(10px, 2vw, 20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.brand {
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 0;
  object-fit: cover;
  border: 0;
  box-shadow: none;
}

.brand-name {
  color: var(--theme-heading-color);
  font-family: var(--theme-font-heading);
  font-size: 1.1rem;
  font-weight: 700;
}

.public-nav {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.nav-link {
  text-decoration: none;
  color: var(--theme-text-secondary);
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  border-radius: 9px;
  padding: 8px 12px;
  font-weight: 700;
  font-size: 0.9rem;
}

.nav-link:hover {
  filter: brightness(1.03);
}

.nav-link.router-link-exact-active {
  color: var(--theme-text-primary);
  border-color: color-mix(in srgb, var(--theme-accent) 52%, var(--theme-button-secondary-border));
  background: color-mix(in srgb, var(--theme-accent) 15%, var(--theme-button-secondary-bg));
}

.primary-link {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  border-color: transparent;
}

.primary-link.router-link-exact-active {
  filter: brightness(1.02);
}

@media (max-width: 560px) {
  .public-header-inner {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
