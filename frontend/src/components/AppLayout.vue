<template>
  <div class="app-layout">
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppLayout',
};
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.content {
  flex-grow: 1;
}
</style> 