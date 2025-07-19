<template>
    <div class="swipe-card">
      <div v-if="currentUser" class="user-card">
        <h2>{{ currentUser.name }}</h2>
        <p>{{ currentUser.major }}</p>
        <p>{{ currentUser.bio }}</p>
        <div class="actions">
          <button @click="swipe(false)">Dislike</button>
          <button @click="swipe(true)">Like</button>
        </div>
      </div>
      <div v-else class="no-matches">
        <p>No more potential matches!</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'SwipeCard',
    data() {
      return {
        potentialMatches: [],
        currentUser: null,
        currentIndex: 0
      };
    },
    created() {
      this.fetchPotentialMatches();
    },
    methods: {
      async fetchPotentialMatches() {
        try {
          const response = await fetch(`/api/matches/potential/1`); // Replace 1 with the actual user ID
          this.potentialMatches = await response.json();
          this.currentUser = this.potentialMatches[0];
        } catch (error) {
          console.error('Error fetching potential matches:', error);
        }
      },
      async swipe(like) {
        if (!this.currentUser) return;
        try {
          const response = await fetch('/api/matches/swipe', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({
              userId: 1, // Replace with the actual user ID
              targetUserId: this.currentUser.id,
              like
            })
          });
          const result = await response.json();
          console.log('Swipe result:', result);
          this.currentIndex++;
          this.currentUser = this.potentialMatches[this.currentIndex];
        } catch (error) {
          console.error('Error swiping:', error);
        }
      }
    }
  };
  </script>
  
  <style scoped>
  .swipe-card {
    max-width: 400px;
    margin: 0 auto;
    padding: 20px;
    border: 1px solid #ccc;
    border-radius: 8px;
  }
  .user-card {
    text-align: center;
  }
  .actions {
    display: flex;
    justify-content: space-around;
    margin-top: 20px;
  }
  button {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  button:first-child {
    background-color: #ff4d4d;
    color: white;
  }
  button:last-child {
    background-color: #4caf50;
    color: white;
  }
  .no-matches {
    text-align: center;
    font-style: italic;
  }
  </style>
  