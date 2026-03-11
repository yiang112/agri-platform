<template>
  <div class="manager-container">
    <div class="toolbar">
      <h3>📊 农业数据库管理中心</h3>
      <div class="actions">
        <button class="btn-refresh" @click="fetchData">🔄 刷新列表</button>
      </div>
    </div>

    <div class="table-wrapper">
      <table class="data-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>省份</th>
          <th>年份</th>
          <th>温度(℃)</th>
          <th>降水(mm)</th>
          <th>产量(kg)</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in dataList" :key="item.id">
          <td>{{ item.id }}</td>
          <td><span class="tag-province">{{ item.province }}</span></td>
          <td>{{ item.year }}</td>
          <td>{{ item.temperature }}</td>
          <td>{{ item.rainfall }}</td>
          <td class="bold-text">{{ item.yieldAmount }}</td>
          <td>
            <div class="btn-group">
              <button class="btn-edit" @click="openEditModal(item)">✏️ 编辑</button>
              <button class="btn-delete" @click="deleteData(item.id)">🗑️ 删除</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination">
      <span>共 {{ dataList.length }} 条数据</span>
    </div>

    <div v-if="showEditModal" class="modal-overlay">
      <div class="edit-modal">
        <div class="modal-header">
          <h4>📝 修改数据 (ID: {{ editForm.id }})</h4>
          <span class="close-icon" @click="closeEditModal">×</span>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <div class="form-item">
              <label>省份</label>
              <input v-model="editForm.province" disabled class="input-disabled" />
              <span class="tip">省份不可修改</span>
            </div>
            <div class="form-item">
              <label>年份</label>
              <input v-model="editForm.year" type="number" />
            </div>
            <div class="form-item">
              <label>平均气温 (℃)</label>
              <input v-model="editForm.temperature" type="number" step="0.1" />
            </div>
            <div class="form-item">
              <label>降水量 (mm)</label>
              <input v-model="editForm.rainfall" type="number" step="0.1" />
            </div>
            <div class="form-item">
              <label>土壤pH值</label>
              <input v-model="editForm.soilPh" type="number" step="0.1" />
            </div>
            <div class="form-item">
              <label>实际产量 (kg)</label>
              <input v-model="editForm.yieldAmount" type="number" step="0.01" class="input-highlight" />
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn-cancel" @click="closeEditModal">取消</button>
          <button class="btn-save" @click="saveEdit">💾 保存修改</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const dataList = ref([])
const showEditModal = ref(false)
// 定义一个对象来存正在编辑的数据
const editForm = ref({})

// === 1. 获取数据 ===
const fetchData = async () => {
  try {
    // 这里用 /list 接口，如果你用的是 /all 请自行修改
    const res = await axios.get('http://localhost:8080/api/data/list')
    dataList.value = res.data
  } catch (error) {
    console.error("加载失败", error)
  }
}

// === 2. 删除数据 ===
const deleteData = async (id) => {
  if(!confirm('确定要删除这条数据吗？删除后不可恢复！')) return;
  try {
    await axios.delete(`http://localhost:8080/api/data/delete?id=${id}`)
    alert('删除成功')
    fetchData() // 刷新列表
  } catch (error) {
    alert('删除失败')
  }
}

// === 3. 打开编辑弹窗 ===
const openEditModal = (row) => {
  // 关键：必须用 JSON 序列化深拷贝，否则修改弹窗时，表格里的字也会跟着变，体验不好
  editForm.value = JSON.parse(JSON.stringify(row))
  showEditModal.value = true
}

// === 4. 关闭弹窗 ===
const closeEditModal = () => {
  showEditModal.value = false
}

// === 5. 保存修改 ===
const saveEdit = async () => {
  try {
    // 发送 POST 请求给后端
    const res = await axios.post('http://localhost:8080/api/data/update', editForm.value)

    // 判断后端返回是不是 "修改成功"
    if (res.data === '修改成功' || res.status === 200) {
      alert('保存成功！')
      showEditModal.value = false
      fetchData() // 刷新列表看结果
    } else {
      alert('保存失败：' + res.data)
    }
  } catch (error) {
    console.error(error)
    alert('保存出错，请检查后端控制台')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* 容器样式 */
.manager-container { padding: 20px; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); height: 100%; display: flex; flex-direction: column; }

/* 顶部工具栏 */
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px solid #f0f2f5; }
.toolbar h3 { margin: 0; color: #333; font-size: 18px; }
.btn-refresh { background: #f0f2f5; border: 1px solid #d9d9d9; padding: 8px 15px; border-radius: 4px; cursor: pointer; transition: 0.3s; }
.btn-refresh:hover { color: #1890ff; border-color: #1890ff; }

/* 表格区域 */
.table-wrapper { flex: 1; overflow-y: auto; border: 1px solid #eee; border-radius: 4px; }
.data-table { width: 100%; border-collapse: collapse; min-width: 800px; }
.data-table th { background: #fafafa; padding: 12px; text-align: left; font-weight: 600; color: #555; position: sticky; top: 0; border-bottom: 1px solid #eee; }
.data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; color: #666; font-size: 14px; }
.data-table tr:hover { background: #f6ffed; } /* 鼠标悬停变淡绿色 */

/* 标签与按钮 */
.tag-province { background: #e6f7ff; color: #1890ff; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.bold-text { font-weight: bold; color: #333; }

.btn-group { display: flex; gap: 8px; }
.btn-edit { background: #1890ff; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-delete { background: #ff4d4f; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; }
.btn-edit:hover { background: #40a9ff; }
.btn-delete:hover { background: #ff7875; }

/* 分页 */
.pagination { margin-top: 15px; text-align: right; color: #999; font-size: 13px; }

/* === 编辑弹窗样式 === */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 3000;
  display: flex; justify-content: center; align-items: center;
}
.edit-modal {
  background: white; width: 500px; border-radius: 8px; padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2); animation: popIn 0.3s;
}
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.modal-header h4 { margin: 0; color: #333; }
.close-icon { cursor: pointer; font-size: 24px; color: #999; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.form-item { display: flex; flex-direction: column; gap: 5px; }
.form-item label { font-size: 13px; color: #666; }
.form-item input { padding: 8px; border: 1px solid #d9d9d9; border-radius: 4px; outline: none; }
.form-item input:focus { border-color: #1890ff; box-shadow: 0 0 0 2px rgba(24,144,255,0.2); }
.input-disabled { background: #f5f5f5; color: #999; cursor: not-allowed; }
.input-highlight { border-color: #2ed573; background: #f6ffed; font-weight: bold; }
.tip { font-size: 12px; color: #ccc; }

.modal-footer { margin-top: 25px; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: white; border: 1px solid #d9d9d9; padding: 8px 20px; border-radius: 4px; cursor: pointer; }
.btn-save { background: #2ed573; color: white; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-save:hover { background: #26af61; }

@keyframes popIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>