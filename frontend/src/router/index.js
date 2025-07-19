import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import ProfilePage from '../views/ProfilePage.vue'
import CommunitiesPage from '../views/CommunitiesPage.vue'
import CommunityDetailPage from '../views/CommunityDetailPage.vue'

import ChatPage from '../views/ChatPage.vue'
import DiscoveryPage from '../views/DiscoveryPage.vue'

import AppLayout from '../components/AppLayout.vue'

const routes = [
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
        redirect: { name: 'Profile' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: ProfilePage
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
    path: '/',
    redirect: (to) => {
      const isAuthenticated = !!localStorage.getItem('token');
      if (isAuthenticated) {
        return { path: '/authenticated' };
      } else {
        return { name: 'Login' };
      }
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const isAuthenticated = !!localStorage.getItem('token')
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  if (requiresAuth && !isAuthenticated) {
    next({ name: 'Login' });
  } else {
    next();
  }
})

export default router
