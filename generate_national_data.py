import pandas as pd
import random
from sqlalchemy import create_engine

# 1. 连接数据库
engine = create_engine('mysql+pymysql://root:123456@localhost:3306/agri_yield_db')

def generate_china_data():
    print("🚀 正在生成全国 34 个省级行政区的数据...")

    # 定义区域特征，让数据更真实
    regions = {
        '东北': {'provinces': ['黑龙江', '吉林', '辽宁'], 'temp': (10, 22), 'rain': (400, 700), 'base_yield': 550},
        '华北': {'provinces': ['北京', '天津', '河北', '山西', '内蒙古'], 'temp': (12, 25), 'rain': (300, 600), 'base_yield': 500},
        '华东': {'provinces': ['上海', '江苏', '浙江', '安徽', '福建', '江西', '山东', '台湾'], 'temp': (18, 28), 'rain': (800, 1400), 'base_yield': 600},
        '华中': {'provinces': ['河南', '湖北', '湖南'], 'temp': (16, 27), 'rain': (700, 1200), 'base_yield': 580},
        '华南': {'provinces': ['广东', '广西', '海南', '香港', '澳门'], 'temp': (22, 30), 'rain': (1200, 2000), 'base_yield': 620},
        '西南': {'provinces': ['重庆', '四川', '贵州', '云南', '西藏'], 'temp': (14, 26), 'rain': (600, 1100), 'base_yield': 530},
        '西北': {'provinces': ['陕西', '甘肃', '青海', '宁夏', '新疆'], 'temp': (8, 24), 'rain': (100, 400), 'base_yield': 450}
    }

    data_list = []
    years = range(2015, 2025) # 生成过去 10 年的数据

    for region_name, info in regions.items():
        for prov in info['provinces']:
            print(f"  -> 正在模拟: {prov} ({region_name})")
            for year in years:
                # 模拟气候波动
                temp_min, temp_max = info['temp']
                rain_min, rain_max = info['rain']

                # 加上年份带来的技术增长 (每年增产 1% 左右)
                tech_boost = (year - 2015) * 5

                # 生成单条数据
                temperature = round(random.uniform(temp_min, temp_max), 1)
                rainfall = round(random.uniform(rain_min, rain_max), 1)
                soil_ph = round(random.uniform(5.5, 7.5), 1)
                soil_humidity = round(random.uniform(40, 80), 1)

                # 产量公式 (模拟)：基础产量 + 气候影响 + 随机波动 + 技术增长
                yield_amount = info['base_yield'] + \
                               (rainfall * 0.05) + \
                               (temperature * 2) + \
                               random.uniform(-30, 30) + \
                               tech_boost

                data_list.append({
                    'province': prov,
                    'year': year,
                    'temperature': temperature,
                    'rainfall': rainfall,
                    'soil_ph': soil_ph,
                    'soil_humidity': soil_humidity,
                    'yield_amount': round(yield_amount, 2)
                })

    df = pd.DataFrame(data_list)

    # ⚠️ replace 会清空旧表，写入新表，保证数据干净
    df.to_sql('agriculture_data', con=engine, if_exists='replace', index=False, index_label='id')

    # 因为 replace 可能会丢掉主键自增属性，建议在数据库里检查一下 id 列
    # 但为了简单，我们先这样存进去
    print(f"✅ 成功生成 {len(df)} 条全国数据！请去网页查看。")

if __name__ == "__main__":
    generate_china_data()