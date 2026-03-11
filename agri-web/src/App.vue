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
      <div class="sidebar-footer"><p>专家系统 v7.1</p></div>
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
          <div class="map-overlay-panel">
            <div class="panel-title">🌾 全国产量分布态势</div>
            <div class="panel-data">
              <div class="data-item"><span>已收录数据</span><strong>{{ allData.length }} <small>条</small></strong></div>
              <div class="data-item"><span>覆盖省份</span><strong>{{ provinceList.length }} <small>个</small></strong></div>
            </div>
            <p class="hint">✨ 点击地图高亮省份查看详情</p>
          </div>
        </div>

        <div v-else-if="currentView === 'dashboard'" class="dashboard-view">
          <div class="stats-row">
            <div class="stat-card"><div class="stat-icon bg-blue">📉</div><div class="stat-info"><h4>收录数据总量</h4><p class="number">{{ allData.length }} <span class="unit">条</span></p></div></div>
            <div class="stat-card"><div class="stat-icon bg-green">🗺️</div><div class="stat-info"><h4>覆盖省份</h4><p class="number">{{ provinceList.length }} <span class="unit">个</span></p></div></div>
            <div class="stat-card"><div class="stat-icon bg-purple">🌾</div><div class="stat-info"><h4>平均亩产</h4><p class="number">{{ averageYield }} <span class="unit">kg</span></p></div></div>
            <div class="stat-card"><div class="stat-icon bg-orange">🏆</div><div class="stat-info"><h4>最高产省份</h4><p class="number text-sm">{{ topProvince }}</p></div></div>
          </div>
          <div class="card chart-section">
            <div class="card-header"><h4>🏆 全国各省平均产量 TOP 10 排行榜</h4></div>
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
              <button @click="getPrediction('page')" :disabled="loading" class="btn-flat">
                {{ loading ? 'AI 正在运算中...' : '启动智能预测' }}
              </button>
            </div>
          </div>
          <div v-if="yieldValue" class="result-grid fade-in">
            <div class="card"><h4>2025年 预测结果仪表盘</h4><div id="yieldChartPage" style="width: 100%; height: 300px;"></div></div>
            <div class="card"><h4>历史趋势回顾分析</h4><div id="historyChartPage" style="width: 100%; height: 300px;"></div></div>
            <div class="card full-width advice-card" :class="statusType"><h4>🧠 AI 智能决策建议</h4><p>{{ adviceText }}</p></div>
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
          <h3>📍 {{ selectedProvince }} - 数据洞察与 AI 分析</h3>
          <button class="close-btn" @click.stop="closeModal">×</button>
        </div>

        <div class="modal-body">
          <div v-if="loading" class="loading-state">
            <div class="tech-spinner"></div><p>正在生成多维诊断报告...</p>
          </div>

          <div v-else class="fade-in">
            <div class="modal-section predict-section card-inner">
              <div class="panel-header">
                <div class="header-left">
                  <h4>🛸 2025年产量智能预测</h4>
                  <button @click="getPrediction('modal')" class="btn-flat btn-sm">重新测算</button>
                </div>
              </div>

              <div class="result-grid-modal">
                <div class="chart-container">
                  <div id="modalYieldChart" style="width: 100%; height: 260px;"></div>
                </div>

                <div class="advice-container-list">
                  <h4 class="advice-title">🧠 AI 决策建议报告</h4>
                  <div class="advice-scroll-box">
                    <div v-for="(item, index) in expertAdviceList" :key="index" :class="['advice-list-item', item.type]">
                      <div class="item-icon">{{ item.icon }}</div>
                      <div class="item-content">
                        <div class="item-title">{{ item.title }}</div>
                        <div class="item-desc">{{ item.content }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-section history-section card-inner" style="margin-top: 20px;">
              <h4>📈 历史产量趋势 (过去10年)</h4>
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

// === 状态 ===
const currentView = ref('home')
const showModal = ref(false)
const mapGeoJson = ref(null)
const allData = ref([])
const provinceList = ref([])
const selectedProvince = ref('黑龙江')
const loading = ref(false)
const yieldValue = ref(null)
const statusText = ref(''), statusType = ref(''), adviceText = ref('')
const expertAdviceList = ref([]) // 新增：存储多维建议

// === 映射 ===
const provinceMap = {
  '北京': '北京市', '天津': '天津市', '上海': '上海市', '重庆': '重庆市',
  '河北': '河北省', '山西': '山西省', '辽宁': '辽宁省', '吉林': '吉林省', '黑龙江': '黑龙江省',
  '江苏': '江苏省', '浙江': '浙江省', '安徽': '安徽省', '福建': '福建省', '江西': '江西省', '山东': '山东省',
  '河南': '河南省', '湖北': '湖北省', '湖南': '湖南省', '广东': '广东省', '海南': '海南省', '四川': '四川省',
  '贵州': '贵州省', '云南': '云南省', '陕西': '陕西省', '甘肃': '甘肃省', '青海': '青海省', '台湾': '台湾省',
  '内蒙古': '内蒙古自治区', '广西': '广西壮族自治区', '西藏': '西藏自治区', '宁夏': '宁夏回族自治区', '新疆': '新疆维吾尔自治区',
  '香港': '香港特别行政区', '澳门': '澳门特别行政区'
}
const reverseProvinceMap = Object.fromEntries(Object.entries(provinceMap).map(([k, v]) => [v, k]))

// === 计算属性 ===
const pageTitle = computed(() => ({ home: '', dashboard: '数据驾驶舱', predict: '智能沙盘演示', manager: '数据资产管理' }[currentView.value]))
const averageYield = computed(() => allData.value.length ? (allData.value.reduce((a, c) => a + c.yieldAmount, 0) / allData.value.length).toFixed(1) : 0)
const topProvince = computed(() => allData.value.length ? allData.value.reduce((p, c) => (p.yieldAmount > c.yieldAmount) ? p : c).province : '暂无')

// === 初始化 ===
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

// === 🔥 核心：生成多维专家建议 🔥 ===
const generateExpertAdvice = (val, prov) => {
  const list = []

  // 1. 产量评级
  if (val > 600) {
    list.push({ type: 'success', icon: '🏆', title: '产量预测：丰收', content: `预计亩产 ${val}kg，显著高于平均水平。建议提前对接仓储与物流。` })
  } else if (val > 450) {
    list.push({ type: 'info', icon: '📊', title: '产量预测：平稳', content: `预计亩产 ${val}kg，属于正常波动范围。建议维持日常田间管理。` })
  } else {
    list.push({ type: 'warning', icon: '⚠️', title: '产量预测：偏低', content: `预计亩产 ${val}kg，存在减产风险。需立即检查水肥及病虫害情况。` })
  }

  // 2. 区域化建议 (根据省份定制)
  if (['新疆', '甘肃', '宁夏', '内蒙古'].includes(prov)) {
    list.push({ type: 'normal', icon: '💧', title: '抗旱节水', content: '西北地区蒸发量大，建议采用滴灌/喷灌技术，并覆盖地膜保墒。' })
  } else if (['黑龙江', '吉林', '辽宁'].includes(prov)) {
    list.push({ type: 'normal', icon: '❄️', title: '防寒防冻', content: '东北地区需关注早霜预警，适时喷施叶面肥提高作物抗寒性。' })
  } else if (['江苏', '浙江', '湖北', '湖南', '江西'].includes(prov)) {
    list.push({ type: 'normal', icon: '🌧️', title: '排涝降渍', content: '南方雨季需疏通沟渠，防止田间积水导致根系缺氧腐烂。' })
  } else if (['河南', '山东', '河北'].includes(prov)) {
    list.push({ type: 'normal', icon: '🚜', title: '一喷三防', content: '黄淮海地区建议实施“一喷三防”，防病虫、防干热风、防早衰。' })
  } else {
    list.push({ type: 'normal', icon: '🛡️', title: '病虫监测', content: '建议安装智能虫情测报灯，通过生物防治减少化学农药使用。' })
  }

  // 3. 施肥建议
  list.push({ type: 'normal', icon: '🧪', title: '科学施肥', content: '建议增施有机肥，控制氮肥用量，适当补充锌、硼等微量元素。' })

  expertAdviceList.value = list
}

const getPrediction = async (target) => {
  loading.value = true
  if(target === 'page') yieldValue.value = null

  try {
    const pRes = await axios.get(`http://localhost:8080/api/data/predict?province=${selectedProvince.value}`)
    const hRes = await axios.get(`http://localhost:8080/api/data/history?province=${selectedProvince.value}`)

    // 解析
    const m = pRes.data.match(/预测产量为: ([\d.]+) kg/);
    const val = m ? parseFloat(m[1]) : 0
    yieldValue.value = val
    parseResult(pRes.data)

    // 🔥 生成建议
    generateExpertAdvice(val, selectedProvince.value)

    loading.value = false
    await nextTick()
    setTimeout(() => {
      if (target === 'modal' && showModal.value) {
        initGaugeChart('modalYieldChart')
        initLineChart('modalHistoryChart', hRes.data)
      } else if (target === 'page') {
        initGaugeChart('yieldChartPage')
        initLineChart('historyChartPage', hRes.data)
      }
    }, 100)
  } catch(e) { loading.value = false; alert('数据请求失败') }
}

const closeModal = () => {
  showModal.value = false
  setTimeout(() => {
    ['modalYieldChart', 'modalHistoryChart'].forEach(id => {
      const dom = document.getElementById(id)
      if (dom) echarts.dispose(dom)
    })
  }, 300)
}

// === 图表绘制 ===
const initGaugeChart = (id) => {
  const dom = document.getElementById(id)
  if(!dom) return;
  echarts.dispose(dom)
  const chart = echarts.init(dom)
  chart.setOption({
    series: [{
      type: 'gauge', min:0, max:800,
      detail:{formatter:'{value} kg', fontSize: 18, color: '#333', fontWeight: 'bold'},
      data:[{value:yieldValue.value}],
      axisLine: { lineStyle: { width: 10, color: [[0.3, '#ff9f43'], [0.7, '#feca57'], [1, '#2ed573']] } }
    }]
  })
}

const initLineChart = (id, data) => {
  const dom = document.getElementById(id)
  if(!dom) return;
  echarts.dispose(dom)
  const chart = echarts.init(dom)
  chart.setOption({
    tooltip:{trigger:'axis'},
    grid: { top: 30, bottom: 30, left: 50, right: 20 },
    xAxis:{type:'category',data:data.map(i=>i.year+'年')},
    yAxis:{type:'value', splitLine:{lineStyle:{type:'dashed', color:'#eee'}}},
    series:[{
      type:'line', smooth:true, areaStyle:{ opacity: 0.2, color: '#2ed573' },
      itemStyle:{color:'#2ed573'}, lineStyle: {width: 3},
      data:data.map(i=>i.yieldAmount)
    }]
  })
}

const initRankChart = () => {
  const chartDom = document.getElementById('rankChart')
  if (!chartDom) return
  echarts.dispose(chartDom)
  const myChart = echarts.init(chartDom)
  const provMap = {}
  allData.value.forEach(d => {
    if(!provMap[d.province]) provMap[d.province] = []
    provMap[d.province].push(d.yieldAmount)
  })
  const categories = [], values = []
  for (const p in provMap) {
    categories.push(p); values.push((provMap[p].reduce((a,b)=>a+b,0) / provMap[p].length).toFixed(2))
  }
  const sorted = categories.map((c, i) => ({name: c, val: parseFloat(values[i])})).sort((a,b) => b.val - a.val).slice(0, 10)
  myChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: {type: 'shadow'} },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: sorted.map(i => i.name), axisLabel: { interval:0, rotate: 30 } },
    yAxis: { type: 'value', name: '平均产量(kg)', splitLine:{lineStyle:{type:'dashed', color:'#eee'}} },
    series: [{ type: 'bar', data: sorted.map(i => i.val), barWidth: '40%', itemStyle: { borderRadius: [5, 5, 0, 0], color: '#2ed573' } }]
  })
}

const parseResult = (text) => {
  const m = text.match(/预测产量为: ([\d.]+) kg/); if(m) yieldValue.value = parseFloat(m[1])
  statusType.value = (text.includes('正常')||text.includes('喜讯'))?'success':'warning'
  adviceText.value = text.split('决策建议:')[1]
}

// === 地图逻辑 ===
const initMapChart = () => {
  const dom = document.getElementById('chinaMapChart')
  if (!dom) return
  echarts.dispose(dom)
  const chart = echarts.init(dom)
  const mapData = provinceList.value.map(dbName => ({ name: dbName, value: 1 }))
  chart.setOption({
    backgroundColor: '#eef2f5',
    tooltip: {
      trigger: 'item', backgroundColor: 'rgba(255,255,255,0.9)', textStyle: { color: '#333' },
      formatter: (params) => provinceList.value.includes(params.data?.name) ? `<div style="font-weight:bold;color:#2ed573">${params.name}</div><div>点击分析</div>` : `<div style="color:#999">${params.name}</div>`
    },
    geo: {
      map: 'china', roam: true, zoom: 1.2, top: 'center', label: { show: true, color: '#555', fontSize: 11 },
      itemStyle: { areaColor: '#fff', borderColor: '#ccc', borderWidth: 1 },
      emphasis: { label:{color:'#fff'}, itemStyle: { areaColor: '#2ed573' } },
      select: { label:{color:'#fff'}, itemStyle: { areaColor: '#ffa502' } }
    },
    series: [{ type: 'map', geoIndex: 0, data: mapData, selectedMode: 'single' }]
  })
  chart.off('click')
  chart.on('click', params => {
    const clickName = params.name
    const cleanClickName = normalizeName(clickName)
    const found = provinceList.value.find(dbName => normalizeName(dbName) === cleanClickName)
    if (found) { selectedProvince.value = found; showModal.value = true; getPrediction('modal') }
  })
  window.addEventListener('resize', () => chart.resize())
}

watch(currentView, (v) => { nextTick(() => { if(v==='home') initMapChart(); if(v==='dashboard') setTimeout(initRankChart, 500) }) })
watch(showModal, v => document.body.style.overflow = v ? 'hidden' : '')
</script>

<style>
/* === 全局重置 === */
html, body, #app { margin: 0 !important; padding: 0 !important; width: 100vw !important; height: 100vh !important; overflow: hidden !important; max-width: none !important; font-family: 'Segoe UI', sans-serif; background-color: #f0f2f5; }
.app-container { display: flex; width: 100%; height: 100%; }
.sidebar { width: 240px; background: #2f3640; color: white; flex-shrink: 0; display: flex; flex-direction: column; }
.main-content { flex: 1; display: flex; flex-direction: column; position: relative; background: #f1f2f6; }

/* 侧边栏样式 */
.logo-area { padding: 30px 20px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-circle { width: 50px; height: 50px; background: #2ed573; border-radius: 50%; font-size: 24px; line-height: 50px; margin: 0 auto 10px; color: white; }
.menu { flex: 1; padding-top: 20px; }
.menu-item { padding: 15px 25px; cursor: pointer; color: #a4b0be; display: flex; gap: 15px; transition: 0.2s; }
.menu-item:hover { color: white; background: rgba(255,255,255,0.05); }
.menu-item.active { background: #2ed573; color: white; font-weight: bold; }
.sidebar-footer { padding: 20px; text-align: center; font-size: 12px; color: #747d8c; }

/* 地图与卡片 */
.home-view { width: 100%; height: 100%; position: relative; }
#chinaMapChart { width: 100%; height: 100%; display: block; }
.map-overlay-panel { position: absolute; top: 20px; left: 20px; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); z-index: 10; }
.panel-title { font-size: 16px; font-weight: bold; margin-bottom: 15px; color: #333; }
.panel-data { display: flex; gap: 20px; margin-bottom: 15px; }
.data-item { display: flex; flex-direction: column; }
.data-item span { font-size: 12px; color: #999; }
.data-item strong { font-size: 20px; color: #2ed573; }
.hint { font-size: 12px; color: #666; }

.content-body { flex: 1; overflow-y: auto; padding: 30px; }
.content-body.no-padding { padding: 0; overflow: hidden; }
.card { background: white; border-radius: 8px; padding: 25px; margin-bottom: 25px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.card-inner { background: #f8f9fa; border-radius: 8px; padding: 20px; border: 1px solid #eee; }

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 25px; margin-bottom: 30px; }
.stat-card { background: white; padding: 20px; border-radius: 8px; display: flex; align-items: center; gap: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.stat-icon { width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.bg-blue { background: #e7f5ff; color: #1890ff; } .bg-green { background: #e6fffa; color: #2ed573; }
.bg-purple { background: #f3f0ff; color: #722ed1; } .bg-orange { background: #fff7e6; color: #fa8c16; }
.number { font-size: 28px; font-weight: bold; color: #2f3542; }

/* 🔥🔥🔥 弹窗样式 (核心修改) 🔥🔥🔥 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; justify-content: center; align-items: center; }
.modal-container { background: white; width: 85%; height: 90%; max-width: 1200px; border-radius: 12px; display: flex; flex-direction: column; box-shadow: 0 10px 40px rgba(0,0,0,0.2); }
.modal-header { padding: 20px 30px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-body { flex: 1; overflow-y: auto; padding: 30px; }

/* 弹窗内容布局 */
.result-grid-modal { display: flex; gap: 30px; height: 260px; margin-bottom: 10px; }
.chart-container { width: 40%; background: #f9f9f9; border-radius: 8px; padding: 10px; display: flex; justify-content: center; align-items: center; }

/* 建议列表容器 */
.advice-container-list { flex: 1; background: #f0fff4; border: 1px solid #c3e6cb; border-radius: 8px; padding: 20px; display: flex; flex-direction: column; overflow: hidden; }
.advice-title { margin-bottom: 15px; color: #155724; font-size: 16px; font-weight: bold; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 10px; }
.advice-scroll-box { flex: 1; overflow-y: auto; padding-right: 5px; }

/* 单条建议样式 */
.advice-list-item { display: flex; gap: 15px; background: white; padding: 12px; border-radius: 6px; margin-bottom: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.03); transition: 0.2s; border-left: 4px solid #ccc; }
.advice-list-item:hover { transform: translateX(5px); }
.advice-list-item.success { border-left-color: #2ed573; }
.advice-list-item.info { border-left-color: #1890ff; }
.advice-list-item.warning { border-left-color: #ffa502; }
.advice-list-item.normal { border-left-color: #70a1ff; }

.item-icon { font-size: 20px; width: 30px; text-align: center; }
.item-content { flex: 1; }
.item-title { font-weight: bold; font-size: 14px; color: #333; margin-bottom: 4px; }
.item-desc { font-size: 13px; color: #666; line-height: 1.4; }

/* 其他 */
.btn-flat { background: #2ed573; color: white; border: none; padding: 10px 25px; border-radius: 6px; cursor: pointer; transition: 0.2s; font-weight: 600; }
.btn-flat:hover { background: #26af61; }
.btn-sm { padding: 6px 15px; font-size: 12px; }
.modern-select { padding: 10px; border: 1px solid #ddd; border-radius: 6px; min-width: 200px; }
.top-header { height: 60px; background: white; padding: 0 30px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 2px 5px rgba(0,0,0,0.02); flex-shrink: 0; }
.close-btn { border: none; background: transparent; font-size: 28px; cursor: pointer; color: #999; }
.loading-state { height: 300px; display: flex; flex-direction: column; align-items: center; justify-content: center; color: #2ed573; }
.tech-spinner { width: 40px; height: 40px; border: 3px solid #eee; border-top-color: #2ed573; border-radius: 50%; animation: spin 1s infinite linear; margin-bottom: 20px; }
@keyframes spin { to { transform: rotate(360deg); } }
.fade-in { animation: fadeIn 0.4s ease; }
.fade-up { animation: fadeUp 0.3s ease; }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>