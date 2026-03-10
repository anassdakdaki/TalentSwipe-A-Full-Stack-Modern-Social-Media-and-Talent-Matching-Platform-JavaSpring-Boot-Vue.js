<template>
  <div class="landing-page">
    <PublicHeader />
    <div class="landing-shell page-shell">
      <section class="hero-card">
        <div class="hero-copy">
          <p class="eyebrow">Study. Network. Build.</p>
          <h1>One platform for students, professionals, and communities.</h1>
          <p class="lead">
            Discover communities, connect with like-minded people, and collaborate through profiles, posts, and chat.
          </p>
          <div class="hero-cta">
            <router-link to="/auth/register" class="primary-btn">Get Started</router-link>
            <router-link to="/auth/login" class="secondary-btn">I already have an account</router-link>
          </div>
        </div>

        <div class="hero-panel">
          <div class="panel-card">
            <h3>What you can do</h3>
            <ul>
              <li>Build a professional public profile</li>
              <li>Join focused school and career communities</li>
              <li>Match for study, networking, and collaboration</li>
              <li>Chat instantly after matching</li>
            </ul>
          </div>
        </div>
      </section>

      <section class="feature-grid">
        <article class="feature-card">
          <i class="fas fa-users"></i>
          <h3>Communities</h3>
          <p>Topic-driven spaces for classes, projects, and career growth.</p>
        </article>
        <article class="feature-card">
          <i class="fas fa-user-plus"></i>
          <h3>Connect</h3>
          <p>Discover people and send meaningful connection requests.</p>
        </article>
        <article class="feature-card">
          <i class="fas fa-comments"></i>
          <h3>Real-time Chat</h3>
          <p>Talk directly once both sides match and start collaborating.</p>
        </article>
      </section>
    </div>
  </div>
</template>

<script>
import PublicHeader from '@/components/navigation/PublicHeader.vue';

export default {
  name: 'Home',
  components: {
    PublicHeader,
  },
};
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
  padding: 0 0 40px;
}

.landing-shell {
  padding: 16px clamp(12px, 2vw, 24px) 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 20px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: clamp(18px, 3vw, 34px);
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(280px, 0.75fr);
  gap: 16px;
}

.eyebrow {
  margin: 0 0 10px;
  color: var(--theme-accent);
  font-size: 0.82rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
}

.hero-copy h1 {
  margin: 0;
  font-family: var(--theme-font-heading);
  color: var(--theme-heading-color);
  font-size: clamp(2rem, 3.6vw, 3.2rem);
  line-height: 1.05;
}

.lead {
  margin: 12px 0 0;
  max-width: 700px;
  color: var(--theme-text-secondary);
  font-size: 1.02rem;
  line-height: 1.6;
}

.hero-cta {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.primary-btn,
.secondary-btn {
  text-decoration: none;
  border-radius: 10px;
  padding: 10px 14px;
  font-weight: 700;
  transition: transform 0.2s ease, filter 0.2s ease;
}

.primary-btn {
  border: none;
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.secondary-btn {
  border: 1px solid var(--theme-button-secondary-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
}

.primary-btn:hover,
.secondary-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.hero-panel {
  display: flex;
  align-items: stretch;
}

.panel-card {
  width: 100%;
  border: 1px solid var(--theme-surface-border);
  border-radius: 14px;
  background: var(--theme-surface-1);
  padding: 14px;
}

.panel-card h3 {
  margin: 0 0 10px;
  color: var(--theme-heading-color);
  font-family: var(--theme-font-heading);
}

.panel-card ul {
  margin: 0;
  padding-left: 18px;
  color: var(--theme-text-secondary);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.feature-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 14px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 14px;
}

.feature-card i {
  color: var(--theme-accent);
  font-size: 1.1rem;
}

.feature-card h3 {
  margin: 10px 0 6px;
  color: var(--theme-heading-color);
  font-family: var(--theme-font-heading);
}

.feature-card p {
  margin: 0;
  color: var(--theme-text-secondary);
  line-height: 1.5;
  font-size: 0.94rem;
}

html[data-theme='futuristic'] .hero-copy h1,
html[data-theme='futuristic'] .panel-card h3 {
  text-shadow: var(--theme-heading-glow);
}

@media (max-width: 960px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .landing-page {
    padding-bottom: 26px;
  }

  .hero-copy h1 {
    font-size: clamp(1.7rem, 8vw, 2.2rem);
  }

  .hero-cta {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
