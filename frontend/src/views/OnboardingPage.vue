<template>
  <div class="onboarding-page">
    <div class="onboarding-shell glass-card">
      <header class="onboarding-header">
        <div>
          <h1>Welcome to Biblo</h1>
          <p>Set your interests, community preferences, and content focus in under two minutes.</p>
        </div>
        <div class="progress-meta">Step {{ step }} of {{ totalSteps }}</div>
      </header>

      <div class="progress-track" role="progressbar" :aria-valuenow="progressPercent" aria-valuemin="0" aria-valuemax="100">
        <div class="progress-fill" :style="{ width: `${progressPercent}%` }"></div>
      </div>

      <ol class="step-tabs">
        <li v-for="(label, index) in stepLabels" :key="label" :class="{ active: step === index + 1, done: step > index + 1 }">
          {{ label }}
        </li>
      </ol>

      <div v-if="loading" class="state-message">Loading onboarding options...</div>
      <div v-else>
        <p v-if="error" class="state-error">{{ error }}</p>

        <section v-if="step === 1" class="step-card">
          <h2>What are you interested in?</h2>
          <p>Pick topics that match what you want to learn, build, or discuss.</p>

          <div class="input-row">
            <input
              v-model="interestInput"
              type="text"
              placeholder="Add an interest and press Enter"
              @keydown.enter.prevent="addInterestFromInput"
            />
            <button class="secondary-btn" @click="addInterestFromInput">Add</button>
          </div>

          <div class="chip-group">
            <button
              v-for="interest in form.interests"
              :key="`selected-${interest}`"
              class="chip chip-selected"
              @click="removeInterest(interest)"
            >
              {{ interest }} <span aria-hidden="true">×</span>
            </button>
            <p v-if="!form.interests.length" class="hint">No interests selected yet.</p>
          </div>

          <h3>Suggestions</h3>
          <div class="chip-group">
            <button
              v-for="interest in interestSuggestions"
              :key="`suggestion-${interest}`"
              class="chip"
              :class="{ active: form.interests.includes(interest) }"
              @click="toggleInterest(interest)"
            >
              {{ interest }}
            </button>
          </div>
        </section>

        <section v-if="step === 2" class="step-card">
          <h2>What are you here for?</h2>
          <p>Choose connection goals and content types to shape your feed.</p>

          <div class="option-grid">
            <label class="toggle-item">
              <input v-model="form.lookingFor.studyPartner" type="checkbox" />
              <span>Study Partner</span>
            </label>
            <label class="toggle-item">
              <input v-model="form.lookingFor.languageExchange" type="checkbox" />
              <span>Language Exchange</span>
            </label>
            <label class="toggle-item">
              <input v-model="form.lookingFor.friendship" type="checkbox" />
              <span>Friendship</span>
            </label>
            <label class="toggle-item">
              <input v-model="form.lookingFor.networking" type="checkbox" />
              <span>Networking</span>
            </label>
            <label class="toggle-item">
              <input v-model="form.lookingFor.community" type="checkbox" />
              <span>Community</span>
            </label>
            <label class="toggle-item">
              <input v-model="form.openToCollaborate" type="checkbox" />
              <span>Open to Collaborate</span>
            </label>
          </div>

          <h3>Preferred Content</h3>
          <div class="content-grid">
            <label v-for="option in contentOptions" :key="option.key" class="content-option">
              <input v-model="form.contentPreferences[option.key]" type="checkbox" />
              <div>
                <strong>{{ option.label }}</strong>
                <p>{{ option.description }}</p>
              </div>
            </label>
          </div>
        </section>

        <section v-if="step === 3" class="step-card">
          <h2>Pick your communities</h2>
          <p>Select communities to join now. You can always add more later.</p>

          <div class="input-row">
            <input
              v-model="tagInput"
              type="text"
              placeholder="Add preferred community tags"
              @keydown.enter.prevent="addPreferredTagFromInput"
            />
            <button class="secondary-btn" @click="addPreferredTagFromInput">Add Tag</button>
          </div>

          <div class="chip-group">
            <button
              v-for="tag in form.preferredCommunityTags"
              :key="`selected-tag-${tag}`"
              class="chip chip-selected"
              @click="removePreferredTag(tag)"
            >
              #{{ tag }} <span aria-hidden="true">×</span>
            </button>
          </div>

          <div class="chip-group suggestions">
            <button
              v-for="tag in communityTagSuggestions"
              :key="`tag-suggestion-${tag}`"
              class="chip"
              :class="{ active: form.preferredCommunityTags.includes(tag) }"
              @click="togglePreferredTag(tag)"
            >
              #{{ tag }}
            </button>
          </div>

          <div class="input-row">
            <input v-model="communitySearch" type="text" placeholder="Search communities by name, description, or tags" />
          </div>

          <div class="community-grid">
            <article v-for="community in filteredCommunities" :key="community.id" class="community-card">
              <div class="community-top">
                <h4>{{ community.name }}</h4>
                <span v-if="community.recommended" class="badge">Recommended</span>
              </div>
              <p class="community-desc">{{ truncate(community.description, 120) }}</p>
              <p class="community-meta">{{ community.membersCount }} members</p>
              <p v-if="community.recommendationReason" class="community-reason">{{ community.recommendationReason }}</p>
              <div class="chip-group compact">
                <span v-for="tag in community.tags.slice(0, 4)" :key="`${community.id}-${tag}`" class="chip">#{{ tag }}</span>
              </div>
              <button
                class="select-btn"
                :class="{ selected: form.selectedCommunityIds.includes(community.id) }"
                @click="toggleCommunitySelection(community.id)"
              >
                {{ form.selectedCommunityIds.includes(community.id) ? 'Selected' : 'Select Community' }}
              </button>
            </article>
          </div>
        </section>

        <section v-if="step === 4" class="step-card">
          <h2>Review and finish</h2>
          <p>Your choices will personalize matches, communities, and recommendations.</p>

          <div class="review-grid">
            <div class="review-card">
              <h3>Interests</h3>
              <div class="chip-group compact">
                <span v-for="interest in form.interests" :key="`review-${interest}`" class="chip">#{{ interest }}</span>
              </div>
            </div>
            <div class="review-card">
              <h3>Goals</h3>
              <ul>
                <li v-if="form.lookingFor.studyPartner">Study Partner</li>
                <li v-if="form.lookingFor.languageExchange">Language Exchange</li>
                <li v-if="form.lookingFor.friendship">Friendship</li>
                <li v-if="form.lookingFor.networking">Networking</li>
                <li v-if="form.lookingFor.community">Community</li>
                <li v-if="form.openToCollaborate">Open to Collaborate</li>
              </ul>
            </div>
            <div class="review-card">
              <h3>Communities</h3>
              <p>{{ form.selectedCommunityIds.length }} communities selected</p>
              <div class="chip-group compact">
                <span v-for="community in selectedCommunities" :key="`selected-community-${community.id}`" class="chip">{{ community.name }}</span>
              </div>
            </div>
            <div class="review-card">
              <h3>Content</h3>
              <ul>
                <li v-for="option in enabledContentLabels" :key="`content-${option}`">{{ option }}</li>
              </ul>
            </div>
          </div>
        </section>

        <div class="footer-actions">
          <button class="secondary-btn" :disabled="step === 1 || saving" @click="previousStep">Back</button>
          <button
            v-if="step < totalSteps"
            class="primary-btn"
            :disabled="saving"
            @click="nextStep"
          >
            Continue
          </button>
          <button
            v-else
            class="primary-btn"
            :disabled="saving"
            @click="finishOnboarding"
          >
            {{ saving ? 'Saving...' : 'Finish Setup' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { completeOnboarding, getOnboardingOptions, writeOnboardingStatusCache } from '../utils/onboardingApi'

const EMPTY_LOOKING_FOR = {
  studyPartner: false,
  languageExchange: false,
  friendship: false,
  networking: false,
  community: false
}

const EMPTY_CONTENT_PREFERENCES = {
  studyGuides: true,
  projectShowcases: true,
  communityDiscussions: true,
  events: true,
  careerOpportunities: true
}

export default {
  name: 'OnboardingPage',
  data() {
    return {
      loading: true,
      saving: false,
      error: '',
      step: 1,
      totalSteps: 4,
      stepLabels: ['Interests', 'Goals', 'Communities', 'Review'],
      interestInput: '',
      tagInput: '',
      communitySearch: '',
      interestSuggestions: [],
      communityTagSuggestions: [],
      contentOptions: [],
      communities: [],
      form: {
        interests: [],
        preferredCommunityTags: [],
        selectedCommunityIds: [],
        lookingFor: { ...EMPTY_LOOKING_FOR },
        contentPreferences: { ...EMPTY_CONTENT_PREFERENCES },
        openToCollaborate: false
      }
    }
  },
  computed: {
    progressPercent() {
      return Math.round((this.step / this.totalSteps) * 100)
    },
    filteredCommunities() {
      const query = this.communitySearch.trim().toLowerCase()
      if (!query) return this.communities
      return this.communities.filter((community) => {
        const haystack = [
          community.name,
          community.description,
          ...(community.tags || [])
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
        return haystack.includes(query)
      })
    },
    selectedCommunities() {
      const selected = new Set(this.form.selectedCommunityIds)
      return this.communities.filter((community) => selected.has(community.id))
    },
    enabledContentLabels() {
      return this.contentOptions
        .filter((option) => this.form.contentPreferences[option.key])
        .map((option) => option.label)
    }
  },
  async created() {
    await this.loadOnboardingOptions()
  },
  methods: {
    async loadOnboardingOptions() {
      this.loading = true
      this.error = ''
      try {
        const response = await getOnboardingOptions()
        const data = response?.data || {}
        if (data.completed) {
          writeOnboardingStatusCache(true)
          this.$router.replace('/authenticated/feed')
          return
        }

        this.interestSuggestions = Array.isArray(data.interestSuggestions) ? data.interestSuggestions : []
        this.communityTagSuggestions = Array.isArray(data.communityTagSuggestions) ? data.communityTagSuggestions : []
        this.contentOptions = Array.isArray(data.contentOptions) ? data.contentOptions : []
        this.communities = Array.isArray(data.communityOptions) ? data.communityOptions : []

        this.form.interests = Array.isArray(data.currentInterests) ? [...data.currentInterests] : []
        this.form.preferredCommunityTags = Array.isArray(data.currentPreferredCommunityTags) ? [...data.currentPreferredCommunityTags] : []
        this.form.selectedCommunityIds = Array.isArray(data.joinedCommunityIds) ? [...data.joinedCommunityIds] : []
        this.form.lookingFor = { ...EMPTY_LOOKING_FOR, ...(data.currentLookingFor || {}) }
        this.form.contentPreferences = { ...EMPTY_CONTENT_PREFERENCES, ...(data.currentContentPreferences || {}) }
        this.form.openToCollaborate = !!data.openToCollaborate
      } catch (err) {
        console.error('Failed to load onboarding options:', err)
        this.error = err?.response?.data?.message || 'Unable to load onboarding right now. Please refresh.'
      } finally {
        this.loading = false
      }
    },
    normalizeChipValue(value) {
      return String(value || '')
        .replace(/[#]/g, '')
        .replace(/\s+/g, ' ')
        .trim()
    },
    addInterestFromInput() {
      const interest = this.normalizeChipValue(this.interestInput)
      if (!interest) return
      this.toggleInterest(interest, true)
      this.interestInput = ''
    },
    toggleInterest(interest, forceAdd = false) {
      const normalized = this.normalizeChipValue(interest)
      if (!normalized) return
      const index = this.form.interests.findIndex((item) => item.toLowerCase() === normalized.toLowerCase())
      if (index >= 0 && !forceAdd) {
        this.form.interests.splice(index, 1)
        return
      }
      if (index < 0) {
        this.form.interests.push(normalized)
      }
    },
    removeInterest(interest) {
      this.toggleInterest(interest)
    },
    addPreferredTagFromInput() {
      const tag = this.normalizeChipValue(this.tagInput)
      if (!tag) return
      this.togglePreferredTag(tag, true)
      this.tagInput = ''
    },
    togglePreferredTag(tag, forceAdd = false) {
      const normalized = this.normalizeChipValue(tag)
      if (!normalized) return
      const index = this.form.preferredCommunityTags.findIndex((item) => item.toLowerCase() === normalized.toLowerCase())
      if (index >= 0 && !forceAdd) {
        this.form.preferredCommunityTags.splice(index, 1)
        return
      }
      if (index < 0) {
        this.form.preferredCommunityTags.push(normalized)
      }
    },
    removePreferredTag(tag) {
      this.togglePreferredTag(tag)
    },
    toggleCommunitySelection(communityId) {
      const index = this.form.selectedCommunityIds.indexOf(communityId)
      if (index >= 0) {
        this.form.selectedCommunityIds.splice(index, 1)
      } else {
        this.form.selectedCommunityIds.push(communityId)
      }
    },
    validateStep() {
      if (this.step === 1 && this.form.interests.length === 0) {
        this.error = 'Select at least one interest to continue.'
        return false
      }

      if (this.step === 2) {
        const anyGoal = Object.values(this.form.lookingFor).some(Boolean) || this.form.openToCollaborate
        const anyContent = Object.values(this.form.contentPreferences).some(Boolean)
        if (!anyGoal) {
          this.error = 'Select at least one goal so we can personalize your recommendations.'
          return false
        }
        if (!anyContent) {
          this.error = 'Select at least one content type to continue.'
          return false
        }
      }

      this.error = ''
      return true
    },
    nextStep() {
      if (!this.validateStep()) return
      this.step = Math.min(this.totalSteps, this.step + 1)
    },
    previousStep() {
      this.error = ''
      this.step = Math.max(1, this.step - 1)
    },
    async finishOnboarding() {
      if (!this.validateStep()) return
      this.saving = true
      this.error = ''
      try {
        await completeOnboarding({
          interests: this.form.interests,
          preferredCommunityTags: this.form.preferredCommunityTags,
          selectedCommunityIds: this.form.selectedCommunityIds,
          lookingFor: this.form.lookingFor,
          contentPreferences: this.form.contentPreferences,
          openToCollaborate: this.form.openToCollaborate
        })
        writeOnboardingStatusCache(true)
        this.$router.replace('/authenticated/feed')
      } catch (err) {
        console.error('Failed to complete onboarding:', err)
        this.error = err?.response?.data?.message || 'Could not complete onboarding. Please try again.'
      } finally {
        this.saving = false
      }
    },
    truncate(value, maxLength) {
      if (!value) return ''
      if (value.length <= maxLength) return value
      return `${value.slice(0, maxLength).trim()}...`
    }
  }
}
</script>

<style scoped>
.onboarding-page {
  min-height: 100vh;
  padding: 32px 20px 120px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.onboarding-shell {
  width: min(1080px, 100%);
  margin: 0 auto;
  border-radius: 22px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 28px;
}

.onboarding-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.onboarding-header h1 {
  margin: 0 0 4px;
  font-size: clamp(1.8rem, 2.6vw, 2.6rem);
}

.onboarding-header p {
  margin: 0;
  color: var(--theme-text-secondary);
}

.progress-meta {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--theme-text-secondary);
}

.progress-track {
  width: 100%;
  height: 10px;
  background: var(--theme-button-secondary-bg);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 16px;
}

.progress-fill {
  height: 100%;
  background: var(--theme-button-primary-bg);
  transition: width 0.25s ease;
}

.step-tabs {
  list-style: none;
  padding: 0;
  margin: 0 0 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.step-tabs li {
  border-radius: 999px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-subtle);
  text-align: center;
  padding: 8px 10px;
  font-size: 0.9rem;
  font-weight: 600;
}

.step-tabs li.active {
  color: var(--theme-text-primary);
  border-color: var(--theme-accent);
  box-shadow: 0 0 0 3px var(--theme-focus-ring);
}

.step-tabs li.done {
  color: var(--theme-text-primary);
  border-color: var(--theme-surface-border);
}

.step-card {
  border-radius: 18px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  padding: 20px;
}

.step-card h2 {
  margin: 0 0 8px;
  font-size: 1.5rem;
}

.step-card h3 {
  margin: 18px 0 10px;
  font-size: 1.1rem;
}

.step-card p {
  margin: 0 0 14px;
  color: var(--theme-text-secondary);
}

.input-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.input-row input {
  flex: 1;
  border-radius: 12px;
  border: 1px solid var(--theme-input-border);
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  padding: 12px 14px;
  font-size: 0.95rem;
}

.input-row input:focus {
  outline: none;
  border-color: var(--theme-accent);
  box-shadow: 0 0 0 3px var(--theme-focus-ring);
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip-group.compact {
  gap: 8px;
}

.chip {
  border: 1px solid var(--theme-chip-border);
  background: var(--theme-chip-bg);
  color: var(--theme-chip-text);
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 0.86rem;
}

button.chip {
  cursor: pointer;
  transition: transform 0.15s ease, filter 0.15s ease;
}

button.chip:hover {
  transform: translateY(-1px);
  filter: brightness(0.98);
}

.chip-selected,
.chip.active {
  border-color: var(--theme-accent);
  color: var(--theme-text-inverse);
  background: var(--theme-accent);
}

.hint {
  color: var(--theme-text-subtle);
  font-size: 0.92rem;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.toggle-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--theme-surface-border);
  border-radius: 12px;
  background: var(--theme-surface-2);
  color: var(--theme-text-primary);
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.content-option {
  display: flex;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--theme-surface-border);
  border-radius: 14px;
  background: var(--theme-surface-2);
}

.content-option p {
  margin: 4px 0 0;
  font-size: 0.9rem;
}

.community-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.community-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 14px;
  background: var(--theme-surface-2);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.community-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.community-top h4 {
  margin: 0;
  font-size: 1rem;
}

.community-desc {
  margin: 0;
  font-size: 0.92rem;
  color: var(--theme-text-secondary);
}

.community-meta {
  margin: 0;
  font-size: 0.85rem;
  color: var(--theme-text-subtle);
}

.community-reason {
  margin: 0;
  font-size: 0.84rem;
  color: var(--theme-accent);
}

.badge {
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.75rem;
  background: var(--theme-accent-soft);
  color: var(--theme-accent-strong);
  border: 1px solid var(--theme-chip-border);
}

.select-btn,
.primary-btn,
.secondary-btn {
  border-radius: 12px;
  padding: 10px 14px;
  font-weight: 700;
  border: 1px solid transparent;
  cursor: pointer;
}

.select-btn {
  margin-top: auto;
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border-color: var(--theme-button-secondary-border);
}

.select-btn.selected {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
}

.review-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.review-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 14px;
  padding: 14px;
  background: var(--theme-surface-2);
}

.review-card h3 {
  margin: 0 0 8px;
}

.review-card ul {
  margin: 0;
  padding-left: 18px;
  color: var(--theme-text-secondary);
}

.footer-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.primary-btn {
  background: var(--theme-button-primary-bg);
  color: var(--theme-button-primary-text);
  box-shadow: var(--theme-button-primary-shadow);
}

.secondary-btn {
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border-color: var(--theme-button-secondary-border);
}

.state-error {
  margin: 0 0 12px;
  color: var(--theme-danger);
}

.state-message {
  color: var(--theme-text-secondary);
}

@media (max-width: 1100px) {
  .community-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 820px) {
  .onboarding-header {
    flex-direction: column;
  }

  .option-grid,
  .content-grid,
  .review-grid {
    grid-template-columns: 1fr;
  }

  .step-tabs {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .onboarding-page {
    padding: 16px 12px 96px;
  }

  .onboarding-shell {
    padding: 16px;
    border-radius: 16px;
  }

  .input-row {
    flex-direction: column;
  }

  .community-grid {
    grid-template-columns: 1fr;
  }

  .footer-actions {
    flex-direction: column-reverse;
  }

  .primary-btn,
  .secondary-btn {
    width: 100%;
  }
}
</style>
