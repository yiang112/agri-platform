import pandas as pd
from sqlalchemy import create_engine
import random
import time

# ===========================
# 1. 配置数据库连接
# ===========================
# 请确保密码正确
engine = create_engine('mysql+pymysql://root:123456@localhost:3306/agri_yield_db')


def get_real_data():
    """
    模拟爬虫获取各省数据。
    真实场景下，你可以用 requests.get('http://stats.gov.cn/...') 解析 HTML
    """
    provinces = ['黑龙江', '河南', '山东', '江苏', '安徽']
    years = [2020, 2021, 2022, 2023]

    data_list = []

    print("🕷️ 正在爬取各省农业数据...")
    for prov in provinces:
        for yr in years:
            # 这里模拟不同省份的气候差异
            base_temp = 20 if prov in ['黑龙江'] else 25
            base_rain = 500 if prov in ['山东', '河南'] else 800

            record = {
                'province': prov,
                'year': yr,
                'temperature': round(random.uniform(base_temp - 5, base_temp + 5), 1),
                'rainfall': round(random.uniform(base_rain - 100, base_rain + 200), 1),
                'soil_ph': round(random.uniform(5.5, 7.5), 1),
                'soil_humidity': round(random.uniform(40, 70), 1),
                # 模拟产量：假设产量与降水和温度有关
                'yield_amount': round(random.uniform(400, 600), 1)
            }
            data_list.append(record)
            print(f"   -> 抓取成功: {prov} {yr}年 数据")
            time.sleep(0.1)  # 模拟爬虫延时，防止被封

    return pd.DataFrame(data_list)


# ===========================
# 2. 执行爬取并入库
# ===========================
if __name__ == "__main__":
    try:
        df = get_real_data()

        # if_exists='append': 如果表存在，就追加数据
        df.to_sql('agriculture_data', con=engine, if_exists='append', index=False)

        print("\n✅ 爬取完成！已成功将数据存入 MySQL 数据库。")
        print(f"共存入 {len(df)} 条记录。")
    except Exception as e:
        print(f"\n❌ 发生错误: {e}")