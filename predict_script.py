import sys
import pandas as pd
import joblib

def predict():
    try:
        # 接收两个参数：省份 和 农作物
        province = sys.argv[1]
        crop_type = sys.argv[2]

        # 模拟气候数据（这里可根据省份优化，暂用均值）
        input_data = {
            'year': [2025], 'temperature': [20.0], 'rainfall': [800.0],
            'soil_ph': [6.5], 'soil_humidity': [60.0],
            'nitrogen': [50.0], 'phosphorus': [30.0], 'potassium': [20.0],
            'province': [province],
            'crop_type': [crop_type] # 加入农产品类型
        }

        df_input = pd.DataFrame(input_data)
        # 对省份和农产品独热编码
        df_input_encoded = pd.get_dummies(df_input, columns=['province', 'crop_type'])

        model_columns = joblib.load('models/model_columns.pkl')
        for col in model_columns:
            if col not in df_input_encoded.columns:
                df_input_encoded[col] = 0
        df_input_encoded = df_input_encoded[model_columns]

        model = joblib.load('models/yield_model.pkl')
        prediction = model.predict(df_input_encoded)[0]

        print(f"预测产量为: {prediction:.2f} kg")
        print("决策建议:[正常] 建议根据品种特性进行精准田间管理。")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    predict()