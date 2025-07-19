<template>
  <div class="progress-container">
    <div class="header">
      <h1>Study Progress</h1>
      <p>Track your academic progress and achievements</p>
    </div>

    <div class="progress-content">
      <div class="progress-overview">
        <div class="overview-card">
          <h3>Study Hours</h3>
          <div class="stat">
            <span class="number">{{ totalStudyHours }}</span>
            <span class="label">hours</span>
          </div>
          <div class="trend" :class="{ positive: studyHoursTrend > 0 }">
            {{ studyHoursTrend > 0 ? '+' : '' }}{{ studyHoursTrend }}% from last week
          </div>
        </div>

        <div class="overview-card">
          <h3>Completed Tasks</h3>
          <div class="stat">
            <span class="number">{{ completedTasks }}</span>
            <span class="label">tasks</span>
          </div>
          <div class="trend" :class="{ positive: tasksTrend > 0 }">
            {{ tasksTrend > 0 ? '+' : '' }}{{ tasksTrend }}% from last week
          </div>
        </div>

        <div class="overview-card">
          <h3>Study Streak</h3>
          <div class="stat">
            <span class="number">{{ studyStreak }}</span>
            <span class="label">days</span>
          </div>
          <div class="trend" :class="{ positive: streakTrend > 0 }">
            {{ streakTrend > 0 ? '+' : '' }}{{ streakTrend }}% from last week
          </div>
        </div>
      </div>

      <div class="progress-details">
        <div class="section">
          <h2>Study Schedule</h2>
          <div class="schedule-chart">
            <!-- Add chart component here -->
            <div class="placeholder-chart">
              Study hours distribution chart will be displayed here
            </div>
          </div>
        </div>

        <div class="section">
          <h2>Recent Activities</h2>
          <div class="activities-list">
            <div v-if="loading" class="loading">
              Loading activities...
            </div>
            <div v-else-if="activities.length === 0" class="no-activities">
              No recent activities found.
            </div>
            <div v-else class="activity-items">
              <div v-for="activity in activities" :key="activity.id" class="activity-item">
                <div class="activity-icon" :class="activity.type">
                  <i :class="getActivityIcon(activity.type)"></i>
                </div>
                <div class="activity-details">
                  <h4>{{ activity.title }}</h4>
                  <p>{{ activity.description }}</p>
                  <span class="activity-time">{{ formatTime(activity.timestamp) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="section">
          <h2>Goals</h2>
          <div class="goals-list">
            <div v-if="loading" class="loading">
              Loading goals...
            </div>
            <div v-else-if="goals.length === 0" class="no-goals">
              No goals set yet.
            </div>
            <div v-else class="goal-items">
              <div v-for="goal in goals" :key="goal.id" class="goal-item">
                <div class="goal-info">
                  <h4>{{ goal.title }}</h4>
                  <p>{{ goal.description }}</p>
                  <div class="goal-progress">
                    <div class="progress-bar">
                      <div 
                        class="progress-fill"
                        :style="{ width: `${goal.progress}%` }"
                      ></div>
                    </div>
                    <span class="progress-text">{{ goal.progress }}%</span>
                  </div>
                </div>
                <button 
                  v-if="goal.progress < 100"
                  @click="updateGoalProgress(goal)"
                  class="update-button"
                >
                  Update Progress
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Progress',
  data() {
    return {
      loading: false,
      totalStudyHours: 0,
      studyHoursTrend: 0,
      completedTasks: 0,
      tasksTrend: 0,
      studyStreak: 0,
      streakTrend: 0,
      activities: [],
      goals: []
    }
  },
  methods: {
    async fetchProgressData() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/api/progress', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        const data = response.data
        this.totalStudyHours = data.totalStudyHours
        this.studyHoursTrend = data.studyHoursTrend
        this.completedTasks = data.completedTasks
        this.tasksTrend = data.tasksTrend
        this.studyStreak = data.studyStreak
        this.streakTrend = data.streakTrend
        this.activities = data.activities
        this.goals = data.goals
      } catch (error) {
        console.error('Error fetching progress data:', error)
        alert('Failed to fetch progress data. Please try again.')
      } finally {
        this.loading = false
      }
    },
    getActivityIcon(type) {
      const icons = {
        study: 'fas fa-book',
        task: 'fas fa-tasks',
        group: 'fas fa-users',
        resource: 'fas fa-file'
      }
      return icons[type] || 'fas fa-circle'
    },
    formatTime(timestamp) {
      return new Date(timestamp).toLocaleString()
    },
    async updateGoalProgress(goal) {
      try {
        const newProgress = prompt('Enter new progress (0-100):', goal.progress)
        if (newProgress === null) return

        const progress = parseInt(newProgress)
        if (isNaN(progress) || progress < 0 || progress > 100) {
          alert('Please enter a valid number between 0 and 100')
          return
        }

        await axios.put(`http://localhost:8080/api/goals/${goal.id}/progress`, {
          progress
        }, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })

        goal.progress = progress
        alert('Goal progress updated successfully!')
      } catch (error) {
        console.error('Error updating goal progress:', error)
        alert('Failed to update goal progress. Please try again.')
      }
    }
  },
  mounted() {
    this.fetchProgressData()
  }
}
</script>

<style scoped>
.progress-container {
  max-width: 1920px;
  margin: 0 auto;
  padding: 2rem;
}

.header {
  text-align: center;
  margin-bottom: 3rem;
}

.header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 1rem;
}

.header p {
  font-size: 1.2rem;
  color: #666;
}

.progress-content {
  display: grid;
  gap: 2rem;
}

.progress-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.overview-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.overview-card h3 {
  font-size: 1.5rem;
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1rem;
}

.stat .number {
  font-size: 3rem;
  font-weight: 600;
  color: #4CAF50;
}

.stat .label {
  font-size: 1.2rem;
  color: #666;
  margin-top: 0.5rem;
}

.trend {
  font-size: 1.1rem;
  color: #dc3545;
}

.trend.positive {
  color: #4CAF50;
}

.progress-details {
  display: grid;
  gap: 2rem;
}

.section {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section h2 {
  font-size: 1.8rem;
  color: #2c3e50;
  margin-bottom: 2rem;
}

.schedule-chart {
  height: 300px;
  background: #f8f9fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-chart {
  color: #666;
  font-size: 1.2rem;
}

.activities-list,
.goals-list {
  margin-top: 1rem;
}

.loading,
.no-activities,
.no-goals {
  text-align: center;
  padding: 2rem;
  color: #666;
  font-size: 1.2rem;
}

.activity-items,
.goal-items {
  display: grid;
  gap: 1rem;
}

.activity-item {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #4CAF50;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.activity-icon.study { background-color: #4CAF50; }
.activity-icon.task { background-color: #2196F3; }
.activity-icon.group { background-color: #9C27B0; }
.activity-icon.resource { background-color: #FF9800; }

.activity-details {
  flex: 1;
}

.activity-details h4 {
  font-size: 1.2rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.activity-details p {
  color: #666;
  margin-bottom: 0.5rem;
}

.activity-time {
  font-size: 0.9rem;
  color: #999;
}

.goal-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 8px;
  align-items: center;
}

.goal-info {
  flex: 1;
}

.goal-info h4 {
  font-size: 1.2rem;
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.goal-info p {
  color: #666;
  margin-bottom: 1rem;
}

.goal-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #4CAF50;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 0.9rem;
  color: #666;
  min-width: 45px;
}

.update-button {
  padding: 0.75rem 1.5rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.update-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

@media (max-width: 1200px) {
  .progress-container {
    padding: 1.5rem;
  }
}

@media (max-width: 768px) {
  .progress-container {
    padding: 1rem;
  }

  .header h1 {
    font-size: 2rem;
  }

  .section {
    padding: 1.5rem;
  }

  .goal-item {
    flex-direction: column;
    align-items: stretch;
  }

  .update-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .header h1 {
    font-size: 1.75rem;
  }

  .section {
    padding: 1.25rem;
  }

  .overview-card {
    padding: 1.5rem;
  }

  .stat .number {
    font-size: 2.5rem;
  }
}
</style> 