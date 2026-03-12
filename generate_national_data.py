import pandas as pd
import random
from sqlalchemy import create_engine

# 1. 连接数据库 (请确保密码是你自己的)
engine = create_engine('mysql+pymysql://root:123456@localhost:3306/agri_yield_db')

def generate_china_data():
    print("🚀 正在生成全国 34 个省份 × 4 种主粮 的多维农业数据...")

    regions = {
        '东北': {'provinces': ['黑龙江', '吉林', '辽宁'], 'temp': (10, 22), 'rain': (400, 700)},
        '华北': {'provinces': ['北京', '天津', '河北', '山西', '内蒙古'], 'temp': (12, 25), 'rain': (300, 600)},
        '华东': {'provinces': ['上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '台湾'], 'temp': (18, 28), 'rain': (800, 1400)},
        '华中': {'provinces': ['河南', '湖北', '湖南'], 'temp': (16, 27), 'rain': (700, 1200)},
        '华南': {'provinces': ['广东', '广西', '海南', '香港', '澳门'], 'temp': (22, 30), 'rain': (1200, 2000)},
        '西南': {'provinces': ['重庆', '四川', '贵州', '云南', '西藏'], 'temp': (14, 26), 'rain': (600, 1100)},
        '西北': {'provinces': ['陕西', '甘肃', '青海', '宁夏', '新疆'], 'temp': (8, 24), 'rain': (100, 400)}
    }

    # 定义农作物的基础属性
    crops = {
        '水稻': {'base_yield': 600, 'rain_weight': 0.08, 'temp_weight': 1.5},
        '小麦': {'base_yield': 400, 'rain_weight': 0.04, 'temp_weight': 2.0},
        '玉米': {'base_yield': 500, 'rain_weight': 0.05, 'temp_weight': 2.2},
        '大豆': {'base_yield': 200, 'rain_weight': 0.03, 'temp_weight': 1.2}
    }

    data_list = []
    years = range(2015, 2025)

    for region_name, info in regions.items():
        for prov in info['provinces']:
            for crop_name, crop_info in crops.items():
                for year in years:
                    temp_min, temp_max = info['temp']
                    rain_min, rain_max = info['rain']
                    tech_boost = (year - 2015) * (crop_info['base_yield'] * 0.01) # 每年1%技术增长

                    temperature = round(random.uniform(temp_min, temp_max), 1)
                    rainfall = round(random.uniform(rain_min, rain_max), 1)
                    soil_ph = round(random.uniform(5.5, 7.5), 1)
                    soil_humidity = round(random.uniform(40, 80), 1)

                    # 差异化产量公式
                    yield_amount = crop_info['base_yield'] + \
                                   (rainfall * crop_info['rain_weight']) + \
                                   (temperature * crop_info['temp_weight']) + \
                                   random.uniform(-20, 20) + tech_boost

                    data_list.append({
                        'province': prov,
                        'crop_type': crop_name,  # 关键：存入作物类型
                        'year': year,
                        'temperature': temperature,
                        'rainfall': rainfall,
                        'soil_ph': soil_ph,
                        'soil_humidity': soil_humidity,
                        'yield_amount': round(yield_amount, 2)
                    })

    df = pd.DataFrame(data_list)
    df.to_sql('agriculture_data', con=engine, if_exists='replace', index=False, index_label='id')
    print(f"✅ 成功生成 {len(df)} 条多品类农业数据！")

if __name__ == "__main__":
    generate_china_data()