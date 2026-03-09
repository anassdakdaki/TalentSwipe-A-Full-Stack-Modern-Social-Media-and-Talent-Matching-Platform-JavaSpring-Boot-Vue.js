<template>
  <div class="feed-page">
    <section class="feed-shell page-shell">
      <header class="feed-header">
        <h1>Your Feed</h1>
        <p>Latest posts from communities you joined.</p>
      </header>

      <p v-if="loadingInitial" class="state-message">Loading feed...</p>
      <p v-else-if="loadError" class="state-error">{{ loadError }}</p>

      <div v-else-if="posts.length === 0" class="empty-state">
        <h2>Join communities to build your feed</h2>
        <p>Once you join communities, you will see posts from members here.</p>
        <button class="primary-button" @click="goToCommunities">Explore Communities</button>
      </div>

      <div v-else class="feed-layout">
        <div class="feed-list">
          <article v-for="post in posts" :key="post.id" class="post-card">
            <div class="post-header">
              <button class="author-button" @click="openProfileModal(post.authorId)">
                <img
                  v-if="post.authorProfilePictureUrl"
                  :src="normalizeMediaUrl(post.authorProfilePictureUrl)"
                  alt="Author avatar"
                  class="author-avatar"
                />
                <div v-else class="author-avatar placeholder">
                  <i class="fas fa-user"></i>
                </div>
                <div class="author-copy">
                  <span class="author-name">{{ post.authorName }}</span>
                  <span class="post-time">{{ formatDate(post.createdAt) }}</span>
                </div>
              </button>

              <button class="community-button" @click="openCommunity(post.communityId)">
                <img
                  v-if="post.communityImageUrl"
                  :src="normalizeMediaUrl(post.communityImageUrl)"
                  alt="Community avatar"
                  class="community-avatar"
                />
                <span>{{ post.communityName }}</span>
              </button>
            </div>

            <p class="post-content">{{ post.content }}</p>
            <img
              v-if="post.imageUrl"
              :src="normalizeMediaUrl(post.imageUrl)"
              alt="Post media"
              class="post-image"
            />

            <div v-if="post.hashtags && post.hashtags.length" class="hashtag-row">
              <span v-for="tag in post.hashtags" :key="`${post.id}-${tag}`" class="tag">#{{ tag }}</span>
            </div>

            <div class="post-actions">
              <button class="action-button" :disabled="post.liking" @click="handleToggleLike(post)">
                <i :class="[post.isLiked ? 'fas' : 'far', 'fa-heart']"></i>
                <span>{{ post.isLiked ? 'Liked' : 'Like' }} ({{ post.likesCount }})</span>
              </button>
              <button class="action-button" @click="toggleComments(post)">
                <i class="far fa-comment"></i>
                <span>Comments ({{ post.commentsCount }})</span>
              </button>
            </div>

            <div v-if="post.commentsExpanded" class="comments-panel">
              <p v-if="post.commentsLoading" class="comments-state">Loading comments...</p>
              <p v-else-if="post.commentsError" class="comments-error">{{ post.commentsError }}</p>
              <p v-else-if="post.comments.length === 0" class="comments-state">No comments yet.</p>

              <div v-else class="comment-list">
                <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
                  <img
                    v-if="comment.authorProfilePictureUrl"
                    :src="normalizeMediaUrl(comment.authorProfilePictureUrl)"
                    alt="Commenter avatar"
                    class="comment-avatar"
                  />
                  <div v-else class="comment-avatar placeholder">
                    <i class="fas fa-user"></i>
                  </div>
                  <div class="comment-body">
                    <div class="comment-meta">
                      <strong>{{ comment.authorName }}</strong>
                      <span>{{ formatDate(comment.createdAt) }}</span>
                    </div>
                    <span>{{ comment.content }}</span>
                  </div>
                </div>
              </div>
            </div>
            <div class="comment-input-row">
              <img
                v-if="currentUserProfilePictureUrl"
                :src="normalizeMediaUrl(currentUserProfilePictureUrl)"
                alt="Your avatar"
                class="comment-avatar"
              />
              <div v-else class="comment-avatar placeholder">
                <i class="fas fa-user"></i>
              </div>
              <input
                v-model="post.newCommentContent"
                type="text"
                placeholder="Write a comment..."
                @focus="ensureCommentsLoaded(post)"
                @keyup.enter="submitComment(post)"
              />
              <button
                class="secondary-button"
                :disabled="post.commentSubmitting"
                @click="submitComment(post)"
              >
                {{ post.commentSubmitting ? 'Posting...' : 'Post' }}
              </button>
            </div>
          </article>

          <div ref="sentinel" class="feed-sentinel">
            <p v-if="loadingMore">Loading more...</p>
            <p v-else-if="hasMore">Scroll for more</p>
          </div>
        </div>

        <aside class="feed-side">
          <div class="side-card snapshot-card">
            <div class="snapshot-head">
              <span class="snapshot-kicker">Feed Snapshot</span>
              <h3>Activity Overview</h3>
            </div>

            <div class="snapshot-highlight">
              <div class="highlight-icon">
                <i class="fas fa-stream"></i>
              </div>
              <div class="highlight-copy">
                <strong>{{ posts.length }}</strong>
                <span>Posts currently loaded</span>
              </div>
            </div>

            <div class="snapshot-grid">
              <div class="snapshot-chip">
                <i class="fas fa-users"></i>
                <div>
                  <strong>{{ communityCount }}</strong>
                  <span>Communities</span>
                </div>
              </div>
              <div class="snapshot-chip">
                <i class="fas fa-user-friends"></i>
                <div>
                  <strong>{{ authorCount }}</strong>
                  <span>People</span>
                </div>
              </div>
              <div class="snapshot-chip">
                <i class="fas fa-image"></i>
                <div>
                  <strong>{{ imagePostCount }}</strong>
                  <span>Image Posts</span>
                </div>
              </div>
              <div class="snapshot-chip">
                <i class="fas fa-heart"></i>
                <div>
                  <strong>{{ likedPostsCount }}</strong>
                  <span>You Liked</span>
                </div>
              </div>
            </div>
          </div>

          <div class="side-card">
            <h3>Quick Actions</h3>
            <button class="side-btn" @click="goToCommunities">
              Explore Communities
            </button>
            <button class="side-btn" @click="$router.push('/authenticated/matches')">
              Find New Matches
            </button>
          </div>
        </aside>
      </div>
    </section>

    <UserProfileModal
      :visible="profileModalVisible"
      :user-id="selectedProfileUserId"
      :current-user-id="currentUserId"
      @close="closeProfileModal"
      @open-chat-requested="openChatFromModal"
    />
  </div>
</template>

<script>
import UserProfileModal from '@/components/UserProfileModal.vue'
import {
  createComment,
  getComments,
  getFeedPosts,
  normalizeMediaUrl,
  toggleLike
} from '@/utils/feedApi'
import { findOrCreateChatRoom, getCurrentUser } from '@/utils/profileApi'

export default {
  name: 'FeedPage',
  components: {
    UserProfileModal
  },
  computed: {
    communityCount() {
      return new Set(this.posts.map((post) => post.communityId).filter((id) => id !== null && id !== undefined)).size
    },
    authorCount() {
      return new Set(this.posts.map((post) => post.authorId).filter((id) => id !== null && id !== undefined)).size
    },
    imagePostCount() {
      return this.posts.filter((post) => Boolean(post.imageUrl)).length
    },
    likedPostsCount() {
      return this.posts.filter((post) => Boolean(post.isLiked)).length
    }
  },
  data() {
    return {
      currentUserId: null,
      currentUserProfilePictureUrl: '',
      posts: [],
      loadingInitial: false,
      loadingMore: false,
      loadError: '',
      hasMore: false,
      nextCursorCreatedAt: null,
      nextCursorPostId: null,
      pageLimit: 10,
      observer: null,
      profileModalVisible: false,
      selectedProfileUserId: null
    }
  },
  async created() {
    await this.fetchCurrentUser()
    await this.loadFeed({ reset: true })
  },
  mounted() {
    this.initObserver()
    this.$nextTick(() => this.observeSentinel())
  },
  beforeUnmount() {
    if (this.observer) {
      this.observer.disconnect()
      this.observer = null
    }
  },
  watch: {
    posts() {
      this.$nextTick(() => this.observeSentinel())
    }
  },
  methods: {
    normalizeMediaUrl,
    async fetchCurrentUser() {
      try {
        const response = await getCurrentUser()
        this.currentUserId = response.data?.id || null
        this.currentUserProfilePictureUrl = response.data?.profilePictureUrl || ''
      } catch (error) {
        this.currentUserId = null
        this.currentUserProfilePictureUrl = ''
      }
    },
    initObserver() {
      if (typeof window === 'undefined' || !window.IntersectionObserver) {
        return
      }

      this.observer = new IntersectionObserver(
        (entries) => {
          for (const entry of entries) {
            if (!entry.isIntersecting) continue
            if (this.loadingInitial || this.loadingMore || !this.hasMore) continue
            this.loadFeed({ reset: false })
          }
        },
        {
          root: null,
          rootMargin: '220px 0px',
          threshold: 0
        }
      )
    },
    observeSentinel() {
      if (!this.observer || !this.$refs.sentinel) {
        return
      }
      this.observer.disconnect()
      this.observer.observe(this.$refs.sentinel)
    },
    mapPost(raw) {
      const hasExistingComments = Number(raw?.commentsCount || 0) > 0
      return {
        ...raw,
        hashtags: Array.isArray(raw?.hashtags) ? raw.hashtags : [],
        likesCount: Number(raw?.likesCount || 0),
        isLiked: Boolean(raw?.isLiked),
        commentsCount: Number(raw?.commentsCount || 0),
        comments: [],
        commentsLoaded: false,
        commentsExpanded: hasExistingComments,
        commentsLoading: hasExistingComments,
        commentsError: '',
        newCommentContent: '',
        commentSubmitting: false,
        liking: false
      }
    },
    prefetchExpandedComments(posts) {
      const candidates = Array.isArray(posts)
        ? posts.filter((post) => post.commentsExpanded && !post.commentsLoaded)
        : []
      if (candidates.length === 0) {
        return
      }
      Promise.all(candidates.map((post) => this.loadCommentsForPost(post))).catch(() => {})
    },
    async loadFeed({ reset }) {
      if (reset) {
        if (this.loadingInitial) return
        this.loadingInitial = true
        this.loadError = ''
        this.posts = []
        this.hasMore = false
        this.nextCursorCreatedAt = null
        this.nextCursorPostId = null
      } else {
        if (this.loadingMore || !this.hasMore) return
        this.loadingMore = true
      }

      try {
        const response = await getFeedPosts({
          limit: this.pageLimit,
          cursorCreatedAt: reset ? null : this.nextCursorCreatedAt,
          cursorPostId: reset ? null : this.nextCursorPostId
        })
        const payload = response.data || {}
        const incoming = Array.isArray(payload.items) ? payload.items.map(this.mapPost) : []

        if (reset) {
          this.posts = incoming
        } else {
          this.posts.push(...incoming)
        }
        this.prefetchExpandedComments(incoming)

        this.nextCursorCreatedAt = payload.nextCursorCreatedAt || null
        this.nextCursorPostId = payload.nextCursorPostId ?? null
        this.hasMore = Boolean(payload.hasMore)
      } catch (error) {
        if (reset) {
          this.loadError = error?.response?.data?.error || 'Could not load feed right now.'
        }
      } finally {
        if (reset) {
          this.loadingInitial = false
        } else {
          this.loadingMore = false
        }
      }
    },
    async handleToggleLike(post) {
      if (post.liking) return
      post.liking = true

      try {
        const response = await toggleLike(post.id)
        const newIsLiked = Boolean(response?.data?.isLiked)
        if (newIsLiked !== post.isLiked) {
          post.likesCount = Math.max(0, post.likesCount + (newIsLiked ? 1 : -1))
        }
        post.isLiked = newIsLiked
      } catch (error) {
        post.commentsError = error?.response?.data?.error || 'Unable to update like right now.'
      } finally {
        post.liking = false
      }
    },
    async toggleComments(post) {
      post.commentsExpanded = !post.commentsExpanded
      if (!post.commentsExpanded || post.commentsLoaded || post.commentsLoading) {
        return
      }
      await this.loadCommentsForPost(post)
    },
    async loadCommentsForPost(post) {
      post.commentsLoading = true
      post.commentsError = ''
      try {
        const response = await getComments(post.id)
        post.comments = Array.isArray(response.data) ? response.data : []
        post.commentsCount = post.comments.length
        post.commentsLoaded = true
      } catch (error) {
        post.commentsError = error?.response?.data?.error || 'Failed to load comments for this post.'
      } finally {
        post.commentsLoading = false
      }
    },
    async ensureCommentsLoaded(post) {
      if (post.commentsLoaded || post.commentsLoading) {
        return
      }
      post.commentsExpanded = true
      await this.loadCommentsForPost(post)
    },
    async submitComment(post) {
      const content = String(post.newCommentContent || '').trim()
      if (!content || post.commentSubmitting) {
        return
      }

      post.commentSubmitting = true
      post.commentsError = ''

      try {
        await createComment({ postId: post.id, content })
        post.newCommentContent = ''
        post.commentsExpanded = true
        await this.loadCommentsForPost(post)
      } catch (error) {
        post.commentsError = error?.response?.data?.error || 'Failed to create comment for this post.'
      } finally {
        post.commentSubmitting = false
      }
    },
    openProfileModal(userId) {
      this.selectedProfileUserId = Number(userId) || null
      this.profileModalVisible = !!this.selectedProfileUserId
    },
    closeProfileModal() {
      this.profileModalVisible = false
      this.selectedProfileUserId = null
    },
    async openChatFromModal(payload) {
      try {
        if (!this.currentUserId) {
          const me = await getCurrentUser()
          this.currentUserId = me.data?.id || null
        }

        if (!this.currentUserId) {
          return
        }

        const response = await findOrCreateChatRoom({
          user1Id: this.currentUserId,
          user2Id: Number(payload.userId),
          studyMatchId: Number(payload.matchId)
        })
        this.closeProfileModal()
        this.$router.push({ name: 'Chat', params: { chatRoomId: response.data.id } })
      } catch (error) {
        this.loadError = error?.response?.data?.error || 'Unable to open chat right now.'
      }
    },
    openCommunity(communityId) {
      this.$router.push({ name: 'CommunityDetail', params: { communityId } })
    },
    goToCommunities() {
      this.$router.push('/authenticated/communities')
    },
    formatDate(rawDate) {
      if (!rawDate) return ''
      try {
        return new Date(rawDate).toLocaleString()
      } catch {
        return rawDate
      }
    }
  }
}
</script>

<style scoped>
.feed-page {
  min-height: 100vh;
  padding: 20px 14px 110px;
  background: var(--theme-page-background);
  color: var(--theme-text-primary);
}

.feed-shell {
  width: min(var(--page-max-width), 100%);
  margin: 0 auto;
}

.feed-header {
  margin-bottom: 16px;
}

.feed-header h1 {
  margin: 0;
  font-size: clamp(1.8rem, 2.6vw, 2.5rem);
}

.feed-header p {
  margin: 6px 0 0;
  color: var(--theme-text-secondary);
}

.feed-layout {
  display: grid;
  grid-template-columns: minmax(0, 700px) 320px;
  gap: var(--page-content-gap);
  justify-content: center;
  align-items: start;
}

.feed-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
}

.feed-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: sticky;
  top: 14px;
  height: fit-content;
}

.side-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 16px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 14px;
}

.side-card h3 {
  margin: 0 0 10px;
  color: var(--theme-heading-color);
  font-size: 1.05rem;
}

.snapshot-card {
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(circle at 92% 8%, color-mix(in srgb, var(--theme-accent) 18%, transparent), transparent 38%),
    linear-gradient(160deg, var(--theme-surface-elevated) 0%, color-mix(in srgb, var(--theme-accent) 6%, var(--theme-surface-elevated)) 100%);
}

.snapshot-head {
  margin-bottom: 10px;
}

.snapshot-kicker {
  display: inline-block;
  margin-bottom: 5px;
  color: var(--theme-text-secondary);
  font-size: 0.76rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.snapshot-head h3 {
  margin: 0;
}

.snapshot-highlight {
  border: 1px solid var(--theme-surface-border);
  background: color-mix(in srgb, var(--theme-surface-1) 86%, transparent);
  border-radius: 12px;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.highlight-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  color: var(--theme-accent);
}

.highlight-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.highlight-copy strong {
  font-size: 1.22rem;
  line-height: 1;
}

.highlight-copy span {
  color: var(--theme-text-secondary);
  font-size: 0.8rem;
}

.snapshot-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.snapshot-chip {
  border: 1px solid var(--theme-surface-border);
  border-radius: 10px;
  background: color-mix(in srgb, var(--theme-surface-1) 88%, transparent);
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.snapshot-chip i {
  width: 24px;
  text-align: center;
  color: var(--theme-accent);
}

.snapshot-chip div {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.snapshot-chip strong {
  font-size: 1.02rem;
  line-height: 1.1;
}

.snapshot-chip span {
  color: var(--theme-text-secondary);
  font-size: 0.76rem;
}

.side-btn {
  width: 100%;
  border: 1px solid var(--theme-button-secondary-border);
  border-radius: 10px;
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  padding: 10px 12px;
  font-weight: 700;
  cursor: pointer;
}

.side-btn + .side-btn {
  margin-top: 8px;
}

.side-btn:hover {
  filter: brightness(1.03);
}

.post-card {
  border: 1px solid var(--theme-surface-border);
  border-radius: 16px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 12px;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.author-button {
  display: flex;
  align-items: center;
  gap: 10px;
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
  padding: 0;
}

.author-avatar,
.community-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-subtle);
}

.author-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-weight: 700;
}

.post-time {
  color: var(--theme-text-subtle);
  font-size: 0.84rem;
}

.community-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-primary);
  border-radius: 999px;
  cursor: pointer;
  padding: 6px 10px;
  font-size: 0.85rem;
}

.community-avatar {
  width: 22px;
  height: 22px;
}

.post-content {
  margin: 12px 0;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid var(--theme-surface-border);
}

.hashtag-row {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  border-radius: 999px;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  color: var(--theme-text-secondary);
  padding: 5px 10px;
  font-size: 0.84rem;
}

.post-actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.action-button {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
  border-radius: 999px;
  cursor: pointer;
  padding: 8px 12px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
}

.action-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.comments-panel {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--theme-surface-border);
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-item {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-1);
  border-radius: 10px;
  padding: 8px 10px;
  line-height: 1.45;
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.comment-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
  flex: 0 0 30px;
}

.comment-avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-surface-elevated);
  color: var(--theme-text-subtle);
}

.comment-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.comment-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--theme-text-subtle);
  font-size: 0.8rem;
}

.comment-input-row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-input-row input {
  flex: 1;
  border-radius: 10px;
  border: 1px solid var(--theme-input-border);
  background: var(--theme-input-bg);
  color: var(--theme-input-text);
  padding: 10px 12px;
}

.comment-input-row input:focus {
  outline: none;
  border-color: var(--theme-accent);
}

.secondary-button,
.primary-button {
  border-radius: 10px;
  padding: 10px 14px;
  font-weight: 700;
  cursor: pointer;
}

.secondary-button {
  border: 1px solid var(--theme-surface-border);
  background: var(--theme-button-secondary-bg);
  color: var(--theme-button-secondary-text);
}

.primary-button {
  border: none;
  color: var(--theme-button-primary-text);
  background: var(--theme-button-primary-bg);
  box-shadow: var(--theme-button-primary-shadow, none);
}

.state-message,
.state-error {
  text-align: center;
  margin-top: 40px;
}

.state-error,
.comments-error {
  color: var(--theme-danger, #ef4444);
}

.comments-state {
  color: var(--theme-text-secondary);
  font-size: 0.92rem;
}

.empty-state {
  text-align: center;
  border: 1px solid var(--theme-surface-border);
  border-radius: 16px;
  background: var(--theme-surface-elevated);
  box-shadow: var(--theme-shadow-soft);
  padding: 30px 16px;
}

.empty-state h2 {
  margin: 0 0 8px;
}

.empty-state p {
  margin: 0 0 16px;
  color: var(--theme-text-secondary);
}

.feed-sentinel {
  padding: 6px 0 0;
  text-align: center;
  color: var(--theme-text-subtle);
  font-size: 0.9rem;
}

@media (max-width: 760px) {
  .feed-page {
    padding: 12px 8px 90px;
  }

  .post-card {
    padding: 12px;
  }

  .post-header {
    flex-direction: column;
    align-items: stretch;
  }

  .community-button {
    width: fit-content;
  }

  .comment-input-row {
    flex-direction: column;
    align-items: stretch;
  }

  .snapshot-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1120px) {
  .feed-layout {
    grid-template-columns: 1fr;
  }

  .feed-side {
    position: static;
  }

  .feed-list {
    width: 100%;
  }
}
</style>
