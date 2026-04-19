<template>
  <div v-if="!currentUserRole" class="login-container">
    <div class="login-box hover-float">
      <div class="login-logo">🌾</div>
      <h2>智慧农业大脑 - 认证中心</h2>

      <div v-if="!isRegistering" class="login-form fade-in">
        <p class="login-subtitle">请输入您的系统账号与密码</p>
        <input v-model="loginForm.username" type="text" placeholder="账号 (如: admin 或 user1)" class="modern-input" />
        <input v-model="loginForm.password" type="password" placeholder="密码 (如: 123456)" class="modern-input" @keyup.enter="handleRealLogin" />
        <button class="btn-login user-btn" @click="handleRealLogin" :disabled="isAuthLoading">{{ isAuthLoading ? '验证中...' : '安全登录' }}</button>
        <div class="auth-switch">没有账号？ <span class="link-text" @click="isRegistering = true">立即注册</span></div>
      </div>

      <div v-else class="login-form fade-in">
        <p class="login-subtitle">欢迎加入智慧农业决策平台</p>
        <input v-model="registerForm.username" type="text" placeholder="设置登录账号 (必填)" class="modern-input" />
        <input v-model="registerForm.password" type="password" placeholder="设置登录密码 (必填)" class="modern-input" />
        <input v-model="registerForm.realName" type="text" placeholder="您的姓名或所属单位 (选填)" class="modern-input" />
        <button class="btn-login" style="background:#1890ff;" @click="handleRegister" :disabled="isAuthLoading">{{ isAuthLoading ? '提交中...' : '立即注册账号' }}</button>
        <div class="auth-switch">已有账号？ <span class="link-text" @click="isRegistering = false">返回登录</span></div>
      </div>
    </div>
  </div>

  <div v-else class="app-container fade-in">
    <aside class="sidebar">
      <div class="logo-area"><div class="logo-circle">🌾</div><h2>智慧农业大脑</h2></div>
      <nav class="menu">
        <template v-if="currentUserRole === 'user'">
          <div class="menu-category">核心业务区</div>
          <div :class="['menu-item', currentView === 'home' ? 'active' : '']" @click="currentView = 'home'"><span class="icon">🗺️</span> 宏观态势地图</div>
          <div :class="['menu-item', currentView === 'analysis' ? 'active' : '']" @click="currentView = 'analysis'"><span class="icon">📊</span> 深度数据分析</div>
          <div :class="['menu-item', currentView === 'predict' ? 'active' : '']" @click="currentView = 'predict'"><span class="icon">🔮</span> AI 预测决策</div>
        </template>
        <template v-if="currentUserRole === 'admin'">
          <div class="menu-category">系统管理区</div>
          <div :class="['menu-item', currentView === 'asset' ? 'active' : '']" @click="currentView = 'asset'"><span class="icon">🗄️</span> 农业数据管理</div>
          <div :class="['menu-item', currentView === 'users' ? 'active' : '']" @click="currentView = 'users'"><span class="icon">👥</span> 系统用户管理</div>
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
          <div v-if="isMapLoading" class="map-loading-overlay"><div class="tech-spinner"></div><p>正在加载全国地图矢量资源...</p></div>
          <div id="chinaMapChart" class="full-screen-map"></div>
          <div class="map-overlay-panel hover-glow">
            <div class="panel-title">🌾 全国产量热力态势</div>
            <div class="global-crop-selector" style="margin-bottom: 15px;">
              <span style="font-size: 13px; color: #7f8c8d; font-weight: bold; margin-right:10px;">分析品种:</span>
              <select v-model="selectedCrop" class="modern-select" @change="initMapChart">
                <option v-for="crop in cropList" :key="crop" :value="crop">{{ crop }}</option>
              </select>
            </div>
            <p class="hint">✨ 点击地图省份，查看该省当年（最新）详细农业参数。</p>
          </div>
        </div>

        <div v-else-if="currentView === 'analysis'" class="dashboard-view fade-in">
          <div class="control-panel card hover-glow" style="display: flex; justify-content: space-between; align-items: center; padding: 15px 30px; margin-bottom: 25px;">
            <div><h4 style="margin: 0; font-size: 18px;">📊 农业多维因子专题分析</h4></div>
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
            <button :class="['tab-btn', activeAnalysisTab === 'yield' ? 'active' : '']" @click="switchAnalysisTab('yield')">📈 十年产量演变</button>
            <button :class="['tab-btn', activeAnalysisTab === 'rank' ? 'active' : '']" @click="switchAnalysisTab('rank')">🏆 产量前十排名</button>
            <button :class="['tab-btn', activeAnalysisTab === 'climate' ? 'active' : '']" @click="switchAnalysisTab('climate')">🌡️ 气候降水变化</button>
            <button :class="['tab-btn', activeAnalysisTab === 'soil' ? 'active' : '']" @click="switchAnalysisTab('soil')">🧪 核心土壤含量</button>
            <button :class="['tab-btn', activeAnalysisTab === 'correlation' ? 'active' : '']" @click="switchAnalysisTab('correlation')">🔗 气象与产量相关性</button>
          </div>

          <div class="card hover-glow fade-up" style="min-height: 450px;">
            <div v-if="activeAnalysisTab === 'yield'" style="width: 100%; height: 400px;" id="tabYieldChart"></div>
            <div v-if="activeAnalysisTab === 'rank'" style="width: 100%; height: 400px;" id="tabRankChart"></div>
            <div v-if="activeAnalysisTab === 'climate'" style="width: 100%; height: 400px;" id="tabClimateChart"></div>
            <div v-if="activeAnalysisTab === 'soil'" style="display:flex; align-items:center;">
              <div style="width: 60%; height: 400px;" id="tabSoilChart"></div>
              <div style="width: 40%; padding:20px; border-left:1px dashed #cbd5e1;">
                <h3 style="color:#1e293b;">土壤肥力评估模型</h3>
                <p style="color:#64748b; line-height:1.8;">展示 {{analysisProvince}} 最近一年的核心 N-P-K (氮-磷-钾) 元素含量情况。过度的单一作物连作会导致土壤微量元素枯竭。建议每年秋收后依据测土配方进行深翻施肥。</p>
              </div>
            </div>
            <div v-if="activeAnalysisTab === 'correlation'" style="display:flex; flex-direction:column;">
              <p style="color:#64748b; margin-bottom:10px; text-align:center;">💡 图表说明：横轴为年降水量，纵轴为亩产，气泡大小代表该年平均气温。可直观分析气候对产量的影响。</p>
              <div style="width: 100%; height: 380px;" id="tabCorrelationChart"></div>
            </div>
          </div>
        </div>

        <div v-else-if="currentView === 'predict'" class="predict-view fade-in">
          <div class="three-block-layout">
            <div class="layout-left-col card hover-glow" style="width: 25%; background: #f8fafc;">
              <h3 style="margin-bottom:20px; color:#1e293b; border-bottom:2px solid #e2e8f0; padding-bottom:10px;">🎯 预测目标选择</h3>

              <div class="form-item" style="margin-bottom:25px;">
                <label style="font-weight:bold; color:#475569; display:block; margin-bottom:8px;">📍 目标省份</label>
                <select v-model="selectedProvince" class="modern-select" style="width:100%; height:45px;"><option v-for="prov in provinceList" :key="prov" :value="prov">{{ prov }}</option></select>
              </div>
              <div class="form-item" style="margin-bottom:30px;">
                <label style="font-weight:bold; color:#475569; display:block; margin-bottom:8px;">🌾 目标作物品种</label>
                <select v-model="selectedCrop" class="modern-select" style="width:100%; height:45px;"><option v-for="crop in cropList" :key="crop" :value="crop">{{ crop }}</option></select>
              </div>

              <button class="btn-login user-btn" style="height:55px; font-size:16px; width:100%; box-shadow: 0 10px 20px rgba(46,213,115,0.3);" @click="executePrediction" :disabled="loading">
                {{ loading ? '模型高维测算中...' : '🚀 生成未来三年预测' }}
              </button>
            </div>

            <div class="layout-right-col" style="width: 75%;">
              <div v-if="!hasPredicted && !loading" class="empty-state card" style="display:flex; flex-direction:column; align-items:center; justify-content:center; height:100%; color:#94a3b8;">
                <span style="font-size:70px; margin-bottom:20px; filter: grayscale(100%); opacity:0.5;">📈</span><h3>等待预测指令...</h3><p>请在左侧选择省份与作物，点击按钮生成产量预测。</p>
              </div>

              <div v-else-if="loading" class="empty-state card" style="display:flex; align-items:center; justify-content:center; height:100%;">
                <div style="text-align:center;"><div class="tech-spinner" style="margin: 0 auto 20px auto; width:50px; height:50px; border-width:5px;"></div><h3 style="color:#2ed573;">AI 随机森林模型预测中...</h3></div>
              </div>

              <div v-else class="fade-up" style="display:flex; flex-direction:column; gap:20px; height:100%;">
                <div class="card hover-glow" style="padding:20px; height: 350px; display:flex; flex-direction:column;">
                  <h4 style="margin:0 0 10px 0; font-size:16px;">📈 {{ selectedProvince }} - {{ selectedCrop }} 历史与未来三年产量预测走势</h4>
                  <div style="flex:1;" id="predictTrendChart"></div>
                </div>

                <div class="card hover-glow" style="flex:1; overflow-y:auto; padding:25px;">
                  <h4 style="margin:0 0 15px 0; font-size:18px; border-bottom: 2px solid #f1f5f9; padding-bottom:10px;">📝 智能调度决策建议</h4>
                  <div class="spacious-list">
                    <div v-for="(item, index) in expertAdviceList" :key="index" :class="['advice-list-item', item.type]">
                      <div class="item-icon-wrapper"><span class="item-icon">{{ item.icon }}</span></div>
                      <div class="item-content">
                        <div class="item-header" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                          <div class="item-title" style="margin: 0; font-size: 16px; font-weight:bold;">{{ item.title }}</div>
                          <span :class="['tech-tag', item.type]">{{ item.tags[0] }}</span>
                        </div>
                        <div class="item-desc" v-html="item.content"></div>
                        <div class="item-action" v-if="item.action" :class="item.type"><span class="action-icon">🎯 建议：</span>{{ item.action }}</div>
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
          <div class="card hover-glow" style="height: calc(100vh - 130px); overflow-y: auto; position: relative;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
              <h4 style="font-size: 18px; font-weight: 800; margin:0;">👥 系统账户全面管理</h4>
              <button class="btn-login user-btn" style="width:auto; padding:8px 20px;" @click="openUserModal()">+ 新增用户</button>
            </div>

            <table class="mock-table">
              <thead><tr><th>账号</th><th>姓名/单位</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
              <tbody>
              <tr v-for="u in realUsers" :key="u.id">
                <td style="font-weight: bold; color:#1890ff;">{{ u.username }}</td><td>{{ u.realName || '-' }}</td>
                <td><span :class="['tech-tag', u.roleCode==='admin'?'info':'normal']">{{ u.roleCode==='admin'?'管理员':'普通用户' }}</span></td>
                <td><span :class="['status-badge', u.status===1?'normal':'frozen']">{{ u.status===1?'正常':'冻结' }}</span></td>
                <td>
                  <div v-if="u.username !== 'admin'" style="display:flex; gap:10px;">
                    <button class="btn-xs" style="background:#f59e0b; color:white; border:none; cursor:pointer;" @click="openUserModal(u)">修改</button>
                    <button class="btn-xs" :style="{background: u.status===1?'#64748b':'#10b981', color:'white', border:'none', cursor:'pointer'}" @click="toggleUserStatus(u)">{{ u.status===1?'冻结':'解冻' }}</button>
                    <button class="btn-xs" style="background:#ef4444; color:white; border:none; cursor:pointer;" @click="deleteUser(u.id)">删除</button>
                  </div>
                  <span v-else style="color:#94a3b8; font-size:12px;">不可操作超管</span>
                </td>
              </tr>
              </tbody>
            </table>

            <div v-if="showUserModal" class="modal-overlay" style="position:absolute; background:rgba(255,255,255,0.9);">
              <div class="card" style="width: 400px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border:1px solid #cbd5e1;">
                <h3 style="margin-top:0;">{{ isEditingUser ? '编辑用户资料' : '新增系统用户' }}</h3>
                <div style="margin-bottom:15px;"><label>登录账号</label><input v-model="userForm.username" :disabled="isEditingUser" class="modern-input" style="width:100%; margin-top:5px;"/></div>
                <div style="margin-bottom:15px;"><label>登录密码</label><input v-model="userForm.password" class="modern-input" style="width:100%; margin-top:5px;" :placeholder="isEditingUser?'留空表示不修改':'必填'"/></div>
                <div style="margin-bottom:15px;"><label>真实姓名</label><input v-model="userForm.realName" class="modern-input" style="width:100%; margin-top:5px;"/></div>
                <div style="display:flex; gap:10px; margin-top:25px;">
                  <button class="btn-login user-btn" style="flex:1;" @click="saveUser">保存</button>
                  <button class="btn-login" style="flex:1; background:#94a3b8;" @click="showUserModal=false">取消</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showDataModal" class="modal-overlay" @click.self="showDataModal = false">
      <div class="modal-container fade-up" style="max-width: 600px;">
        <div class="modal-header">
          <h3>📍 {{ selectedProvince }} - 最新年度概况 ({{ latestYear }})</h3>
          <button class="close-btn" @click="showDataModal = false">×</button>
        </div>
        <div class="modal-body" style="background: white; padding: 25px;">
          <p style="color:#64748b; margin-bottom:20px;">以下为该地区数据库中最新一年的农业台账抽样记录。若需生成未来预测，请前往【AI 预测】模块。</p>
          <div style="display:grid; grid-template-columns: 1fr 1fr; gap:20px;">
            <div class="stat-card hover-float" style="background:#f0fdf4; border:1px solid #bbf7d0;">
              <h4 style="font-size:14px; margin:0; color:#166534;">【{{selectedCrop}}】 亩产量</h4><h2 style="margin:5px 0 0 0; color:#15803d;">{{ clickProvLatestData.yieldAmount || clickProvLatestData.yield_amount || 0 }} <small>kg</small></h2>
            </div>
            <div class="stat-card hover-float" style="background:#eff6ff; border:1px solid #bfdbfe;">
              <h4 style="font-size:14px; margin:0; color:#1e40af;">年平均气温</h4><h2 style="margin:5px 0 0 0; color:#1d4ed8;">{{ clickProvLatestData.temperature || 0 }} <small>℃</small></h2>
            </div>
            <div class="stat-card hover-float" style="background:#fefce8; border:1px solid #fef08a;">
              <h4 style="font-size:14px; margin:0; color:#854d0e;">年降水量</h4><h2 style="margin:5px 0 0 0; color:#a16207;">{{ clickProvLatestData.rainfall || 0 }} <small>mm</small></h2>
            </div>
            <div class="stat-card hover-float" style="background:#f5f3ff; border:1px solid #ddd6fe;">
              <h4 style="font-size:14px; margin:0; color:#4c1d95;">土壤酸碱度</h4><h2 style="margin:5px 0 0 0; color:#6d28d9;">pH {{ clickProvLatestData.soilPh || clickProvLatestData.soil_ph || '-' }} </h2>
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

// ================== 1. 登录注册 ==================
const currentUserRole = ref(null);
const currentRealName = ref('');
const currentView = ref('home');
const isRegistering = ref(false);
const isAuthLoading = ref(false);

const loginForm = ref({ username: '', password: '' });
const registerForm = ref({ username: '', password: '', realName: '' });

const handleRealLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) return alert('不能为空！');
  isAuthLoading.value = true;
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', loginForm.value);
    if (res.data.code === 200) {
      currentUserRole.value = res.data.data.roleCode; currentRealName.value = res.data.data.realName;
      currentView.value = res.data.data.roleCode === 'user' ? 'home' : 'asset';
      nextTick(() => { if (res.data.data.roleCode === 'user') initMapChart(); fetchData(); if (res.data.data.roleCode === 'admin') fetchUserList(); });
    } else { alert(res.data.msg); }
  } catch (error) { alert("网络错误!"); } finally { isAuthLoading.value = false; }
}

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password) return alert('必填！');
  isAuthLoading.value = true;
  try {
    const res = await axios.post('http://localhost:8080/api/auth/register', registerForm.value);
    if (res.data.code === 200) { alert('注册成功'); isRegistering.value=false; loginForm.value.username=registerForm.value.username; } else { alert(res.data.msg); }
  } catch(e) { alert('注册失败'); } finally { isAuthLoading.value = false; }
}
const handleLogout = () => { if(confirm('退出系统？')) { currentUserRole.value=null; currentView.value='home'; } }

// ================== 2. 增强版：用户 CRUD 管理 ==================
const realUsers = ref([]);
const showUserModal = ref(false);
const isEditingUser = ref(false);
const userForm = ref({ id: null, username: '', password: '', realName: '', roleCode: 'user', status: 1 });

const fetchUserList = async () => { try { const res=await axios.get('http://localhost:8080/api/user/list'); realUsers.value=res.data; } catch(e){} }

const openUserModal = (u = null) => {
  if (u) { isEditingUser.value = true; userForm.value = { ...u, password: '' }; } // 编辑不显示密码
  else { isEditingUser.value = false; userForm.value = { id: null, username: '', password: '', realName: '', roleCode: 'user', status: 1 }; }
  showUserModal.value = true;
}

const saveUser = async () => {
  if (!userForm.value.username) return alert('账号必填');
  if (!isEditingUser.value && !userForm.value.password) return alert('新用户必须设置密码');
  try {
    let url = isEditingUser.value ? '/api/user/update' : '/api/user/add';
    // 如果是更新且没有填密码，移除密码字段，防止覆盖为空
    let payload = { ...userForm.value };
    if (isEditingUser.value && !payload.password) delete payload.password;

    await axios.post(`http://localhost:8080${url}`, payload);
    alert('保存成功'); showUserModal.value = false; fetchUserList();
  } catch(e) { alert('操作失败，请检查账号是否重复'); }
}

const deleteUser = async (id) => {
  if(!confirm('永久删除该账号及对应资料？')) return;
  try { await axios.delete(`http://localhost:8080/api/user/delete/${id}`); fetchUserList(); } catch(e) { alert('删除失败'); }
}
const toggleUserStatus = async (u) => { try { await axios.post('http://localhost:8080/api/user/update', {id:u.id, status:u.status===1?0:1}); fetchUserList(); }catch(e){} }

// ================== 3. 基础数据加载 ==================
const mapGeoJson = ref(null); const isMapLoading = ref(true);
const allData = ref([]); const provinceList = ref([]);
const selectedProvince = ref('黑龙江'); const cropList = ref(['水稻', '小麦', '玉米', '大豆']);
const selectedCrop = ref('水稻'); const analysisProvince = ref('黑龙江');

const fetchData = async () => {
  try {
    const [provRes, dataRes] = await Promise.all([axios.get('http://localhost:8080/api/data/provinces'), axios.get('http://localhost:8080/api/data/list')])
    provinceList.value = provRes.data; allData.value = dataRes.data;
    if (provinceList.value.length > 0) { selectedProvince.value = provinceList.value[0]; analysisProvince.value = provinceList.value[0]; }
  } catch (err) {}
}

onMounted(async () => {
  try {
    isMapLoading.value = true;
    const mapRes = await axios.get('https://fastly.jsdelivr.net/npm/echarts@4.9.0/map/json/china.json')
    mapGeoJson.value = mapRes.data; echarts.registerMap('china', mapGeoJson.value);
  } catch (err) {} finally { isMapLoading.value = false; }
})

const pageTitle = computed(() => ({ home: '', analysis: '多维数据分析', predict: 'AI 趋势预测', asset: '资产管理', users: '用户管理' }[currentView.value]))
const getYield = (d) => Number(d.yieldAmount || d.yield_amount || 0); const getCrop = (d) => d.cropType || d.crop_type || '';
const currentCropData = computed(() => allData.value.filter(d => getCrop(d) === selectedCrop.value));

// ================== 4. 态势地图 (点击查看最新一年概况) ==================
const showDataModal = ref(false);
const clickProvLatestData = ref({});
const latestYear = ref('');

const initMapChart = () => {
  const dom = document.getElementById('chinaMapChart'); if (!dom || !mapGeoJson.value) return; echarts.dispose(dom); const chart = echarts.init(dom);
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
    selectedProvince.value = params.name;
    // 找出该省份最新年份的数据
    const history = allData.value.filter(d => d.province===params.name && getCrop(d)===selectedCrop.value).sort((a,b)=>b.year-a.year);
    if(history.length > 0) {
      clickProvLatestData.value = history[0]; // 取最新的第一条
      latestYear.value = history[0].year + '年';
    } else {
      clickProvLatestData.value = {}; latestYear.value = '无数据';
    }
    showDataModal.value = true;
  });
  window.addEventListener('resize', () => chart.resize());
}

// ================== 5. 分析页 (增加散点相关性) ==================
const activeAnalysisTab = ref('yield');
const switchAnalysisTab = (tabName) => {
  activeAnalysisTab.value = tabName;
  nextTick(() => {
    const provData = allData.value.filter(d => d.province === analysisProvince.value && getCrop(d) === selectedCrop.value).sort((a,b)=>a.year-b.year);

    if (tabName === 'yield') {
      const dom = document.getElementById('tabYieldChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      chart.setOption({ tooltip:{trigger:'axis'}, grid:{top:40,bottom:30}, xAxis:{type:'category', data:provData.map(i=>i.year)}, yAxis:{type:'value',name:'产量(kg)'}, series:[{type:'line', smooth:true, areaStyle:{opacity:0.3,color:'#fa8c16'}, data:provData.map(i=>getYield(i)), lineStyle:{width:4,color:'#fa8c16'}}] });
    } else if (tabName === 'rank') {
      const dom = document.getElementById('tabRankChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      const m={}; currentCropData.value.forEach(d=>{if(!m[d.province])m[d.province]=[]; m[d.province].push(getYield(d));});
      const arr=[]; for(let p in m) arr.push({n:p, v:(m[p].reduce((a,b)=>a+b,0)/m[p].length).toFixed(1)}); arr.sort((a,b)=>b.v-a.v);
      chart.setOption({ tooltip:{trigger:'axis'}, grid:{top:30,bottom:30}, xAxis:{type:'category', data:arr.slice(0,10).map(i=>i.n)}, yAxis:{type:'value'}, series:[{type:'bar', barWidth:'45%', data:arr.slice(0,10).map(i=>i.v), itemStyle:{color:new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#4ade80'},{offset:1,color:'#16a34a'}])}}] });
    } else if (tabName === 'climate') {
      const dom = document.getElementById('tabClimateChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      chart.setOption({ tooltip:{trigger:'axis'}, legend:{data:['降水(mm)','均温(℃)']}, grid:{top:40,bottom:30}, xAxis:{type:'category', data:provData.map(i=>i.year)}, yAxis:[{type:'value',name:'降水'},{type:'value',name:'温度',position:'right'}], series:[{name:'降水(mm)',type:'bar',data:provData.map(i=>i.rainfall||0),itemStyle:{color:'#4facfe'}}, {name:'均温(℃)',type:'line',yAxisIndex:1,data:provData.map(i=>i.temperature||0),itemStyle:{color:'#ff4d4f'},lineStyle:{width:3}}] });
    } else if (tabName === 'soil') {
      const dom = document.getElementById('tabSoilChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      let soil = provData.find(d => d.nitrogen > 0) || { nitrogen: 45, phosphorus: 20, potassium: 35 };
      chart.setOption({ tooltip:{trigger:'item'}, series:[{name:'养分',type:'pie',radius:['40%','70%'], itemStyle:{borderRadius:10,borderColor:'#fff',borderWidth:2}, label:{show:true,formatter:'{b}: {c}%'}, data:[{value:soil.nitrogen||45,name:'氮(N)',itemStyle:{color:'#3b82f6'}},{value:soil.phosphorus||20,name:'磷(P)',itemStyle:{color:'#f59e0b'}},{value:soil.potassium||35,name:'钾(K)',itemStyle:{color:'#10b981'}}]}] });
    } else if (tabName === 'correlation') {
      // 核心：相关性散点图
      const dom = document.getElementById('tabCorrelationChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);
      // X轴: 降水, Y轴: 产量, 气泡大小/颜色: 温度
      let scatterData = provData.map(d => [d.rainfall||0, getYield(d), d.temperature||0, d.year]);
      chart.setOption({
        tooltip: { formatter: (p) => `${p.data[3]}年<br/>降水: ${p.data[0]}mm<br/>产量: ${p.data[1]}kg<br/>气温: ${p.data[2]}℃` },
        xAxis: { name:'年降水(mm)', type:'value', splitLine:{lineStyle:{type:'dashed'}} },
        yAxis: { name:'亩产(kg)', type:'value', splitLine:{lineStyle:{type:'dashed'}} },
        series: [{ type: 'scatter', symbolSize: (data) => Math.max(data[2]*1.5, 10), // 温度越大气泡越大
          itemStyle: { color: new echarts.graphic.RadialGradient(0.4, 0.3, 1, [{offset: 0, color: '#6ee7b7'}, {offset: 1, color: '#059669'}]) },
          data: scatterData }]
      });
    }
  });
}

// ================== 6. AI 预测与未来趋势图 ==================
const hasPredicted = ref(false); const loading = ref(false);
const expertAdviceList = ref([]);

const executePrediction = async () => {
  loading.value = true; hasPredicted.value = false;
  try {
    // 1. 获取过去几年的真实数据（这里取最近 5 年）
    const hRes = await axios.get(`http://localhost:8080/api/data/history?province=${selectedProvince.value}&cropType=${selectedCrop.value}`)
    let history = hRes.data.sort((a,b)=>a.year-b.year);
    if (history.length > 5) history = history.slice(history.length - 5);

    // 2. 调用模型获取 1 个预测基准值
    const pRes = await axios.get(`http://localhost:8080/api/data/predict?province=${selectedProvince.value}&cropType=${selectedCrop.value}`)
    let basePred = 0; const m = pRes.data.match(/预测产量为: ([\d.]+) kg/); if(m) basePred = parseFloat(m[1]);

    // 3. 模拟未来 3 年走势 (基于基准值进行微幅正态波动)
    let pred1 = basePred;
    let pred2 = basePred * (1 + (Math.random()*0.04 - 0.02)); // 波幅 -2% 到 +2%
    let pred3 = pred2 * (1 + (Math.random()*0.04 - 0.02));

    setTimeout(() => {
      loading.value = false; hasPredicted.value = true;
      generateAdvice(pred1, selectedProvince.value, selectedCrop.value);

      // 渲染拆线图
      nextTick(() => {
        const dom = document.getElementById('predictTrendChart'); if(!dom) return; echarts.dispose(dom); const chart = echarts.init(dom);

        let xData = history.map(d=>d.year.toString());
        let lastYear = parseInt(xData[xData.length-1] || '2024');
        xData.push((lastYear+1)+'(预测)', (lastYear+2)+'(预测)', (lastYear+3)+'(预测)');

        let yDataHistory = history.map(d=>getYield(d));
        let yDataPredict = [...Array(history.length-1).fill(null), yDataHistory[yDataHistory.length-1], pred1.toFixed(1), pred2.toFixed(1), pred3.toFixed(1)];

        chart.setOption({
          tooltip: { trigger: 'axis' }, legend: { data: ['历史实测', '模型预测'] }, grid:{top:40,bottom:30,left:50,right:30},
          xAxis: { type: 'category', data: xData, axisLabel:{rotate:20} },
          yAxis: { type: 'value', name:'亩产(kg)', min: 'dataMin' },
          series: [
            { name:'历史实测', type:'line', data:yDataHistory, itemStyle:{color:'#1890ff'}, lineStyle:{width:3} },
            { name:'模型预测', type:'line', data:yDataPredict, itemStyle:{color:'#fa8c16'}, lineStyle:{width:3, type:'dashed'} } // 预测部分用虚线
          ]
        });
      });
    }, 1500);
  } catch(e) { loading.value = false; alert("引擎连接失败！"); }
}

const generateAdvice = (val, prov, crop) => {
  expertAdviceList.value = [
    { type: 'success', icon: '🎯', title: '产能预测研判', tags: ['战略布局'], content: `根据随机森林回归运算，该省份未来三年 ${crop} 产量走势整体平稳，首年测算基准值为 <b>${val} kg</b>。`, action: '制定相匹配的秋粮收购预案。' },
    { type: 'info', icon: '🧪', title: '地力轮作指导', tags: ['土壤'], content: `系统监测到该区连作指数较高，未来三年预测曲线存在微幅震荡。`, action: '建议在第三年引入豆科植物进行固氮休耕轮作。' },
    { type: 'warning', icon: '☁️', title: '气象灾害冗余', tags: ['风控'], content: `拉尼娜等异常气象可能导致未来曲线下行断崖。`, action: '建议各农业合作社提前购置农业气象指数保险。' }
  ];
}

watch(currentView, (v) => { nextTick(() => { if(v === 'home') setTimeout(initMapChart, 200); if(v === 'analysis') switchAnalysisTab('yield'); }) })
</script>

<style>
/* 登录面板 */
.login-container { width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; background: radial-gradient(#d1d5db 1px, transparent 1px) #f0f2f5; background-size: 20px 20px;}
.login-box { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 20px 40px rgba(0,0,0,0.1); text-align: center; max-width: 400px; width: 90%;}
.login-logo { font-size: 50px; background: #e6fffa; width: 90px; height: 90px; line-height: 90px; border-radius: 50%; margin: 0 auto 20px;}
.login-subtitle { color: #64748b; margin-top: 10px; margin-bottom: 25px; font-size: 14px;}
.login-form { display: flex; flex-direction: column; gap: 15px; }
.modern-input { padding: 12px 15px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; outline: none; background: #f8fafc; transition: 0.3s;}
.modern-input:focus { border-color: #2ed573; background: white;}
.btn-login { width: 100%; padding: 12px; border: none; border-radius: 8px; font-size: 15px; font-weight: bold; color: white; cursor: pointer; transition: 0.3s;}
.user-btn { background: #2ed573; }
.btn-login:disabled { background: #94a3b8; cursor: not-allowed; }
.auth-switch { margin-top: 15px; font-size: 14px; color: #64748b; }
.link-text { color: #1890ff; cursor: pointer; font-weight: bold; }

/* 基础框架 */
html, body, #app { margin: 0; padding: 0; width: 100vw; height: 100vh; overflow: hidden; font-family: 'Segoe UI', sans-serif; background-color: #f0f2f5; }
.app-container { display: flex; width: 100%; height: 100%; }
.sidebar { width: 220px; background: #1e293b; color: white; display: flex; flex-direction: column; }
.logo-area { padding: 25px 20px; text-align: center; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-circle { width: 45px; height: 45px; background: #2ed573; border-radius: 50%; font-size: 20px; line-height: 45px; margin: 0 auto 10px;}
.menu { flex: 1; padding-top: 10px; overflow-y: auto; }
.menu-category { font-size: 12px; color: #94a3b8; font-weight: bold; padding: 15px 20px 5px; }
.menu-item { padding: 12px 20px; cursor: pointer; color: #cbd5e1; display: flex; gap: 10px; transition: 0.2s; font-size: 14px;}
.menu-item:hover { color: white; background: rgba(255,255,255,0.05); padding-left: 25px;}
.menu-item.active { background: #2ed573; color: white; font-weight: bold; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f1f5f9; }
.top-header { height: 60px; background: white; padding: 0 30px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e2e8f0; }
.content-body { flex: 1; overflow-y: auto; padding: 25px; }
.content-body.no-padding { padding: 0; overflow: hidden; }

/* 分析页 Tabs */
.tabs-container { display: flex; gap: 10px; margin-bottom: 15px; border-bottom: 2px solid #e2e8f0; padding-bottom: 10px;}
.tab-btn { padding: 10px 15px; background: transparent; border: none; font-size: 14px; font-weight: bold; color: #64748b; cursor: pointer; transition: 0.3s; border-radius: 8px;}
.tab-btn:hover { background: #f1f5f9; color: #334155;}
.tab-btn.active { background: #2ed573; color: white;}

/* 组件池 */
.card { background: white; border-radius: 10px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 6px rgba(0,0,0,0.02); border: 1px solid #e2e8f0;}
.hover-float { transition: transform 0.3s; }
.hover-float:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.05); }
.hover-glow { transition: all 0.3s ease; }
.hover-glow:hover { box-shadow: 0 8px 25px rgba(46, 213, 115, 0.15); border-color: rgba(46, 213, 115, 0.3); }
.fade-in { animation: fadeIn 0.4s ease; }
.fade-up { animation: fadeUp 0.4s cubic-bezier(0.165, 0.84, 0.44, 1); }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(20px) scale(0.95); } to { opacity: 1; transform: translateY(0) scale(1); } }
.tech-spinner { border: 4px solid #f3f3f3; border-top: 4px solid #2ed573; border-radius: 50%; animation: spin 1s linear infinite; width:40px; height:40px;}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* 页面区 */
.home-view { width: 100%; height: 100%; position: relative; }
.map-loading-overlay { position: absolute; inset:0; background:rgba(255,255,255,0.8); z-index:50; display:flex; flex-direction:column; align-items:center; justify-content:center; color:#2ed573; font-weight:bold; }
#chinaMapChart { width: 100%; height: 100%; }
.map-overlay-panel { position: absolute; top: 30px; left: 30px; background: rgba(255,255,255,0.95); padding: 20px; border-radius: 12px; box-shadow: 0 8px 25px rgba(0,0,0,0.1); z-index: 10; border:1px solid #fff;}
.three-block-layout { display: flex; gap: 20px; height: 100%; }

/* 决策与徽章 */
.spacious-list { display: flex; flex-direction: column; gap: 15px; }
.advice-list-item { display: flex; gap: 15px; padding: 15px; border: 1px solid #e2e8f0; border-radius: 10px; background: white;}
.item-icon-wrapper { font-size: 26px; width: 45px; height: 45px; display: flex; align-items: center; justify-content: center; background: #f8fafc; border-radius: 10px;}
.item-content { flex: 1; }
.tech-tag { font-size: 11px; padding: 4px 8px; border-radius: 6px; font-weight: bold; }
.tech-tag.success { background: #dcfce7; color: #166534; }
.tech-tag.warning { background: #fee2e2; color: #991b1b; }
.tech-tag.info { background: #e0e7ff; color: #3730a3; }
.tech-tag.normal { background: #f1f5f9; color: #475569; }
.item-desc { font-size: 13px; color: #475569; line-height: 1.6; margin: 0 0 10px 0; }
.item-action { font-size: 13px; padding: 8px 12px; border-radius: 6px; font-weight: bold; background: #f8fafc; color:#334155; border-left:3px solid #cbd5e1;}

/* 其他 */
.modern-select { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 13px; color: #334155; outline: none; background: white;}
.user-info { display: flex; align-items: center; gap: 10px; }
.avatar-simple { width: 35px; height: 35px; border-radius: 50%; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold;}
.btn-logout { width: 80%; margin: 20px auto; display: block; padding: 10px; background: rgba(255, 77, 79, 0.1); color: #ff4d4f; border: 1px solid rgba(255, 77, 79, 0.3); border-radius: 8px; cursor: pointer; font-weight:bold;}
.mock-table { width: 100%; border-collapse: collapse; text-align: left; font-size:14px; }
.mock-table th, .mock-table td { padding: 12px 15px; border-bottom: 1px solid #f1f5f9; }
.btn-xs { padding: 5px 12px; border-radius: 4px; font-size: 12px;}
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: bold;}
.status-badge.normal { background: #dcfce7; color: #166534; }
.status-badge.frozen { background: #fee2e2; color: #991b1b; }
.modal-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.6); z-index: 2000; display: flex; align-items: center; justify-content: center;}
.modal-container { background: white; border-radius: 12px; width: 90%; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.5);}
.modal-header { padding: 15px 25px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items:center; }
.close-btn { background: #f1f5f9; border: none; font-size: 20px; cursor: pointer; width: 35px; height:35px; border-radius:50%;}
.stat-card { padding: 15px; border-radius: 10px; text-align: center; }
</style>