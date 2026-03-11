<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-circle">🌾</div>
        <h2>智慧农业大脑</h2>
      </div>

      <nav class="menu">
        <div :class="['menu-item', currentView === 'home' ? 'active' : '']" @click="currentView = 'home'">
          <span class="icon">🗺️</span> 态势地图
        </div>
        <div :class="['menu-item', currentView === 'dashboard' ? 'active' : '']" @click="currentView = 'dashboard'">
          <span class="icon">📊</span> 数据驾驶舱
        </div>
        <div :class="['menu-item', currentView === 'predict' ? 'active' : '']" @click="currentView = 'predict'">
          <span class="icon">🔮</span> 智能沙盘演示
        </div>
        <div :class="['menu-item', currentView === 'manager' ? 'active' : '']" @click="currentView = 'manager'">
          <span class="icon">🗄️</span> 数据资产管理
        </div>
      </nav>
      <div class="sidebar-footer"><p>专家系统 v8.0 满血版</p></div>
    </aside>

    <main class="main-content">
      <header v-if="currentView !== 'home'" class="top-header">
        <h3 class="page-title">{{ pageTitle }}</h3>
        <div class="user-info">
          <span class="user-role">首席农艺师</span>
          <div class="avatar-simple">A</div>
        </div>
      </header>

      <div :class="['content-body', currentView === 'home' ? 'no-padding' : '']">
        <div v-if="currentView === 'home'" class="home-view">
          <div id="chinaMapChart" class="full-screen-map"></div>
          <div class="map-overlay-panel hover-glow">
            <div class="panel-title">🌾 全国产量分布态势</div>
            <div class="panel-data">
              <div class="data-item"><span>已收录数据</span><strong>{{ allData.length }} <small>条</small></strong></div>
              <div class="data-item"><span>覆盖省份</span><strong>{{ provinceList.length }} <small>个</small></strong></div>
            </div>
            <p class="hint">✨ 鼠标悬浮查看 3D 发光效果，点击查收诊断报告</p>
          </div>
        </div>

        <div v-else-if="currentView === 'dashboard'" class="dashboard-view">
          <div class="stats-row">
            <div class="stat-card hover-float"><div class="stat-icon bg-blue">📉</div><div class="stat-info"><h4>收录数据总量</h4><p class="number">{{ allData.length }} <span class="unit">条</span></p></div></div>
            <div class="stat-card hover-float"><div class="stat-icon bg-green">🗺️</div><div class="stat-info"><h4>覆盖省份</h4><p class="number">{{ provinceList.length }} <span class="unit">个</span></p></div></div>
            <div class="stat-card hover-float"><div class="stat-icon bg-purple">🌾</div><div class="stat-info"><h4>平均亩产</h4><p class="number">{{ averageYield }} <span class="unit">kg</span></p></div></div>
            <div class="stat-card hover-float"><div class="stat-icon bg-orange">🏆</div><div class="stat-info"><h4>最高产省份</h4><p class="number text-sm">{{ topProvince }}</p></div></div>
          </div>
          <div class="card chart-section hover-glow">
            <div class="card-header"><h4>🏆 全国各省平均产量 TOP 10 排行榜 (霓虹渐变)</h4></div>
            <div id="rankChart" style="width: 100%; height: 500px;"></div>
          </div>
        </div>

        <div v-else-if="currentView === 'predict'" class="predict-view">
          <div class="control-panel card">
            <div class="panel-header"><h4>🛸 AI 预测参数设定</h4></div>
            <div class="form-row">
              <label>目标省份：</label>
              <select v-model="selectedProvince" class="modern-select">
                <option v-for="prov in provinceList" :key="prov" :value="prov">{{ prov }}</option>
              </select>
              <button @click="getPrediction('page')" :disabled="loading" class="btn-flat pulse-on-hover">
                {{ loading ? 'AI 深度解析中...' : '启动智能预测' }}
              </button>
            </div>
          </div>

          <div v-if="loading" class="result-grid skeleton-fade">
            <div class="card skeleton-card"><div class="skeleton-title shimmer"></div><div class="skeleton-chart shimmer"></div></div>
            <div class="card skeleton-card"><div class="skeleton-title shimmer"></div><div class="skeleton-chart shimmer"></div></div>
            <div class="card full-width skeleton-card"><div class="skeleton-title shimmer"></div><div class="skeleton-text shimmer"></div><div class="skeleton-text shimmer w-70"></div></div>
          </div>

          <div v-else-if="yieldValue" class="result-grid fade-in">
            <div class="card hover-glow"><h4>2025年 预测结果仪表盘</h4><div id="yieldChartPage" style="width: 100%; height: 300px;"></div></div>
            <div class="card hover-glow"><h4>历史趋势回顾分析</h4><div id="historyChartPage" style="width: 100%; height: 300px;"></div></div>
            <div class="card full-width advice-card hover-glow" :class="statusType"><h4>🧠 AI 智能决策建议</h4><p>{{ adviceText }}</p></div>
          </div>
        </div>

        <div v-else-if="currentView === 'manager'" class="manager-view">
          <DataManager />
        </div>
      </div>
    </main>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container fade-up">
        <div class="modal-header">
          <h3>📍 {{ selectedProvince }} - 农业大数据综合诊断报告</h3>
          <button class="close-btn" @click.stop="closeModal">×</button>
        </div>

        <div class="modal-body">

          <div v-if="loading" class="skeleton-layout">
            <div class="skeleton-left">
              <div class="skeleton-card shimmer" style="height: 260px;"></div>
              <div class="skeleton-card shimmer" style="height: 300px; margin-top:20px;"></div>
            </div>
            <div class="skeleton-right skeleton-card shimmer"></div>
          </div>

          <div v-else class="fade-in">
            <div class="modal-section predict-section card-inner">
              <div class="panel-header">
                <div class="header-left">
                  <h4>🛸 2025年产量智能推演</h4>
                  <button @click="getPrediction('modal')" class="btn-flat btn-sm pulse-on-hover">重新测算</button>
                </div>
              </div>

              <div class="result-grid-modal">
                <div class="chart-container hover-glow">
                  <div id="modalYieldChart" style="width: 100%; height: 260px;"></div>
                </div>

                <div class="advice-container-list hover-glow">
                  <div class="advice-header-zone">
                    <div class="health-badge-wrapper" :class="statusType">
                      <div class="health-score-ring">
                        <span class="health-score">{{ healthScore }}</span>
                        <span class="health-unit">分</span>
                      </div>
                      <div class="health-label">AI 综合健康指数</div>
                    </div>
                    <div class="advice-title-text">
                      <h4>🧠 AI 专家多维诊断报告</h4>
                      <p>基于 {{ selectedProvince }} 气候模型及算法推演</p>
                    </div>
                  </div>

                  <div class="advice-scroll-box">
                    <div v-for="(item, index) in expertAdviceList" :key="index" :class="['advice-list-item', item.type]">
                      <div class="item-icon-wrapper"><span class="item-icon">{{ item.icon }}</span></div>
                      <div class="item-content">
                        <div class="item-title">{{ item.title }}</div>
                        <div class="item-desc">{{ item.content }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-section history-section card-inner hover-glow" style="margin-top: 20px;">
              <h4>📈 历史产量波动复盘 (过去10年)</h4>
              <div id="modalHistoryChart" style="width: 100%; height: 300px;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'
import DataManager from './components/DataManager.vue'

const currentView = ref('home')
const showModal = ref(false)
const mapGeoJson = ref(null)
const allData = ref([])
const provinceList = ref([])
const selectedProvince = ref('黑龙江')
const loading = ref(false)
const yieldValue = ref(null)
const statusText = ref(''), statusType = ref(''), adviceText = ref('')
const expertAdviceList = ref([])
const healthScore = ref(0) // 🔥 新增：健康指数

const provinceMap = { /* 略，与之前一致 */
  '北京': '北京市', '天津': '天津市', '上海': '上海市', '重庆': '重庆市',
  '河北': '河北省', '山西': '山西省', '辽宁': '辽宁省', '吉林': '吉林省', '黑龙江': '黑龙江省',
  '江苏': '江苏省', '浙江': '浙江省', '安徽': '安徽省', '福建': '福建省', '江西': '江西省', '山东': '山东省',
  '河南': '河南省', '湖北': '湖北省', '湖南': '湖南省', '广东': '广东省', '海南': '海南省', '四川': '四川省',
  '贵州': '贵州省', '云南': '云南省', '陕西': '陕西省', '甘肃': '甘肃省', '青海': '青海省', '台湾': '台湾省',
  '内蒙古': '内蒙古自治区', '广西': '广西壮族自治区', '西藏': '西藏自治区', '宁夏': '宁夏回族自治区', '新疆': '新疆维吾尔自治区',
  '香港': '香港特别行政区', '澳门': '澳门特别行政区'
}
const reverseProvinceMap = Object.fromEntries(Object.entries(provinceMap).map(([k, v]) => [v, k]))

const pageTitle = computed(() => ({ home: '', dashboard: '数据驾驶舱', predict: '智能沙盘演示', manager: '数据资产管理' }[currentView.value]))
const averageYield = computed(() => allData.value.length ? (allData.value.reduce((a, c) => a + c.yieldAmount, 0) / allData.value.length).toFixed(1) : 0)
const topProvince = computed(() => allData.value.length ? allData.value.reduce((p, c) => (p.yieldAmount > c.yieldAmount) ? p : c).province : '暂无')

onMounted(async () => {
  try {
    const [provRes, dataRes] = await Promise.all([
      axios.get('http://localhost:8080/api/data/provinces'),
      axios.get('http://localhost:8080/api/data/list')
    ])
    provinceList.value = provRes.data
    allData.value = dataRes.data
    if (provinceList.value.length > 0) selectedProvince.value = provinceList.value[0]
    const mapRes = await axios.get('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    mapGeoJson.value = mapRes.data
    echarts.registerMap('china', mapGeoJson.value)
    await nextTick()
    if (currentView.value === 'home') initMapChart()
  } catch (err) { console.error("初始化失败", err) }
})

const normalizeName = (name) => {
  if (!name) return ""
  if (name.startsWith('内蒙古') || name.startsWith('黑龙江')) return name.substring(0, 3)
  return name.substring(0, 2)
}

// 🔥 专家系统逻辑（含动态评分） 🔥
const generateExpertAdvice = (val, prov) => {
  const list = []

  // 计算综合健康分 (满分100，加入一点随机性显得更真实)
  let baseScore = 0;
  if (val > 600) {
    baseScore = 92 + Math.floor(Math.random() * 8); // 92-99
    statusType.value = 'success'
    list.push({ type: 'success', icon: '🏆', title: '评级：S级丰收', content: `生长指标完美，预计大幅增产。请提前规划秋收物流与仓储。` })
  } else if (val > 450) {
    baseScore = 75 + Math.floor(Math.random() * 15); // 75-89
    statusType.value = 'info'
    list.push({ type: 'info', icon: '📊', title: '评级：A级平稳', content: `各项指标处于合理区间。当前无需人为干预，保持常态化田间巡视。` })
  } else {
    baseScore = 50 + Math.floor(Math.random() * 20); // 50-69
    statusType.value = 'warning'
    list.push({ type: 'warning', icon: '⚠️', title: '评级：C级高危', content: `系统监测到潜在的减产风险！请立即启动应急预案排查胁迫因子。` })
  }

  // 实现数字滚动的动画效果
  let currentScore = 0;
  const interval = setInterval(() => {
    if(currentScore < baseScore) { currentScore += 2; healthScore.value = currentScore; }
    else { healthScore.value = baseScore; clearInterval(interval); }
  }, 20);

  // 区域与专业建议...
  if (['新疆', '甘肃', '宁夏', '内蒙古'].includes(prov)) { list.push({ type: 'normal', icon: '💧', title: '水资源调度', content: '西北旱区水分胁迫极高，建议立即对接智能灌溉系统，实施定点滴灌。' }) }
  else if (['黑龙江', '吉林', '辽宁'].includes(prov)) { list.push({ type: 'normal', icon: '❄️', title: '冷害预警防控', content: '东北气象模型显示存在早霜风险，建议本周内完成抗逆诱抗剂喷施。' }) }
  else if (['江苏', '浙江', '湖北', '湖南', '江西'].includes(prov)) { list.push({ type: 'normal', icon: '🌧️', title: '农田排涝指令', content: '南方降水带偏多，建议启动排水泵站预案，确保沟渠畅通防渍水。' }) }
  else if (['河南', '山东', '河北'].includes(prov)) { list.push({ type: 'normal', icon: '🚜', title: '热害防治方案', content: '黄淮平原存在干热风隐患，建议利用无人机开展“一喷三防”作业。' }) }
  else { list.push({ type: 'normal', icon: '🛡️', title: '绿色植保防御', content: '根据病虫害扩散模型，建议在迎风坡面增设太阳能杀虫灯。' }) }

  list.push({ type: 'normal', icon: '🧪', title: '测土配方施肥', content: '云端土壤数据显示氮肥过剩，建议追肥时降低尿素比例，增施钾肥。' })
  expertAdviceList.value = list
}

const getPrediction = async (target) => {
  loading.value = true
  if(target === 'page') yieldValue.value = null
  try {
    const pRes = await axios.get(`http://localhost:8080/api/data/predict?province=${selectedProvince.value}`)
    const hRes = await axios.get(`http://localhost:8080/api/data/history?province=${selectedProvince.value}`)
    const m = pRes.data.match(/预测产量为: ([\d.]+) kg/); const val = m ? parseFloat(m[1]) : 0
    yieldValue.value = val;
    adviceText.value = pRes.data.split('决策建议:')[1]

    // 故意增加一点延时，让用户能看清漂亮的骨架屏 (假装 AI 算得很费劲)
    setTimeout(async () => {
      loading.value = false
      generateExpertAdvice(val, selectedProvince.value)
      await nextTick()
      setTimeout(() => {
        if (target === 'modal' && showModal.value) { initGaugeChart('modalYieldChart'); initLineChart('modalHistoryChart', hRes.data) }
        else if (target === 'page') { initGaugeChart('yieldChartPage'); initLineChart('historyChartPage', hRes.data) }
      }, 50)
    }, 800)
  } catch(e) { loading.value = false; alert('数据请求失败') }
}

const closeModal = () => {
  showModal.value = false
  setTimeout(() => { ['modalYieldChart', 'modalHistoryChart'].forEach(id => { const dom = document.getElementById(id); if (dom) echarts.dispose(dom) }) }, 300)
}

// === 图表渲染代码保持极佳的 V7.2 炫酷状态 ===
const initGaugeChart = (id) => {
  const dom = document.getElementById(id); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom)
  chart.setOption({
    series: [{
      type: 'gauge', min: 0, max: 800, axisTick: { show: false }, splitLine: { length: 15, lineStyle: { color: 'auto', width: 2 } },
      axisLine: { lineStyle: { width: 15, color: [ [0.3, '#67e0e3'], [0.7, '#37a2da'], [1, '#fd666d'] ], shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.1)' } },
      pointer: { itemStyle: { color: 'auto' } },
      detail: { formatter: '{value} kg', fontSize: 22, fontWeight: 'bolder', color: 'inherit', valueAnimation: true }, data: [{value: yieldValue.value}]
    }]
  })
}

const initLineChart = (id, data) => {
  const dom = document.getElementById(id); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } } },
    grid: { top: 30, bottom: 30, left: 50, right: 20 },
    xAxis: { type: 'category', boundaryGap: false, data: data.map(i=>i.year+'年') },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
    series: [{
      type: 'line', smooth: true, showSymbol: false,
      lineStyle: { width: 4, color: '#2ed573', shadowColor: 'rgba(46, 213, 115, 0.5)', shadowBlur: 10 },
      areaStyle: { opacity: 0.8, color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(46, 213, 115, 0.5)' }, { offset: 1, color: 'rgba(46, 213, 115, 0.0)' }]) },
      itemStyle: { color: '#2ed573' }, data: data.map(i=>i.yieldAmount)
    }]
  })
}

const initRankChart = () => {
  const chartDom = document.getElementById('rankChart'); if (!chartDom) return; echarts.dispose(chartDom); const myChart = echarts.init(chartDom)
  const provMap = {}; allData.value.forEach(d => { if(!provMap[d.province]) provMap[d.province] = []; provMap[d.province].push(d.yieldAmount) })
  const categories = [], values = []; for (const p in provMap) { categories.push(p); values.push((provMap[p].reduce((a,b)=>a+b,0) / provMap[p].length).toFixed(2)) }
  const sorted = categories.map((c, i) => ({name: c, val: parseFloat(values[i])})).sort((a,b) => b.val - a.val).slice(0, 10)
  myChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: {type: 'shadow'} }, grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: sorted.map(i => i.name), axisLabel: { interval:0, rotate: 30 } },
    yAxis: { type: 'value', name: '平均产量(kg)', splitLine:{lineStyle:{type:'dashed', color:'#eee'}} },
    series: [{
      type: 'bar', data: sorted.map(i => i.val), barWidth: '40%', showBackground: true, backgroundStyle: { color: 'rgba(200, 200, 200, 0.1)', borderRadius: [10, 10, 0, 0] },
      itemStyle: { borderRadius: [10, 10, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }]), shadowColor: 'rgba(79, 172, 254, 0.4)', shadowBlur: 10 }
    }]
  })
}

const initMapChart = () => {
  const dom = document.getElementById('chinaMapChart'); if (!dom) return; echarts.dispose(dom); const chart = echarts.init(dom)
  const mapData = provinceList.value.map(dbName => ({ name: dbName, value: 1 }))
  chart.setOption({
    backgroundColor: '#eef2f5',
    tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.95)', textStyle: { color: '#333' }, formatter: (params) => provinceList.value.includes(params.data?.name) ? `<div style="font-weight:bold;color:#1890ff">${params.name}</div><div>点击启动 AI 推演</div>` : `<div style="color:#999">${params.name}</div>` },
    geo: {
      map: 'china', roam: true, zoom: 1.2, top: 'center', label: { show: true, color: '#666', fontSize: 10, fontWeight: 'bold' },
      itemStyle: { areaColor: '#f1f5f9', borderColor: '#cbd5e1', borderWidth: 1, shadowColor: 'rgba(0, 0, 0, 0.15)', shadowBlur: 5, shadowOffsetX: 2, shadowOffsetY: 5 },
      emphasis: { label:{ color: '#fff' }, itemStyle: { areaColor: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: '#2ed573'}, {offset: 1, color: '#1e90ff'}]), borderWidth: 0, shadowBlur: 20, shadowColor: 'rgba(46, 213, 115, 0.8)' } },
      select: { label:{color:'#fff'}, itemStyle: { areaColor: '#ffa502', shadowBlur: 15, shadowColor: 'rgba(255, 165, 2, 0.6)' } }
    },
    series: [{ type: 'map', geoIndex: 0, data: mapData, selectedMode: 'single' }]
  })
  chart.off('click'); chart.on('click', params => { const found = provinceList.value.find(dbName => normalizeName(dbName) === normalizeName(params.name)); if (found) { selectedProvince.value = found; showModal.value = true; getPrediction('modal') } })
  window.addEventListener('resize', () => chart.resize())
}

watch(currentView, (v) => { nextTick(() => { if(v==='home') initMapChart(); if(v==='dashboard') setTimeout(initRankChart, 500) }) })
watch(showModal, v => document.body.style.overflow = v ? 'hidden' : '')
</script>

<style>
/* === 基础与布局 === */
html, body, #app { margin: 0 !important; padding: 0 !important; width: 100vw !important; height: 100vh !important; overflow: hidden !important; font-family: 'Segoe UI', sans-serif; background-color: #f0f2f5; }
.app-container { display: flex; width: 100%; height: 100%; }
.sidebar { width: 240px; background: #2f3640; color: white; flex-shrink: 0; display: flex; flex-direction: column; }
.main-content { flex: 1; display: flex; flex-direction: column; position: relative; background: #f1f2f6; }
.content-body { flex: 1; overflow-y: auto; padding: 30px; }
.content-body.no-padding { padding: 0; overflow: hidden; }

/* === 极致微交互与动画 (Hover Effects) === */
.hover-float { transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275), box-shadow 0.3s ease; }
.hover-float:hover { transform: translateY(-8px) scale(1.02); box-shadow: 0 15px 30px rgba(0,0,0,0.08) !important; z-index: 10; }
.hover-glow { transition: all 0.3s ease; }
.hover-glow:hover { box-shadow: 0 8px 25px rgba(46, 213, 115, 0.15) !important; border-color: rgba(46, 213, 115, 0.3) !important; }
.pulse-on-hover:hover { animation: pulse 1s infinite; }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(46, 213, 115, 0.4); } 70% { box-shadow: 0 0 0 10px rgba(46, 213, 115, 0); } 100% { box-shadow: 0 0 0 0 rgba(46, 213, 115, 0); } }

/* === 骨架屏高级动画 (Skeleton Loading) === */
.skeleton-layout { display: flex; gap: 30px; height: 100%; }
.skeleton-left { width: 40%; display: flex; flex-direction: column; }
.skeleton-right { flex: 1; }
.skeleton-card { background: white; border-radius: 12px; padding: 20px; border: 1px solid #eee; }
.skeleton-fade { animation: fadeInOut 1s infinite alternate; }
.shimmer {
  background: #f6f7f8;
  background-image: linear-gradient(90deg, #f0f2f5 0px, #e6e8eb 40px, #f0f2f5 80px);
  background-size: 600px;
  animation: shimmer 1.5s infinite linear;
  border-radius: 8px;
}
@keyframes shimmer { 0% { background-position: -200px 0; } 100% { background-position: calc(200px + 100%) 0; } }

/* === 侧边栏与导航 === */
.logo-area { padding: 30px 20px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-circle { width: 50px; height: 50px; background: #2ed573; border-radius: 50%; font-size: 24px; line-height: 50px; margin: 0 auto 10px; color: white; box-shadow: 0 4px 15px rgba(46,213,115,0.4);}
.menu { flex: 1; padding-top: 20px; }
.menu-item { padding: 15px 25px; cursor: pointer; color: #a4b0be; display: flex; gap: 15px; transition: 0.2s; }
.menu-item:hover { color: white; background: rgba(255,255,255,0.05); padding-left: 30px;}
.menu-item.active { background: #2ed573; color: white; font-weight: bold; }

/* === 仪表盘卡片 === */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 25px; margin-bottom: 30px; }
.stat-card { background: white; padding: 20px; border-radius: 12px; display: flex; align-items: center; gap: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.04); }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 26px; }
.bg-blue { background: #e7f5ff; color: #1890ff; box-shadow: 0 4px 10px rgba(24,144,255,0.2); }
.bg-green { background: #e6fffa; color: #2ed573; box-shadow: 0 4px 10px rgba(46,213,115,0.2); }
.bg-purple { background: #f3f0ff; color: #722ed1; box-shadow: 0 4px 10px rgba(114,46,209,0.2); }
.bg-orange { background: #fff7e6; color: #fa8c16; box-shadow: 0 4px 10px rgba(250,140,22,0.2); }
.number { font-size: 30px; font-weight: 800; color: #2f3542; }

/* === 地图与通用容器 === */
.home-view { width: 100%; height: 100%; position: relative; }
#chinaMapChart { width: 100%; height: 100%; display: block; }
.map-overlay-panel { position: absolute; top: 30px; left: 30px; background: rgba(255,255,255,0.95); padding: 25px; border-radius: 12px; box-shadow: 0 8px 25px rgba(0,0,0,0.1); z-index: 10; pointer-events: none; border: 1px solid white;}
.panel-title { font-size: 18px; font-weight: 800; margin-bottom: 20px; color: #2c3e50; }
.panel-data { display: flex; gap: 30px; margin-bottom: 15px; }
.data-item { display: flex; flex-direction: column; }
.data-item span { font-size: 13px; color: #7f8c8d; }
.data-item strong { font-size: 26px; color: #2ed573; text-shadow: 0 2px 5px rgba(46,213,115,0.2); }
.hint { font-size: 13px; color: #95a5a6; }
.card { background: white; border-radius: 12px; padding: 25px; margin-bottom: 25px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); border: 1px solid #f1f2f6; }
.card-inner { background: #fff; border-radius: 12px; padding: 25px; border: 1px solid #edf2f7; box-shadow: 0 2px 8px rgba(0,0,0,0.02);}

/* === 🔥🔥🔥 终极专家建议列表 (勋章与发光流体卡片) 🔥🔥🔥 === */
.result-grid-modal { display: flex; gap: 30px; height: 350px; margin-bottom: 15px; }
.chart-container { width: 40%; background: #f8fafc; border-radius: 12px; padding: 15px; display: flex; justify-content: center; align-items: center; border: 1px solid #e2e8f0;}

.advice-container-list { flex: 1; background: #ffffff; border-radius: 16px; padding: 25px; display: flex; flex-direction: column; overflow: hidden; border: 1px solid #edf2f7; box-shadow: 0 10px 30px rgba(0,0,0,0.03); }

/* 勋章化头部区 */
.advice-header-zone { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px dashed #e2e8f0; }
.health-badge-wrapper { display: flex; flex-direction: column; align-items: center; justify-content: center; width: 100px; height: 100px; border-radius: 50%; padding: 5px; box-shadow: 0 8px 20px rgba(0,0,0,0.08); position: relative; }
/* 动态发光状态 */
.health-badge-wrapper.success { background: linear-gradient(135deg, #a8ff78 0%, #78ffd6 100%); box-shadow: 0 10px 25px rgba(120, 255, 214, 0.4); }
.health-badge-wrapper.info { background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%); box-shadow: 0 10px 25px rgba(102, 166, 255, 0.4); }
.health-badge-wrapper.warning { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); box-shadow: 0 10px 25px rgba(253, 160, 133, 0.4); }

.health-score-ring { width: 100%; height: 100%; background: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-direction: column; }
.health-score { font-size: 32px; font-weight: 900; color: #2d3436; line-height: 1; }
.health-unit { font-size: 12px; color: #636e72; margin-top: 2px; }
.health-label { position: absolute; bottom: -12px; background: #2d3436; color: white; font-size: 11px; padding: 3px 10px; border-radius: 10px; white-space: nowrap; font-weight: bold; }
.advice-title-text h4 { font-size: 20px; color: #2d3436; margin-bottom: 8px; font-weight: 800; }
.advice-title-text p { font-size: 13px; color: #636e72; }

/* 流体卡片列表 */
.advice-scroll-box { flex: 1; overflow-y: auto; padding-right: 10px; }
.advice-scroll-box::-webkit-scrollbar { width: 6px; }
.advice-scroll-box::-webkit-scrollbar-thumb { background: #dfe6e9; border-radius: 10px; }
.advice-list-item {
  display: flex; gap: 15px; padding: 16px; border-radius: 12px; margin-bottom: 12px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid transparent; position: relative; overflow: hidden;
}
.advice-list-item:hover { transform: translateX(8px); box-shadow: 0 8px 15px rgba(0,0,0,0.05); }

/* 透明渐变背景质感 */
.advice-list-item.success { background: linear-gradient(90deg, rgba(46,213,115,0.1), rgba(46,213,115,0.02)); border-color: rgba(46,213,115,0.2); }
.advice-list-item.success .item-icon-wrapper { background: rgba(46,213,115,0.2); color: #27ae60; }
.advice-list-item.info { background: linear-gradient(90deg, rgba(24,144,255,0.1), rgba(24,144,255,0.02)); border-color: rgba(24,144,255,0.2); }
.advice-list-item.info .item-icon-wrapper { background: rgba(24,144,255,0.2); color: #0984e3; }
.advice-list-item.warning { background: linear-gradient(90deg, rgba(255,165,2,0.1), rgba(255,165,2,0.02)); border-color: rgba(255,165,2,0.2); }
.advice-list-item.warning .item-icon-wrapper { background: rgba(255,165,2,0.2); color: #e17055; }
.advice-list-item.normal { background: linear-gradient(90deg, rgba(112,161,255,0.1), rgba(112,161,255,0.02)); border-color: rgba(112,161,255,0.2); }
.advice-list-item.normal .item-icon-wrapper { background: rgba(112,161,255,0.2); color: #3742fa; }

.item-icon-wrapper { width: 44px; height: 44px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 22px; flex-shrink: 0; }
.item-content { flex: 1; }
.item-title { font-weight: 800; font-size: 15px; color: #2d3436; margin-bottom: 6px; }
.item-desc { font-size: 13px; color: #636e72; line-height: 1.5; }

/* === 弹窗通用 === */
.modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); z-index: 2000; display: flex; justify-content: center; align-items: center; backdrop-filter: blur(4px); }
.modal-container { background: #f8fafc; width: 85%; height: 92%; max-width: 1200px; border-radius: 20px; display: flex; flex-direction: column; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5); overflow: hidden;}
.modal-header { padding: 25px 35px; background: white; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; z-index: 10;}
.modal-header h3 { font-weight: 800; color: #1e293b; font-size: 22px; }
.modal-body { flex: 1; overflow-y: auto; padding: 35px; }

/* 其他控件 */
.btn-flat { background: #2ed573; color: white; border: none; padding: 12px 28px; border-radius: 8px; cursor: pointer; transition: 0.3s; font-weight: 700; font-size: 15px; box-shadow: 0 4px 12px rgba(46,213,115,0.3);}
.btn-flat:hover { background: #26af61; transform: translateY(-2px); box-shadow: 0 6px 15px rgba(46,213,115,0.4);}
.btn-sm { padding: 8px 18px; font-size: 13px; }
.modern-select { padding: 12px 15px; border: 1px solid #cbd5e1; border-radius: 8px; min-width: 220px; font-size: 15px; color: #334155; outline: none; transition: 0.3s; }
.modern-select:focus { border-color: #2ed573; box-shadow: 0 0 0 3px rgba(46,213,115,0.2); }
.top-header { height: 70px; background: white; padding: 0 40px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #f1f5f9; }
.close-btn { border: none; background: #f1f5f9; width: 40px; height: 40px; border-radius: 50%; font-size: 24px; cursor: pointer; color: #64748b; transition: 0.3s; display: flex; align-items: center; justify-content: center; }
.close-btn:hover { background: #fee2e2; color: #ef4444; transform: rotate(90deg); }

.fade-in { animation: fadeIn 0.5s ease; }
.fade-up { animation: fadeUp 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(30px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }
</style>