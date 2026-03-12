import pandas as pd
from sqlalchemy import create_engine
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestRegressor
import joblib
import os

# ===========================
# 1. 连接数据库获取爬虫数据
# ===========================
print("🚀 正在连接数据库...")
# 请确保密码是正确的 (这里假设是 123456)
db_connection_str = 'mysql+pymysql://root:123456@localhost:3306/agri_yield_db'
db_connection = create_engine(db_connection_str)

# 读取数据 (现在的表里应该有 province, year, temperature, rainfall, soil_ph, soil_humidity, yield_amount)
df = pd.read_sql("SELECT * FROM agriculture_data", db_connection)
print(f"📊 成功读取到 {len(df)} 条数据")

# ===========================
# 2. 数据预处理 (最关键的一步！)
# ===========================
# 机器看不懂 "黑龙江"，我们需要把它变成数字。
# pd.get_dummies 会自动把 'province' 列炸开，变成 'province_黑龙江', 'province_河南'...
df_encoded = pd.get_dummies(df, columns=['province'])

# ... 之前连接数据库的代码不变 ...

# 提取特征和目标变量
# 注意：我们要保留 crop_type，把 province 和 crop_type 都作为特征
drop_columns = ['id', 'yield_amount', 'create_time'] # 删掉这里面的 crop_type（如果有的话）
X_raw = df.drop(drop_columns, axis=1, errors='ignore')
y = df['yield_amount']

# 对省份和农作物都进行 One-Hot 编码
X = pd.get_dummies(X_raw, columns=['province', 'crop_type'])

# 保存特征列顺序
import joblib
joblib.dump(X.columns.tolist(), 'models/model_columns.pkl')

# ... 后面的拆分数据集和 RandomForestRegressor 训练代码保持不变 ...

print("🧠 特征处理完毕，训练所使用的特征列名如下：")
print(list(X.columns))

# ===========================
# 3. 训练模型
# ===========================
print("🤖 开始训练随机森林模型...")
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = RandomForestRegressor(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

score = model.score(X_test, y_test)
print(f"✅ 训练完成！模型准确率 (R2 Score): {score:.4f}")

# ===========================
# 4. 保存模型 (存两个文件！)
# ===========================
# 确保 models 文件夹存在
if not os.path.exists('models'):
    os.makedirs('models')

# 保存 1: 模型本体
joblib.dump(model, 'models/yield_model.pkl')

# 保存 2: 特征列名 (这非常重要！！！)
# 预测的时候，Python 必须知道"省份"的顺序，否则会乱套
joblib.dump(X.columns, 'models/model_columns.pkl')

print("💾 模型已保存到 models/yield_model.pkl")
print("💾 特征列已保存到 models/model_columns.pkl (预测脚本需要用到它)")