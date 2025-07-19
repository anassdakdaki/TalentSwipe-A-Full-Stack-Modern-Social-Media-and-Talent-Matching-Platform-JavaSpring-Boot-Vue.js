<template>
  <div class="home">
    <h1>Find Your Study Partner</h1>
    <div class="swipe-container">
      <SwipeCard />
    </div>
  </div>
</template>

<script>
import SwipeCard from '../components/SwipeCard.vue'

export default {
  name: 'HomeView',
  components: {
    SwipeCard
  }
}
</script>

<style scoped>
.home {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 2rem;
}

.swipe-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}
</style>
