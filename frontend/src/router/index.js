import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import ProfilePage from '../views/ProfilePage.vue'
import FeedPage from '../views/FeedPage.vue'
import OnboardingPage from '../views/OnboardingPage.vue'
import PublicProfilePage from '../views/PublicProfilePage.vue'
import SettingsPage from '../views/SettingsPage.vue'
import CommunitiesPage from '../views/CommunitiesPage.vue'
import CommunityDetailPage from '../views/CommunityDetailPage.vue'
import { getOnboardingStatus, readOnboardingStatusCache, writeOnboardingStatusCache } from '../utils/onboardingApi'

import ChatPage from '../views/ChatPage.vue'
import DiscoveryPage from '../views/DiscoveryPage.vue'

import AppLayout from '../components/AppLayout.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/authenticated',
    component: AppLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: { name: 'Feed' }
      },
      {
        path: 'feed',
        name: 'Feed',
        component: FeedPage
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfilePage
      },
      {
        path: 'onboarding',
        name: 'Onboarding',
        component: OnboardingPage
      },
      {
        path: 'people/:userId',
        alias: 'profile/:userId',
        name: 'PublicProfile',
        component: PublicProfilePage,
        props: true
      },
      {
        path: 'settings',
        name: 'Settings',
        component: SettingsPage
      },
      {
        path: 'communities',
        name: 'Communities',
        component: CommunitiesPage
      },
      {
        path: 'communities/:communityId',
        name: 'CommunityDetail',
        component: CommunityDetailPage,
        props: true
      },
      {
        path: 'matches',
        name: 'Matches',
        component: DiscoveryPage
      },
      {
        path: 'chat/:chatRoomId?',
        name: 'Chat',
        component: ChatPage
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach(async (to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  if (requiresAuth && !isAuthenticated) {
    next({ name: 'Login' })
    return
  }

  if (to.name === 'Home' && isAuthenticated) {
    next({ path: '/authenticated' })
    return
  }

  if (isAuthenticated && to.name === 'Onboarding') {
    const cachedStatus = readOnboardingStatusCache()
    if (cachedStatus === true) {
      next({ name: 'Feed' })
      return
    }

    try {
      const response = await getOnboardingStatus()
      const completed = response?.data?.completed === true
      writeOnboardingStatusCache(completed)
      if (completed) {
        next({ name: 'Feed' })
        return
      }
    } catch (error) {
      // Keep navigation permissive if the onboarding status endpoint fails.
    }
  }

  next()
})

export default router
