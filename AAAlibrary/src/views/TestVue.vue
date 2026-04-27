<template>
    <div class="user-statistics-container">
      <el-card class="box-card">
        <template #header>
          <div class="card-header">
            <span>用户活跃排行榜</span>
            <div>
              <el-button :type="viewMode === 'table' ? 'primary' : ''" @click="viewMode = 'table'">
                表格视图
              </el-button>
              <el-button :type="viewMode === 'chart' ? 'primary' : ''" @click="viewMode = 'chart'">
                图表视图
              </el-button>
              <el-button type="success" @click="refreshData">刷新数据</el-button>
            </div>
          </div>
        </template>
   
        <!-- 表格视图 -->
        <div v-if="viewMode === 'table'">
          <el-table
            :data="userList"
            stripe
            style="width: 100%"
            :default-sort="{ prop: 'activityCount', order: 'descending' }"
          >
            <el-table-column type="index" label="排名" width="80" align="center">
              <template #default="scope">
                <el-tag :type="getRankType(scope.$index + 1)">
                  {{ scope.$index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
   
            <el-table-column prop="userName" label="用户名" min-width="150">
              <template #default="scope">
                <span class="user-name">{{ scope.row.userName }}</span>
              </template>
            </el-table-column>
   
            <el-table-column prop="activityCount" label="活跃次数" width="120" sortable>
              <template #default="scope">
                <el-tag effect="dark" type="warning"> {{ scope.row.activityCount }} 次 </el-tag>
              </template>
            </el-table-column>
   
            <el-table-column label="活跃度进度条" width="200">
              <template #default="scope">
                <el-progress
                  :percentage="getActivityPercentage(scope.row.activityCount)"
                  :color="getProgressColor(scope.row.activityCount)"
                  :show-text="false"
                />
              </template>
            </el-table-column>
          </el-table>
        </div>
   
        <!-- 图表视图 -->
        <div v-else-if="viewMode === 'chart' && userList.length > 0" class="chart-container">
          <div ref="chartRef" style="width: 100%; height: 400px"></div>
        </div>
   
        <!-- 空状态 -->
        <el-empty v-else description="暂无数据" />
      </el-card>
    </div>
  </template>
   
  <script setup>
  import { ref, onMounted, watch, nextTick } from 'vue'
  import * as echarts from 'echarts'
  import { userStaticStore } from '@/stores/userStatic'
   
  // 创建实例
  const userStatisticsStore = userStaticStore()
   
  // 导入getUserStatistics方法
  import { userCountService } from '@/api/static'
   
  const fetchUserStatistics = async () => {
    try {
      const res = await userCountService()
      userStatisticsStore.userList = res.data
      console.log('获取数据成功:', res.data)
    } catch (error) {
      console.error('获取数据失败:', error)
    }
  }
   
  // 响应式数据
  const viewMode = ref('table') // 'table' 或 'chart'
  const chartRef = ref(null)
  let chartInstance = null
   
  // 计算属性获取用户列表
  const userList = ref([])
   
  // 初始化数据
  onMounted(() => {
    fetchUserStatistics()
  })
   
  // 监听store中的数据变化
  watch(
    () => userStatisticsStore.userList,
    (newList) => {
      userList.value = newList
      console.log('用户列表已更新:', newList)
   
      if (viewMode.value === 'chart') {
        nextTick(() => {
          initChart()
        })
      }
    },
    { immediate: true },
  )
   
  // 监听视图模式变化
  watch(viewMode, (newVal) => {
    if (newVal === 'chart' && userList.value.length > 0) {
      nextTick(() => {
        initChart()
      })
    }
  })
   
  // 方法
  const refreshData = () => {
    fetchUserStatistics()
  }
   
  const getRankType = (rank) => {
    if (rank === 1) return 'danger'
    if (rank === 2) return 'warning'
    if (rank === 3) return 'success'
    return ''
  }
   
  const getActivityPercentage = (count) => {
    if (userList.value.length === 0) return 0
    const maxCount = Math.max(...userList.value.map((user) => user.activityCount))
    return Math.round((count / maxCount) * 100)
  }
   
  const getProgressColor = (count) => {
    const percentage = getActivityPercentage(count)
    if (percentage >= 80) return '#f56c6c'
    if (percentage >= 60) return '#e6a23c'
    if (percentage >= 40) return '#5cb87a'
    if (percentage >= 20) return '#6f7ad3'
    return '#909399'
  }
   
  // 初始化图表
  const initChart = () => {
    if (!chartRef.value) return
   
    // 销毁旧实例
    if (chartInstance) {
      chartInstance.dispose()
    }
   
    // 创建新实例
    chartInstance = echarts.init(chartRef.value)
   
    // 准备图表数据
    const chartData = userList.value.slice(0, 10).map((user, index) => ({
      name: user.userName,
      value: user.activityCount,
      itemStyle: {
        color: getChartColor(index),
      },
    }))
   
    const option = {
      title: {
        text: '用户活跃度Top10',
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
        formatter: '{b}: {c} 次活跃',
      },
      xAxis: {
        type: 'category',
        data: chartData.map((item) => item.name),
        axisLabel: {
          rotate: 45,
          interval: 0,
        },
      },
      yAxis: {
        type: 'value',
        name: '活跃次数',
      },
      series: [
        {
          name: '活跃次数',
          type: 'bar',
          data: chartData,
          itemStyle: {
            color: (params) => {
              return getChartColor(params.dataIndex)
            },
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c} 次',
          },
        },
      ],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        top: '15%',
        containLabel: true,
      },
    }
   
    chartInstance.setOption(option)
   
    // 响应式调整
    window.addEventListener('resize', () => {
      chartInstance.resize()
    })
  }
   
  // 获取图表颜色
  const getChartColor = (index) => {
    const colors = [
      '#c23531',
      '#2f4554',
      '#61a0a8',
      '#d48265',
      '#91c7ae',
      '#749f83',
      '#ca8622',
      '#bda29a',
      '#6e7074',
      '#546570',
    ]
    return colors[index % colors.length]
  }
   
  // 组件卸载时清理
  import { onUnmounted } from 'vue'
  onUnmounted(() => {
    if (chartInstance) {
      chartInstance.dispose()
    }
  })
  </script>
   
  <style scoped>
  .user-statistics-container {
    padding: 20px;
  }
   
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
   
  .chart-container {
    padding: 10px;
  }
   
  .user-name {
    font-weight: 500;
  }
   
  :deep(.el-table .cell) {
    display: flex;
    align-items: center;
  }
  </style>
   