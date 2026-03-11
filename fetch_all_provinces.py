import akshare as ak
import pandas as pd
from sqlalchemy import create_engine

# 1. 连接数据库
engine = create_engine('mysql+pymysql://root:123456@localhost:3306/agri_yield_db')

def fetch_and_save():
    print("正在从国家统计局接口获取全国各省产量数据...")
    # 获取年度分省份粮食产量数据
    # 注意：akshare 的接口名可能会随版本更新，这是常用的统计局接口
    df = ak.macro_china_gdp_yearly() # 示例：此处可替换为具体的农业接口

    # 模拟一份各省份特征数据（因为统计局气象数据通常在不同表，建议先用模拟数据跑通流程）
    data = [
        {'province': '黑龙江', 'temperature': 18.5, 'rainfall': 500, 'soil_ph': 6.2, 'yield_amount': 650, 'data_year': 2024},
        {'province': '河南省', 'temperature': 22.1, 'rainfall': 650, 'soil_ph': 7.0, 'yield_amount': 590, 'data_year': 2024},
        {'province': '山东省', 'temperature': 21.5, 'rainfall': 600, 'soil_ph': 6.8, 'yield_amount': 570, 'data_year': 2024},
        # ... 可以循环添加更多省份
    ]

    df_new = pd.DataFrame(data)
    df_new.to_sql('agriculture_data', con=engine, if_exists='append', index=False)
    print("✅ 全省份数据同步完成！")

if __name__ == "__main__":
    fetch_and_save()