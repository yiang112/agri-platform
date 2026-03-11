import joblib
import pandas as pd
import sys
import os
import warnings

# 忽略警告，保持输出干净
warnings.filterwarnings("ignore")

# ===========================
# 1. 接收参数 (从 Java 传过来的)
# ===========================
# 我们约定参数顺序：python predict_script.py [省份] [年份] [温度] [降水] [pH] [湿度]
# 如果没有参数，就用默认值测试
if len(sys.argv) > 1:
    province = sys.argv[1]
    year = int(sys.argv[2])
    temperature = float(sys.argv[3])
    rainfall = float(sys.argv[4])
    soil_ph = float(sys.argv[5])
    soil_humidity = float(sys.argv[6])
else:
    # 默认测试数据
    province = "黑龙江"
    year = 2025
    temperature = 20.5
    rainfall = 600.0
    soil_ph = 6.5
    soil_humidity = 50.0

# ===========================
# 2. 加载模型和特征列名
# ===========================
model_path = 'models/yield_model.pkl'
columns_path = 'models/model_columns.pkl'

if not os.path.exists(model_path):
    print("错误: 找不到模型文件，请先运行 train_model.py")
    sys.exit(1)

model = joblib.load(model_path)
model_columns = joblib.load(columns_path) # 加载"暗号本"

# ===========================
# 3. 数据预处理 (最关键！)
# ===========================
# 构造输入数据的字典
input_data = {
    'province': [province],
    'year': [year],
    'temperature': [temperature],
    'rainfall': [rainfall],
    'soil_ph': [soil_ph],
    'soil_humidity': [soil_humidity],
    # 下面这些如果模型需要但我们没有，给个默认值
    'nitrogen': [0],
    'phosphorus': [0],
    'potassium': [0],
}

# 转成 DataFrame
df = pd.DataFrame(input_data)

# 进行独热编码 (变成 province_黑龙江, province_河南...)
df_encoded = pd.get_dummies(df, columns=['province'])

# ⚡⚡⚡ 核心黑科技：对齐列名 ⚡⚡⚡
# 训练时有 "province_安徽"，但如果现在输入是 "黑龙江"，生成的 df 里就没有 "province_安徽" 这个列。
# reindex 会强制把缺少的列补上 0，多余的列删掉，确保和训练时一模一样！
df_ready = df_encoded.reindex(columns=model_columns, fill_value=0)

# ===========================
# 4. 预测与输出
# ===========================
try:
    prediction = model.predict(df_ready)
    result = prediction[0]

    # 输出结果给 Java
    print(f"预测省份: {province}")
    print(f"当前环境预测产量为: {result:.2f} kg/亩")

    # 简单的决策逻辑
    if result < 450:
        print("决策建议: [警告] 该省份预期产量偏低，建议增加水肥管理。")
    elif result > 600:
        print("决策建议: [喜讯] 环境适宜，预期丰收！")
    else:
        print("决策建议: [正常] 产量预期稳定，请保持日常养护。")

except Exception as e:
    print(f"预测出错: {str(e)}")