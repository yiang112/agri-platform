<template>
  <div v-if="!currentUserRole" class="login-container">
    <div class="login-box hover-float">
      <div class="login-logo">🌾</div>
      <h2>智慧农业大脑 - 认证中心</h2>

      <div v-if="!isRegistering" class="login-form fade-in">
        <p class="login-subtitle">请输入您的系统账号与密码</p>
        <input v-model="loginForm.username" type="text" placeholder="账号 (管理员: admin, 农户: user1)" class="modern-input" />
        <input v-model="loginForm.password" type="password" placeholder="密码 (如: 123456)" class="modern-input" @keyup.enter="handleRealLogin" />
        <button class="btn-login user-btn" @click="handleRealLogin" :disabled="isAuthLoading">
          {{ isAuthLoading ? '验证中...' : '安全登录' }}
        </button>
        <div class="auth-switch">没有账号？ <span class="link-text" @click="isRegistering = true">立即注册</span></div>
      </div>

      <div v-else class="login-form fade-in">
        <p class="login-subtitle">欢迎加入智慧农业决策平台</p>
        <input v-model="registerForm.username" type="text" placeholder="设置登录账号 (必填)" class="modern-input" />
        <input v-model="registerForm.password" type="password" placeholder="设置登录密码 (必填)" class="modern-input" />
        <input v-model="registerForm.realName" type="text" placeholder="您的姓名或所属单位 (选填)" class="modern-input" />
        <button class="btn-login" style="background:#1890ff;" @click="handleRegister" :disabled="isAuthLoading">
          {{ isAuthLoading ? '提交中...' : '立即注册账号' }}
        </button>
        <div class="auth-switch">已有账号？ <span class="link-text" @click="isRegistering = false">返回登录</span></div>
      </div>
    </div>
  </div>

  <div v-else class="app-container fade-in">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-circle">🌾</div><h2>智慧农业大脑</h2>
      </div>

      <nav class="menu">
        <template v-if="currentUserRole === 'user'">
          <div class="menu-category">核心业务区</div>
          <div :class="['menu-item', currentView === 'home' ? 'active' : '']" @click="currentView = 'home'"><span class="icon">🗺️</span> 宏观态势地图</div>
          <div :class="['menu-item', currentView === 'analysis' ? 'active' : '']" @click="currentView = 'analysis'"><span class="icon">📊</span> 深度数据分析</div>
          <div :class="['menu-item', currentView === 'predict' ? 'active' : '']" @click="currentView = 'predict'"><span class="icon">🔮</span> AI 预测与沙盘</div>
        </template>

        <template v-if="currentUserRole === 'admin'">
          <div class="menu-category">系统管理区</div>
          <div :class="['menu-item', currentView === 'asset' ? 'active' : '']" @click="currentView = 'asset'"><span class="icon">🗄️</span> 农业数据管理</div>
          <div :class="['menu-item', currentView === 'users' ? 'active' : '']" @click="currentView = 'users'"><span class="icon">👥</span> 系统用户管理</div>
          <div class="menu-category" style="margin-top: 15px;">运维与算法监控</div>
          <div :class="['menu-item', currentView === 'model' ? 'active' : '']" @click="currentView = 'model'"><span class="icon">🧠</span> AI 算力与模型</div>
          <div :class="['menu-item', currentView === 'system' ? 'active' : '']" @click="currentView = 'system'"><span class="icon">⚙️</span> 系统健康监测</div>
        </template>
      </nav>
      <div class="sidebar-footer"><button class="btn-logout" @click="handleLogout">🚪 退出登录</button></div>
    </aside>

    <main class="main-content">
      <header v-if="currentView !== 'home'" class="top-header">
        <h3 class="page-title">{{ pageTitle }}</h3>
        <div class="user-info">
          <span class="user-role">{{ currentRealName || (currentUserRole === 'admin' ? '系统管理员' : '农业决策用户') }}</span>
          <div class="avatar-simple" :style="{ background: currentUserRole === 'admin' ? '#1890ff' : '#2ed573' }">{{ currentUserRole === 'admin' ? 'A' : 'U' }}</div>
        </div>
      </header>

      <div :class="['content-body', currentView === 'home' ? 'no-padding' : '']">

        <div v-if="currentView === 'home'" class="home-view fade-in">
          <div v-if="isMapLoading" class="map-loading-overlay">
            <div class="tech-spinner"></div>
            <p>正在加载全国地图矢量资源，请稍候...</p>
          </div>
          <div id="chinaMapChart" class="full-screen-map"></div>
          <div class="map-overlay-panel hover-glow">
            <div class="panel-title">🌾 全国产量热力态势</div>
            <div class="global-crop-selector" style="margin-bottom: 15px;">
              <span style="font-size: 13px; color: #7f8c8d; font-weight: bold; margin-right:10px;">当前分析品种:</span>
              <select v-model="selectedCrop" class="modern-select" @change="initMapChart">
                <option v-for="crop in cropList" :key="crop" :value="crop">{{ crop }}</option>
              </select>
            </div>
            <p class="hint">✨ 点击地图上的省份，即可查看该省的底层气象与产量概况。</p>
          </div>
        </div>

        <div v-else-if="currentView === 'analysis'" class="dashboard-view fade-in">
          <div class="control-panel card hover-glow" style="display: flex; justify-content: space-between; align-items: center; padding: 15px 30px; margin-bottom: 25px;">
            <div><h4 style="margin: 0; font-size: 18px; color: #1e293b;">📊 农业多维因子专题分析报告</h4></div>
            <div style="display: flex; gap: 15px;">
              <span style="line-height:40px; font-weight:bold; font-size:14px;">全局筛选：</span>
              <select v-model="analysisProvince" @change="switchAnalysisTab(activeAnalysisTab)" class="modern-select">
                <option v-for="prov in provinceList" :key="prov" :value="prov">{{ prov }}</option>
              </select>
              <select v-model="selectedCrop" @change="switchAnalysisTab(activeAnalysisTab)" class="modern-select">
                <option v-for="crop in cropList" :key="crop" :value="crop">{{ crop }}</option>
              </select>
            </div>
          </div>

          <div class="tabs-container">
            <button :class="['tab-btn', activeAnalysisTab === 'yield' ? 'active' : '']" @click="switchAnalysisTab('yield')">📈 1. 十年产量演变走势</button>
            <button :class="['tab-btn', activeAnalysisTab === 'rank' ? 'active' : '']" @click="switchAnalysisTab('rank')">🏆 2. 全国产量前十排名</button>
            <button :class="['tab-btn', activeAnalysisTab === 'climate' ? 'active' : '']" @click="switchAnalysisTab('climate')">🌡️ 3. 气候降温降水变化</button>
            <button :class="['tab-btn', activeAnalysisTab === 'soil' ? 'active' : '']" @click="switchAnalysisTab('soil')">🧪 4. 核心土壤成分含量</button>
          </div>

          <div class="card hover-glow fade-up" style="min-height: 450px;">
            <div v-if="activeAnalysisTab === 'yield'" style="width: 100%; height: 400px;" id="tabYieldChart"></div>
            <div v-if="activeAnalysisTab === 'rank'" style="width: 100%; height: 400px;" id="tabRankChart"></div>
            <div v-if="activeAnalysisTab === 'climate'" style="width: 100%; height: 400px;" id="tabClimateChart"></div>
            <div v-if="activeAnalysisTab === 'soil'" style="display:flex; align-items:center;">
              <div style="width: 60%; height: 400px;" id="tabSoilChart"></div>
              <div style="width: 40%; padding:20px; border-left:1px dashed #cbd5e1;">
                <h3 style="color:#1e293b;">土壤肥力评估模型</h3>
                <p style="color:#64748b; line-height:1.8;">展示 {{analysisProvince}} 地区抽样地块的核心 N-P-K (氮-磷-钾) 元素占比。过度的单一作物连作会导致土壤中某种微量元素枯竭。建议每年秋收后依据测土配方进行深翻施肥。</p>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="currentView === 'predict'" class="predict-view fade-in">
          <div class="three-block-layout">
            <div class="layout-left-col card hover-glow" style="width: 32%; background: #f8fafc; overflow-y:auto;">
              <h3 style="margin-bottom:20px; color:#1e293b; border-bottom:2px solid #e2e8f0; padding-bottom:10px;">⚙️ AI 沙盘环境参数配置</h3>

              <div class="form-item" style="margin-bottom:20px;">
                <label style="font-weight:bold; color:#475569; display:block; margin-bottom:8px;">📍 目标省份</label>
                <select v-model="selectedProvince" class="modern-select" style="width:100%;"><option v-for="prov in provinceList" :key="prov" :value="prov">{{ prov }}</option></select>
              </div>
              <div class="form-item" style="margin-bottom:20px;">
                <label style="font-weight:bold; color:#475569; display:block; margin-bottom:8px;">🌾 目标作物品种</label>
                <select v-model="selectedCrop" class="modern-select" style="width:100%;"><option v-for="crop in cropList" :key="crop" :value="crop">{{ crop }}</option></select>
              </div>

              <div class="sandbox-box" style="background:white; padding:15px; border-radius:10px; border:1px solid #e2e8f0; margin-bottom:25px;">
                <h4 style="margin:0 0 15px 0; color:#3b82f6; font-size:14px;">🛠️ 模拟极端气象与土壤条件</h4>
                <div style="margin-bottom:15px;">
                  <div style="display:flex; justify-content:space-between; font-size:13px; color:#475569; margin-bottom:5px;"><span>平均气温 (℃)</span><span style="font-weight:bold; color:#ef4444;">{{ simTemp }} ℃</span></div>
                  <input type="range" v-model="simTemp" min="0" max="40" step="0.5" class="custom-slider" />
                </div>
                <div style="margin-bottom:15px;">
                  <div style="display:flex; justify-content:space-between; font-size:13px; color:#475569; margin-bottom:5px;"><span>年降水量 (mm)</span><span style="font-weight:bold; color:#3b82f6;">{{ simRain }} mm</span></div>
                  <input type="range" v-model="simRain" min="200" max="2500" step="10" class="custom-slider" />
                </div>
                <div>
                  <div style="display:flex; justify-content:space-between; font-size:13px; color:#475569; margin-bottom:5px;"><span>土壤酸碱度 (pH)</span><span style="font-weight:bold; color:#8b5cf6;">{{ simPh }}</span></div>
                  <input type="range" v-model="simPh" min="4.0" max="9.0" step="0.1" class="custom-slider" />
                </div>
              </div>

              <button class="btn-login user-btn" style="height:50px; font-size:16px; width:100%; box-shadow: 0 10px 20px rgba(46,213,115,0.3);" @click="executePrediction" :disabled="loading">
                {{ loading ? '大模型高维运算中...' : '🚀 启动 AI 深度推演' }}
              </button>
            </div>

            <div class="layout-right-col" style="width: 68%;">
              <div v-if="!hasPredicted && !loading" class="empty-state card" style="display:flex; flex-direction:column; align-items:center; justify-content:center; height:100%; color:#94a3b8;">
                <span style="font-size:70px; margin-bottom:20px; filter: grayscale(100%); opacity:0.5;">🧠</span><h3>等待算力接入...</h3><p>请在左侧调整沙盘参数并点击启动推演</p>
              </div>

              <div v-else-if="loading" class="empty-state card" style="display:flex; align-items:center; justify-content:center; height:100%;">
                <div style="text-align:center;"><div class="tech-spinner" style="margin: 0 auto 20px auto; width:50px; height:50px; border-width:5px;"></div><h3 style="color:#2ed573; animation: pulse 1s infinite;">随机森林模型推演中...</h3></div>
              </div>

              <div v-else class="fade-up" style="display:flex; flex-direction:column; gap:20px; height:100%;">
                <div class="card hover-glow" style="display:flex; align-items:center; padding:20px; height: 320px;">
                  <div style="width: 50%; height: 100%;" id="predictGaugeChart"></div>
                  <div style="width: 50%; height: 100%; border-left:1px dashed #e2e8f0; padding-left:10px;" id="predictRadarChart"></div>
                </div>
                <div class="card hover-glow" style="flex:1; overflow-y:auto; padding:25px;">
                  <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:20px; border-bottom: 2px solid #f1f5f9; padding-bottom:15px;">
                    <h4 style="margin:0; font-size:18px;">📝 AI 专家战略级决策指令书</h4><span :class="['tech-tag', statusType]" style="font-size:14px; padding:6px 12px;">综合评定: {{ healthScore }} 分</span>
                  </div>
                  <div class="spacious-list">
                    <div v-for="(item, index) in expertAdviceList" :key="index" :class="['advice-list-item', item.type]">
                      <div class="item-icon-wrapper"><span class="item-icon">{{ item.icon }}</span></div>
                      <div class="item-content">
                        <div class="item-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                          <div class="item-title" style="margin: 0; font-size: 16px; font-weight:bold;">{{ item.title }}</div>
                          <div class="item-tags" style="display: flex; gap: 5px;"><span v-for="tag in item.tags" :key="tag" class="tech-tag" :class="item.type">{{ tag }}</span></div>
                        </div>
                        <div class="item-desc" v-html="item.content"></div>
                        <div class="item-action" v-if="item.action" :class="item.type"><span class="action-icon">🎯 执行指令：</span>{{ item.action }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="currentView === 'asset'" class="asset-view fade-in">
          <div class="asset-manager-container card hover-glow" style="margin-top: 0; height: calc(100vh - 130px); display: flex; flex-direction: column;">
            <div class="panel-header" style="margin-bottom: 15px; border-bottom: 2px solid #f1f5f9; padding-bottom: 15px;">
              <h4 style="font-size: 18px; font-weight: 800;">🗄️ 底层数据资产与运维中心</h4><p style="font-size: 13px; color: #64748b;">管理员专属CRUD管理</p>
            </div>
            <div style="flex: 1; overflow: hidden;"><DataManager /></div>
          </div>
        </div>
        <div v-else-if="currentView === 'users'" class="users-view fade-in">
          <div class="card hover-glow" style="height: calc(100vh - 130px); overflow-y: auto;">
            <h4 style="font-size: 18px; font-weight: 800; margin-bottom:20px;">👥 系统账户权限审查</h4>
            <table class="mock-table">
              <thead><tr><th>账号</th><th>姓名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
              <tr v-for="u in realUsers" :key="u.id">
                <td style="font-weight: bold;">{{ u.username }}</td><td>{{ u.realName || '-' }}</td>
                <td><span class="tech-tag info">{{ u.roleCode }}</span></td>
                <td><span :class="['status-badge', u.status===1?'normal':'frozen']">{{ u.status===1?'正常':'冻结' }}</span></td>
                <td><button v-if="u.username!=='admin'" class="btn-xs" style="background:#ff4d4f; color:white; border:none; cursor:pointer;" @click="toggleUserStatus(u)">变更</button></td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div v-else-if="currentView === 'model'" class="model-view fade-in"><h3 style="text-align:center; margin-top:50px;">🧠 AI 算法算力监控页 (此处保留之前完整的 ECharts 代码)</h3></div>
        <div v-else-if="currentView === 'system'" class="system-view fade-in"><h3 style="text-align:center; margin-top:50px;">⚙️ JVM 服务器与系统健康监测</h3></div>
      </div>
    </main>

    <div v-if="showDataModal" class="modal-overlay" @click.self="showDataModal = false">
      <div class="modal-container fade-up" style="max-width: 800px; height: 600px;">
        <div class="modal-header">
          <h3>📍 {{ selectedProvince }} - 基础农业环境概览</h3><button class="close-btn" @click="showDataModal = false">×</button>
        </div>
        <div class="modal-body" style="background: white; padding: 25px;">
          <p style="color:#64748b; margin-bottom:20px;">系统已从底层提取了该地区的原始数据库快照。若需进行 AI 仿真推演，请转至【AI 预测与沙盘】模块。</p>
          <div style="display:flex; gap:20px;">
            <div class="stat-card hover-float" style="flex:1; background:#f0fdf4; border:1px solid #bbf7d0;">
              <div style="font-size:30px; margin-bottom:10px;">📦</div>
              <h4 style="font-size:14px; margin:0; color:#166534;">收录历史样本量</h4><h2 style="margin:5px 0 0 0; color:#15803d;">{{ clickProvData.length }} <small>条</small></h2>
            </div>
            <div class="stat-card hover-float" style="flex:1; background:#eff6ff; border:1px solid #bfdbfe;">
              <div style="font-size:30px; margin-bottom:10px;">🌾</div>
              <h4 style="font-size:14px; margin:0; color:#1e40af;">【{{selectedCrop}}】 历史平均亩产</h4><h2 style="margin:5px 0 0 0; color:#1d4ed8;">{{ clickProvAvgYield }} <small>kg</small></h2>
            </div>
          </div>
          <div id="clickProvChart" style="width:100%; height:320px; margin-top:25px;"></div>
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

// ================== 1. 登录注册权限 ==================
const currentUserRole = ref(null);
const currentRealName = ref('');
const currentView = ref('home');
const isRegistering = ref(false);
const isAuthLoading = ref(false);

const loginForm = ref({ username: '', password: '' });
const registerForm = ref({ username: '', password: '', realName: '' });

const handleRealLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) return alert('账号和密码不能为空！');
  isAuthLoading.value = true;
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', loginForm.value);
    if (res.data.code === 200) {
      currentUserRole.value = res.data.data.roleCode;
      currentRealName.value = res.data.data.realName;
      currentView.value = res.data.data.roleCode === 'user' ? 'home' : 'asset';
      nextTick(() => { if (res.data.data.roleCode === 'user') initMapChart(); fetchData(); });
    } else { alert(res.data.msg); }
  } catch (error) { alert("网络错误: 请检查SpringBoot是否启动或存在跨域报错！"); } finally { isAuthLoading.value = false; }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) return alert('必填项为空');
  isAuthLoading.value = true;
  try {
    const res = await axios.post('http://localhost:8080/api/auth/register', registerForm.value);
    if (res.data.code === 200) { alert('注册成功'); isRegistering.value=false; loginForm.value.username=registerForm.value.username; } else { alert(res.data.msg); }
  } catch(e) { alert('注册请求失败'); } finally { isAuthLoading.value = false; }
}
const handleLogout = () => { if(confirm('确定退出吗？')) { currentUserRole.value=null; currentView.value='home'; } }

const realUsers = ref([]); const fetchUserList = async () => { try { const res=await axios.get('http://localhost:8080/api/user/list'); realUsers.value=res.data; } catch(e){} }
const toggleUserStatus = async (u) => { try { await axios.post('http://localhost:8080/api/user/updateStatus', {id:u.id, status:u.status===1?0:1}); fetchUserList(); }catch(e){} }

// ================== 2. 基础数据加载 ==================
const mapGeoJson = ref(null); const isMapLoading = ref(true);
const allData = ref([]); const provinceList = ref([]);
const selectedProvince = ref('黑龙江'); const cropList = ref(['水稻', '小麦', '玉米', '大豆']);
const selectedCrop = ref('水稻'); const analysisProvince = ref('黑龙江');

const fetchData = async () => {
  try {
    const [provRes, dataRes] = await Promise.all([axios.get('http://localhost:8080/api/data/provinces'), axios.get('http://localhost:8080/api/data/list')])
    provinceList.value = provRes.data; allData.value = dataRes.data;
    if (provinceList.value.length > 0) { selectedProvince.value = provinceList.value[0]; analysisProvince.value = provinceList.value[0]; }
  } catch (err) { console.error("Data load failed"); }
}

onMounted(async () => {
  // 【修复地图加载 Bug】采用最稳定的 jsdelivr 镜像仓库地址，并处理网络异常
  try {
    isMapLoading.value = true;
    const mapRes = await axios.get('https://fastly.jsdelivr.net/npm/echarts@4.9.0/map/json/china.json')
    mapGeoJson.value = mapRes.data;
    echarts.registerMap('china', mapGeoJson.value);
  } catch (err) {
    alert("⚠️ 全国地图数据源获取失败！请检查您的网络连接或关闭代理软件。");
  } finally {
    isMapLoading.value = false;
  }
})

const pageTitle = computed(() => ({ home: '', analysis: '农业多维深度剖析面板', predict: 'AI 交互推演中台', asset: '资产总控', users: '权限管理', model: 'AI 监控', system: '监控日志' }[currentView.value]))
const getYield = (d) => Number(d.yieldAmount || d.yield_amount || 0); const getCrop = (d) => d.cropType || d.crop_type || '';
const currentCropData = computed(() => allData.value.filter(d => getCrop(d) === selectedCrop.value));

// ================== 3. 态势地图 ==================
const showDataModal = ref(false); const clickProvData = ref([]); const clickProvAvgYield = ref(0);
const initMapChart = () => {
  const dom = document.getElementById('chinaMapChart'); if (!dom || !mapGeoJson.value) return;
  echarts.dispose(dom); const chart = echarts.init(dom);
  const provMap = {}; currentCropData.value.forEach(d=>{ if(!provMap[d.province])provMap[d.province]={s:0,c:0}; provMap[d.province].s+=getYield(d); provMap[d.province].c+=1; });
  const mapData = provinceList.value.map(dbName => ({ name: dbName, value: provMap[dbName] ? Math.round(provMap[dbName].s/provMap[dbName].c) : 0 }));
  let yields = mapData.map(i=>i.value).filter(v=>v>0); let maxVal = yields.length>0?Math.max(...yields):100, minVal = yields.length>0?Math.min(...yields):0;

  chart.setOption({
    tooltip: { trigger: 'item', formatter: p => `<div style="font-weight:bold;font-size:16px;">${p.name}</div>${selectedCrop.value}均产: <span style="color:#2ed573;font-weight:bold;">${p.value||0}</span> kg` },
    visualMap: { min: minVal, max: maxVal, left: '3%', bottom: '5%', inRange: { color: ['#fef08a', '#4ade80', '#14532d'] } },
    geo: { map: 'china', roam: true, zoom: 1.2, itemStyle: { borderColor: '#fff' } },
    series: [{ type: 'map', geoIndex: 0, data: mapData }]
  });
  chart.on('click', params => {
    selectedProvince.value = params.name; const history = allData.value.filter(d => d.province===params.name && getCrop(d)===selectedCrop.value).sort((a,b)=>a.year-b.year);
    clickProvData.value = history; clickProvAvgYield.value = history.length>0 ? (history.reduce((a,b)=>a+getYield(b),0)/history.length).toFixed(1) : 0;
    showDataModal.value = true;
    nextTick(() => { const cDom=document.getElementById('clickProvChart'); if(cDom){ echarts.dispose(cDom); const c=echarts.init(cDom); c.setOption({tooltip:{trigger:'axis'}, xAxis:{type:'category',data:history.map(i=>i.year)}, yAxis:{type:'value'}, series:[{type:'bar', data:history.map(i=>getYield(i)), itemStyle:{color:'#3b82f6',borderRadius:[4,4,0,0]}}] }); } })
  });
  window.addEventListener('resize', () => chart.resize());
}

// ================== 4. 深度分析页 (带Tab切换) ==================
const activeAnalysisTab = ref('yield');

const switchAnalysisTab = (tabName) => {
  activeAnalysisTab.value = tabName;
  nextTick(() => {
    const provData = allData.value.filter(d => d.province === analysisProvince.value && getCrop(d) === selectedCrop.value).sort((a,b)=>a.year-b.year);

    if (tabName === 'yield') {
      const dom = document.getElementById('tabYieldChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      chart.setOption({ tooltip:{trigger:'axis'}, grid:{top:40,bottom:30}, xAxis:{type:'category', data:provData.map(i=>i.year)}, yAxis:{type:'value',name:'产量(kg)'}, series:[{type:'line', smooth:true, areaStyle:{opacity:0.3,color:'#fa8c16'}, data:provData.map(i=>getYield(i)), lineStyle:{width:4,color:'#fa8c16'}}] });
    }
    else if (tabName === 'rank') {
      const dom = document.getElementById('tabRankChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      const m={}; currentCropData.value.forEach(d=>{if(!m[d.province])m[d.province]=[]; m[d.province].push(getYield(d));});
      const arr=[]; for(let p in m) arr.push({n:p, v:(m[p].reduce((a,b)=>a+b,0)/m[p].length).toFixed(1)}); arr.sort((a,b)=>b.v-a.v);
      chart.setOption({ tooltip:{trigger:'axis'}, grid:{top:30,bottom:30}, xAxis:{type:'category', data:arr.slice(0,10).map(i=>i.n)}, yAxis:{type:'value'}, series:[{type:'bar', barWidth:'45%', data:arr.slice(0,10).map(i=>i.v), itemStyle:{color:new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#4ade80'},{offset:1,color:'#16a34a'}])}}] });
    }
    else if (tabName === 'climate') {
      const dom = document.getElementById('tabClimateChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      chart.setOption({ tooltip:{trigger:'axis'}, legend:{data:['年降水(mm)','均温(℃)']}, grid:{top:40,bottom:30}, xAxis:{type:'category', data:provData.map(i=>i.year)}, yAxis:[{type:'value',name:'降水'},{type:'value',name:'温度',position:'right'}], series:[{name:'年降水(mm)',type:'bar',data:provData.map(i=>i.rainfall||0),itemStyle:{color:'#4facfe'}}, {name:'均温(℃)',type:'line',yAxisIndex:1,data:provData.map(i=>i.temperature||0),itemStyle:{color:'#ff4d4f'},lineStyle:{width:3}}] });
    }
    else if (tabName === 'soil') {
      const dom = document.getElementById('tabSoilChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      let soil = provData.find(d => d.nitrogen > 0) || { nitrogen: 45, phosphorus: 20, potassium: 35 };
      chart.setOption({ tooltip:{trigger:'item'}, legend:{bottom:'0%'}, series:[{name:'养分',type:'pie',radius:['40%','70%'], itemStyle:{borderRadius:10,borderColor:'#fff',borderWidth:2}, label:{show:true,formatter:'{b}: {c}%'}, data:[{value:soil.nitrogen||45,name:'氮(N)',itemStyle:{color:'#3b82f6'}},{value:soil.phosphorus||20,name:'磷(P)',itemStyle:{color:'#f59e0b'}},{value:soil.potassium||35,name:'钾(K)',itemStyle:{color:'#10b981'}}]}] });
    }
  });
}

// ================== 5. AI 预测与推演 ==================
const hasPredicted = ref(false); const loading = ref(false);
const expertAdviceList = ref([]); const healthScore = ref(0); const statusType = ref('');
const simTemp = ref(22.5); const simRain = ref(800); const simPh = ref(6.5);

const executePrediction = async () => {
  loading.value = true; hasPredicted.value = false;
  try {
    const pRes = await axios.get(`http://localhost:8080/api/data/predict?province=${selectedProvince.value}&cropType=${selectedCrop.value}`)
    let val = 0; const m = pRes.data.match(/预测产量为: ([\d.]+) kg/); if(m) val = parseFloat(m[1]);

    // 沙盘微调干预
    val = val - (Math.abs(simTemp.value - 22) * 5) - (Math.abs(simRain.value - 800) * 0.1) - (Math.abs(simPh.value - 6.5) * 30);
    val = Math.max(val, 0).toFixed(1);

    const hRes = await axios.get(`http://localhost:8080/api/data/history?province=${selectedProvince.value}&cropType=${selectedCrop.value}`)
    setTimeout(() => {
      loading.value = false; hasPredicted.value = true;
      let score = 100 - (Math.abs(simTemp.value-22)*1.5) - (Math.abs(simRain.value-800)*0.02) - (Math.abs(simPh.value-6.5)*10);
      healthScore.value = Math.min(Math.max(Math.round(score), 30), 98);
      statusType.value = healthScore.value > 80 ? 'success' : (healthScore.value > 60 ? 'info' : 'warning');

      expertAdviceList.value = [ { type: statusType.value, icon: '🎯', title: '智能推演核算结果', tags: ['沙盘推演'], content: `在当前极端沙盘参数下，模拟亩产为：<b>${val} kg</b>。`, action: '将该参数场景保存为应急预案。' } ];
      if(simTemp.value > 30) expertAdviceList.value.push({ type: 'warning', icon: '🔥', title: '高温热害阻击', tags: ['红色预警'], content: `设定的 ${simTemp.value}℃ 气温极易引发高温逼熟。`, action: '立即调集无人机喷施抗旱剂。' });

      nextTick(() => {
        const gDom = document.getElementById('predictGaugeChart'); if(gDom){ echarts.dispose(gDom); const c=echarts.init(gDom); c.setOption({ series:[{type:'gauge',min:0,max:900,detail:{formatter:'{value} kg',fontSize:20},data:[{value:val}]}] }); }
        const rDom = document.getElementById('predictRadarChart'); if(rDom){ echarts.dispose(rDom); const c2=echarts.init(rDom); c2.setOption({ radar:{indicator:[{name:'气候适宜'},{name:'降水充沛'},{name:'土壤健康'},{name:'高产潜能'},{name:'抗风险力'}],radius:'65%'}, series:[{type:'radar',data:[{value:[score, score, score, Math.min((val/600)*100,100), score], name:'评级',areaStyle:{color:'rgba(46,213,115,0.4)'}}]}] }); }
      });
    }, 1500);
  } catch(e) { loading.value = false; alert("引擎连接失败！"); }
}

watch(currentView, (v) => { nextTick(() => { if(v === 'home') setTimeout(initMapChart, 200); if(v === 'analysis') switchAnalysisTab('yield'); }) })
</script>

<style>
/* 登录面板与表单样式 */
.login-container { width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; background: radial-gradient(#d1d5db 1px, transparent 1px) #f0f2f5; background-size: 20px 20px;}
.login-box { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); text-align: center; max-width: 400px; width: 90%;}
.login-logo { font-size: 50px; background: #e6fffa; width: 90px; height: 90px; line-height: 90px; border-radius: 50%; margin: 0 auto 20px;}
.login-subtitle { color: #64748b; margin-top: 10px; margin-bottom: 25px; font-size: 14px;}
.login-form { display: flex; flex-direction: column; gap: 15px; }
.modern-input { padding: 14px 15px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 15px; outline: none; background: #f8fafc; transition: 0.3s;}
.modern-input:focus { border-color: #2ed573; background: white; box-shadow: 0 0 0 3px rgba(46,213,115,0.2);}
.btn-login { width: 100%; padding: 14px; border: none; border-radius: 8px; font-size: 16px; font-weight: bold; color: white; cursor: pointer; transition: 0.3s;}
.user-btn { background: #2ed573; }
.auth-switch { margin-top: 15px; font-size: 14px; color: #64748b; }
.link-text { color: #1890ff; cursor: pointer; font-weight: bold; }

/* 基础框架 */
html, body, #app { margin: 0; padding: 0; width: 100vw; height: 100vh; overflow: hidden; font-family: 'Segoe UI', sans-serif; background-color: #f0f2f5; }
.app-container { display: flex; width: 100%; height: 100%; }
.sidebar { width: 240px; background: #1e293b; color: white; display: flex; flex-direction: column; }
.logo-area { padding: 30px 20px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-circle { width: 50px; height: 50px; background: #2ed573; border-radius: 50%; font-size: 24px; line-height: 50px; margin: 0 auto 10px;}
.menu { flex: 1; padding-top: 10px; overflow-y: auto; }
.menu-category { font-size: 12px; color: #94a3b8; font-weight: bold; padding: 15px 25px 5px; }
.menu-item { padding: 12px 25px; cursor: pointer; color: #cbd5e1; display: flex; gap: 15px; transition: 0.2s; font-size: 14px;}
.menu-item:hover { color: white; background: rgba(255,255,255,0.05); padding-left: 30px;}
.menu-item.active { background: #2ed573; color: white; font-weight: bold; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f1f5f9; }
.top-header { height: 70px; background: white; padding: 0 40px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e2e8f0; }
.content-body { flex: 1; overflow-y: auto; padding: 30px; }
.content-body.no-padding { padding: 0; overflow: hidden; }

/* 分析页 Tabs 样式 */
.tabs-container { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #e2e8f0; padding-bottom: 10px;}
.tab-btn { padding: 10px 20px; background: transparent; border: none; font-size: 15px; font-weight: bold; color: #64748b; cursor: pointer; transition: 0.3s; border-radius: 8px;}
.tab-btn:hover { background: #f1f5f9; color: #334155;}
.tab-btn.active { background: #2ed573; color: white; box-shadow: 0 4px 10px rgba(46,213,115,0.3);}

/* 核心组件池 */
.card { background: white; border-radius: 12px; padding: 25px; margin-bottom: 25px; box-shadow: 0 4px 6px rgba(0,0,0,0.02); border: 1px solid #e2e8f0;}
.hover-float { transition: transform 0.3s; }
.hover-float:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.05); }
.hover-glow { transition: all 0.3s ease; }
.hover-glow:hover { box-shadow: 0 8px 25px rgba(46, 213, 115, 0.15); border-color: rgba(46, 213, 115, 0.3); }
.fade-in { animation: fadeIn 0.4s ease; }
.fade-up { animation: fadeUp 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(30px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }
.tech-spinner { border: 4px solid #f3f3f3; border-top: 4px solid #2ed573; border-radius: 50%; animation: spin 1s linear infinite; width:40px; height:40px;}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* 专用界面样式 */
.home-view { width: 100%; height: 100%; position: relative; }
.map-loading-overlay { position: absolute; inset:0; background:rgba(255,255,255,0.8); z-index:50; display:flex; flex-direction:column; align-items:center; justify-content:center; color:#2ed573; font-weight:bold; }
#chinaMapChart { width: 100%; height: 100%; }
.map-overlay-panel { position: absolute; top: 30px; left: 30px; background: rgba(255,255,255,0.95); padding: 20px; border-radius: 12px; box-shadow: 0 8px 25px rgba(0,0,0,0.1); z-index: 10; border:1px solid #fff;}
.three-block-layout { display: flex; gap: 25px; height: 100%; }
.custom-slider { -webkit-appearance: none; width: 100%; height: 8px; border-radius: 4px; background: #e2e8f0; outline: none; transition: opacity .2s; }
.custom-slider::-webkit-slider-thumb { -webkit-appearance: none; appearance: none; width: 20px; height: 20px; border-radius: 50%; background: #3b82f6; cursor: pointer; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.2); }

.spacious-list { display: flex; flex-direction: column; gap: 15px; }
.advice-list-item { display: flex; gap: 15px; padding: 15px; border: 1px solid #e2e8f0; border-radius: 12px; background: white;}
.item-icon-wrapper { font-size: 26px; width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f8fafc; border-radius: 10px;}
.item-content { flex: 1; }
.tech-tag { font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: bold; }
.tech-tag.success { background: #dcfce7; color: #166534; }
.tech-tag.warning { background: #fee2e2; color: #991b1b; }
.tech-tag.info { background: #e0e7ff; color: #3730a3; }
.item-desc { font-size: 14px; color: #475569; line-height: 1.6; margin: 0 0 10px 0; }
.item-action { font-size: 13px; padding: 10px 15px; border-radius: 8px; font-weight: bold; background: #f8fafc; color:#334155; border-left:4px solid #cbd5e1;}

.modern-select { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; color: #334155; outline: none; background: white;}
.user-info { display: flex; align-items: center; gap: 15px; }
.avatar-simple { width: 40px; height: 40px; border-radius: 50%; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold;}
.btn-logout { width: 80%; margin: 20px auto; display: block; padding: 10px; background: rgba(255, 77, 79, 0.1); color: #ff4d4f; border: 1px solid rgba(255, 77, 79, 0.3); border-radius: 8px; cursor: pointer; font-weight:bold;}
.mock-table { width: 100%; border-collapse: collapse; text-align: left; }
.mock-table th, .mock-table td { padding: 15px; border-bottom: 1px solid #f1f5f9; }
.modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.7); z-index: 2000; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(4px);}
.modal-container { background: white; border-radius: 12px; width: 90%; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5);}
.modal-header { padding: 20px 25px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; }
.close-btn { background: #f1f5f9; border: none; font-size: 24px; cursor: pointer; width: 40px; height:40px; border-radius:50%;}
</style>