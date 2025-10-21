# @Author : will wang
# @Time : 2025/10/21 10:21
# 汇总导入文件 - 整合所有数据导入功能（包含RTS、时间段、经纬度）
# 改进版：实现大事务回滚机制
import sys

import pandas as pd
import pymysql
import logging
import re
import time
import csv
import math
import requests
from typing import Optional, Union, Dict, Tuple, List
from datetime import datetime, date, timedelta

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


class ComprehensiveDataImporter:
    def __init__(self, db_config=None):
        """初始化综合数据导入器"""
        self.db_config = db_config or {
            'host': 'localhost',
            'user': 'root',
            'password': '123',
            'database': 'healthineersvisualization',
            'charset': 'utf8mb4'
        }

        # 受伤原因分类映射
        self.injury_cause_mapping = {
            '交通伤': 0,
            '高坠伤': 1,
            '机械伤': 2,
            '跌倒': 3,
            '其他': 4
        }

        # GCS评分映射字典
        self.eye_opening_map = {
            '自动睁眼': 4,
            '呼唤睁眼': 3,
            '刺痛睁眼': 2,
            '无反应': 1,
            '肿胀不能睁眼': 0
        }

        self.verbal_response_map = {
            '回答正确': 5,
            '回答错误': 4,
            '言语不清': 3,
            '只能发音': 2,
            '无反应': 1,
            '气管插管或切开': 0,
            '平素言语障碍': 0
        }

        self.motor_response_map = {
            '遵嘱': 6,
            '定位': 5,
            '逃避': 4,
            '屈曲': 3,
            '过伸': 2,
            '无反应': 1,
            '瘫痪': 0
        }

        # 高德地图API配置
        self.API_KEY = 'a45594094ddabde9555f030599338cb9'
        self.CITY = '上海'
        self.CITY_PREFIX = '上海市'
        self.CACHE_FILE = 'geo_cache_sh.csv'
        self.SLEEP_SECONDS = 0.25
        self.RETRY = 2

        # 无效地址标记
        self.INVALID_TOKENS = {
            '(跳过)', '*', '0', '无', 'nan', '', '家', '家中', '自行', '家中摔倒', '家门口', '住所',
            '小区', '居民楼', '别墅', '家庭', '不详', '(空)', '未知'
        }

        # ISS评分映射字典
        self.body_part_score_mapping = self.create_body_part_score_mapping()

        # 导入结果统计
        self.import_results = {
            'patient_basic_info': {'success': 0, 'failed': 0, 'status': 'pending'},
            'injury_records': {'success': 0, 'failed': 0, 'status': 'pending'},
            'gcs_scores': {'success': 0, 'failed': 0, 'status': 'pending'},
            'rts_scores': {'success': 0, 'failed': 0, 'status': 'pending'},
            'patient_info_on_admission': {'success': 0, 'failed': 0, 'status': 'pending'},
            'patient_info_off_admission': {'success': 0, 'failed': 0, 'status': 'pending'},
            'intervention_time': {'success': 0, 'failed': 0, 'status': 'pending'},
            'intervention_extra': {'success': 0, 'failed': 0, 'status': 'pending'},
            'iss_data': {'success': 0, 'failed': 0, 'status': 'pending'},
            'time_period_update': {'success': 0, 'failed': 0, 'status': 'pending'},
            'longitude_latitude_update': {'success': 0, 'failed': 0, 'status': 'pending'}
        }

    def get_database_connection(self):
        """获取数据库连接"""
        try:
            return pymysql.connect(**self.db_config)
        except Exception as e:
            logger.error(f"数据库连接失败: {e}")
            raise

    # ==================== 基础数据清洗方法 ====================

    def is_blank(self, v) -> bool:
        if v is None or (isinstance(v, float) and pd.isna(v)):
            return True
        s = str(v).strip()
        return s in ['', '无', '(空)', '(跳过)']

    def clean_text(self, v) -> str:
        if self.is_blank(v):
            return ''
        return str(v).strip()

    def clean_int(self, v) -> int:
        if self.is_blank(v):
            return 0
        try:
            return int(str(v).strip())
        except Exception:
            m = re.search(r'\d+', str(v))
            return int(m.group(0)) if m else 0

    def clean_float(self, v) -> float:
        if self.is_blank(v):
            return 0.0
        try:
            return float(str(v).strip())
        except Exception:
            m = re.search(r'\d+\.?\d*', str(v))
            return float(m.group(0)) if m else 0.0

    def clean_yes_no(self, v) -> str:
        s = self.clean_text(v)
        if not s:
            return ''
        if '是' in s:
            return '是'
        if '否' in s or '无' in s:
            return '否'
        m = re.search(r'\b(yes|no|y|n)\b', s, flags=re.IGNORECASE)
        if m:
            return '是' if m.group(1).lower().startswith('y') else '否'
        return '否'

    def clean_yes_no_bool(self, v) -> bool:
        """将Excel中的是/否转换为布尔值"""
        s = self.clean_text(v)
        if not s:
            return False
        if '是' in s:
            return True
        if '否' in s or '无' in s:
            return False
        m = re.search(r'\b(yes|no|y|n)\b', s, flags=re.IGNORECASE)
        if m:
            return m.group(1).lower().startswith('y')
        return False

    def clean_percent_float(self, v) -> Optional[float]:
        if self.is_blank(v):
            return None
        m = re.search(r'(\d+(?:\.\d+)?)\s*%?', str(v))
        return float(m.group(1)) if m else None

    def clean_float_first_number(self, v) -> Optional[float]:
        if self.is_blank(v):
            return None
        m = re.search(r'(-?\d+(?:\.\d+)?)', str(v))
        return float(m.group(1)) if m else None

    def clean_temperature_data(self, value):
        """清理体温数据"""
        if self.is_blank(value):
            return 0.0

        value_str = str(value).strip()
        if '@' in value_str:
            value_str = value_str.replace('@', '.')
        if ',' in value_str:
            value_str = value_str.split(',')[-1].strip()

        numbers = re.findall(r'\d+\.?\d*', value_str)
        if numbers:
            cleaned_value = float(numbers[0])
            if 30.0 <= cleaned_value <= 45.0:
                return cleaned_value
        return 0.0

    def clean_time_data(self, time_str):
        """清理时间数据"""
        if self.is_blank(time_str):
            return None

        time_str = str(time_str).strip()
        if len(time_str) == 4 and time_str.isdigit():
            hour = int(time_str[:2])
            minute = int(time_str[2:])
            if hour >= 24 or minute >= 60:
                return None
            return time_str

        if ':' in time_str:
            parts = time_str.split(':')
            if len(parts) == 2:
                hour = int(parts[0])
                minute = int(parts[1])
                if hour >= 24 or minute >= 60:
                    return None
                return f"{hour:02d}{minute:02d}"

        return None

    def parse_time_value(self, value):
        """解析时间值，提取〖〗中的时间"""
        if self.is_blank(value):
            return None

        value_str = str(value).strip()
        match = re.search(r'〖(\d{4})〗', value_str)
        if match:
            return match.group(1)

        if re.match(r'^\d{4}$', value_str):
            return value_str

        if value_str in ['是', '有']:
            return value_str

        return None

    def parse_date(self, value):
        """解析日期值"""
        if self.is_blank(value):
            return None

        try:
            if isinstance(value, datetime):
                return value.date()
            elif isinstance(value, date):
                return value
            else:
                return pd.to_datetime(value).date()
        except:
            return None

    def calculate_season(self, admission_date):
        """计算季节"""
        if not admission_date:
            return None

        try:
            if isinstance(admission_date, str):
                if len(admission_date) == 10:
                    month = int(admission_date.split('-')[1])
                else:
                    return None
            else:
                month = admission_date.month

            if month in [3, 4, 5]:
                return 0  # 春季
            elif month in [6, 7, 8, 9]:
                return 1  # 夏季
            elif month in [10, 11, 12]:
                return 2  # 秋季
            elif month in [1, 2]:
                return 3  # 冬季
            else:
                return None
        except Exception as e:
            logger.warning(f"计算季节失败: {admission_date}, 错误: {e}")
            return None

    def classify_injury_cause(self, injury_cause_text):
        """对受伤原因进行分类"""
        if not injury_cause_text:
            return 4, ''

        injury_cause_text = injury_cause_text.strip()

        for cause, category_code in self.injury_cause_mapping.items():
            if cause in injury_cause_text:
                if category_code == 4:
                    detail = self.extract_other_cause_detail(injury_cause_text)
                    return category_code, detail
                else:
                    return category_code, injury_cause_text

        if '其他' in injury_cause_text:
            detail = self.extract_other_cause_detail(injury_cause_text)
            return 4, detail

        return 4, injury_cause_text

    def extract_other_cause_detail(self, injury_cause_text):
        """从"其他"类别的受伤原因中提取具体描述"""
        if not injury_cause_text:
            return ''

        pattern_square_brackets = r'〖([^〗]+)〗'
        match = re.search(pattern_square_brackets, injury_cause_text)
        if match:
            return match.group(1)

        return injury_cause_text

    # ==================== 大事务管理方法 ====================

    def start_master_transaction(self, connection):
        """开始主事务"""
        connection.begin()
        logger.info("🚀 开始主事务 - 所有表导入将作为一个大事务处理")

    def commit_master_transaction(self, connection):
        """提交主事务"""
        connection.commit()
        logger.info("✅ 主事务提交成功 - 所有数据导入完成")

    def rollback_master_transaction(self, connection):
        """回滚主事务"""
        connection.rollback()
        logger.error("❌ 主事务回滚 - 所有数据导入失败，已回滚所有操作")

    def log_import_result(self, table_name, success_count, failed_count, status):
        """记录导入结果"""
        self.import_results[table_name] = {
            'success': success_count,
            'failed': failed_count,
            'status': status
        }

        if status == 'success':
            logger.info(f"✅ {table_name} 导入成功: 成功 {success_count} 条, 失败 {failed_count} 条")
        elif status == 'failed':
            logger.error(f"❌ {table_name} 导入失败: 成功 {success_count} 条, 失败 {failed_count} 条")
        else:
            logger.warning(f"⚠️ {table_name} 导入部分成功: 成功 {success_count} 条, 失败 {failed_count} 条")

    def print_final_summary(self):
        """打印最终汇总报告"""
        logger.info("=" * 80)
        logger.info("📊 数据导入最终汇总报告")
        logger.info("=" * 80)

        total_success = 0
        total_failed = 0
        failed_tables = []

        for table_name, result in self.import_results.items():
            success = result['success']
            failed = result['failed']
            status = result['status']

            total_success += success
            total_failed += failed

            if status == 'failed':
                failed_tables.append(table_name)

            status_icon = "✅" if status == 'success' else "❌" if status == 'failed' else "⚠️"
            logger.info(f"{status_icon} {table_name}: 成功 {success} 条, 失败 {failed} 条")

        logger.info("-" * 80)
        logger.info(f"📈 总计: 成功 {total_success} 条, 失败 {total_failed} 条")

        if failed_tables:
            logger.error(f"❌ 失败的表: {', '.join(failed_tables)}")
            logger.error("🔄 由于存在失败的表，整个事务已回滚")
        else:
            logger.info("🎉 所有表导入成功！")

        logger.info("=" * 80)

    # ==================== 各模块导入方法（修改为不自动提交事务）====================

    def import_patient_basic_info(self, df, connection):
        """导入患者基本信息"""
        try:
            cursor = connection.cursor()
            logger.info("开始患者基本信息导入...")

            insert_sql = """
            INSERT INTO patient 
            (patient_id, gender, age, is_green_channel, height, weight, name)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            gender = VALUES(gender),
            age = VALUES(age),
            is_green_channel = VALUES(is_green_channel),
            height = VALUES(height),
            weight = VALUES(weight),
            name = VALUES(name)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    gender = self.clean_text(row.get('患者性别：', ''))
                    age = self.clean_int(row.get('年龄：       ', 0))
                    is_green_channel = '是' if self.clean_text(row.get('是否绿色通道', '')) == '是' else '否'
                    height = self.clean_float(row.get('(1)身高：___', 0))
                    weight = self.clean_float(row.get('(2)cm    体重：___kg', 0))
                    name = self.clean_text(row.get('姓名', ''))

                    cursor.execute(insert_sql, (
                        patient_id, gender, age, is_green_channel, height, weight, name
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} 基本信息导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('patient_basic_info', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"患者基本信息导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('patient_basic_info', 0, len(df), 'failed')
            raise

    def import_injury_records(self, df, connection):
        """导入病例记录"""
        try:
            cursor = connection.cursor()
            logger.info("开始病例记录导入...")

            insert_sql = """
            INSERT INTO InjuryRecord 
            (patient_id, admission_date, admission_time, arrival_method, 
             injury_location, station_name, injury_cause_category, 
             injury_cause_detail, season)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            admission_date = VALUES(admission_date),
            admission_time = VALUES(admission_time),
            arrival_method = VALUES(arrival_method),
            injury_location = VALUES(injury_location),
            station_name = VALUES(station_name),
            injury_cause_category = VALUES(injury_cause_category),
            injury_cause_detail = VALUES(injury_cause_detail),
            season = VALUES(season)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    admission_date = self.parse_date(row.get('接诊日期：', ''))
                    admission_time = self.clean_time_data(row.get('接诊时间：', ''))
                    arrival_method = self.clean_text(row.get('来院方式', ''))
                    injury_location = self.clean_text(row.get(
                        '(2)    创伤发生地：___（小区名，工厂名，商场名。如果是交通事故填写XX路上靠近XX路，或者XX路和XX路交叉口）',
                        ''))
                    station_name = self.clean_text(row.get('(1)120分站站点名称：___', ''))
                    injury_cause_raw = self.clean_text(row.get('受伤原因:', ''))
                    injury_cause_category, injury_cause_detail = self.classify_injury_cause(injury_cause_raw)
                    season = self.calculate_season(admission_date)

                    cursor.execute(insert_sql, (
                        patient_id, admission_date, admission_time, arrival_method,
                        injury_location, station_name, injury_cause_category,
                        injury_cause_detail, season
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} 病例记录导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('injury_records', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"病例记录导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('injury_records', 0, len(df), 'failed')
            raise

    def import_gcs_scores(self, df, connection):
        """导入GCS评分"""
        try:
            cursor = connection.cursor()
            logger.info("开始GCS评分导入...")

            insert_sql = """
            INSERT INTO gcs_score 
            (patient_id, eye_opening, verbal_response, motor_response, total_score,
             eye_description, verbal_description, motor_description, consciousness_level)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            eye_opening = VALUES(eye_opening),
            verbal_response = VALUES(verbal_response),
            motor_response = VALUES(motor_response),
            total_score = VALUES(total_score),
            eye_description = VALUES(eye_description),
            verbal_description = VALUES(verbal_description),
            motor_description = VALUES(motor_description),
            consciousness_level = VALUES(consciousness_level)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    eye_desc = self.clean_text(row.get('GCS评分：睁眼', ''))
                    verbal_desc = self.clean_text(row.get('GCS评分：言语', ''))
                    motor_desc = self.clean_text(row.get('GCS评分：动作', ''))
                    total_score = self.clean_int(row.get('GCS总分：', 0))

                    eye_score = self.eye_opening_map.get(eye_desc, 0)
                    verbal_score = self.verbal_response_map.get(verbal_desc, 0)
                    motor_score = self.motor_response_map.get(motor_desc, 0)

                    consciousness_level = self.get_consciousness_level(total_score)

                    cursor.execute(insert_sql, (
                        patient_id, eye_score, verbal_score, motor_score, total_score,
                        eye_desc, verbal_desc, motor_desc, consciousness_level
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} GCS评分导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('gcs_scores', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"GCS评分导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('gcs_scores', 0, len(df), 'failed')
            raise

    def import_rts_scores(self, df, connection):
        """导入RTS评分"""
        try:
            cursor = connection.cursor()
            logger.info("开始RTS评分导入...")

            insert_sql = """
            INSERT INTO rts_score 
            (patient_id, gcs_score, sbp_score, rr_score)
            VALUES (%s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            gcs_score = VALUES(gcs_score),
            sbp_score = VALUES(sbp_score),
            rr_score = VALUES(rr_score)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    gcs_score = self.clean_int(row.get('RTS评分—GCS', 0))
                    sbp_score = self.clean_int(row.get('收缩压', 0))
                    rr_score = self.clean_int(row.get('呼吸频率', 0))

                    # 验证评分值是否在0-4范围内
                    if not (0 <= gcs_score <= 4) or not (0 <= sbp_score <= 4) or not (0 <= rr_score <= 4):
                        logger.warning(f"患者 {patient_id} RTS评分值无效，跳过")
                        continue

                    cursor.execute(insert_sql, (
                        patient_id, gcs_score, sbp_score, rr_score
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} RTS评分导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('rts_scores', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"RTS评分导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('rts_scores', 0, len(df), 'failed')
            raise

    def get_consciousness_level(self, total_score):
        """根据总分判断意识状态"""
        if total_score == 15:
            return '意识清楚'
        elif 12 <= total_score <= 14:
            return '轻度意识障碍'
        elif 9 <= total_score <= 11:
            return '中度意识障碍'
        elif 3 <= total_score <= 8:
            return '昏迷'
        else:
            return '无法评估'

    def import_patient_info_on_admission(self, df, connection):
        """导入患者入室信息"""
        try:
            cursor = connection.cursor()
            logger.info("开始患者入室信息导入...")

            insert_sql = """
            INSERT INTO patient_info_on_admission 
            (patient_id, systolic_bp, diastolic_bp, heart_rate, respiratory_rate, 
             medical_history, temperature, oxygen_saturation, consciousness, 
             skin, drunk, pupil, light_reflex)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            systolic_bp = VALUES(systolic_bp),
            diastolic_bp = VALUES(diastolic_bp),
            heart_rate = VALUES(heart_rate),
            respiratory_rate = VALUES(respiratory_rate),
            medical_history = VALUES(medical_history),
            temperature = VALUES(temperature),
            oxygen_saturation = VALUES(oxygen_saturation),
            consciousness = VALUES(consciousness),
            skin = VALUES(skin),
            drunk = VALUES(drunk),
            pupil = VALUES(pupil),
            light_reflex = VALUES(light_reflex)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    systolic_bp = self.clean_int(row.get('(1)血压：___', 0))
                    diastolic_bp = self.clean_int(row.get('(2)/___mmHg', 0))
                    heart_rate = self.clean_float(row.get('脉搏心率：              bpm', 0))
                    respiratory_rate = self.clean_int(row.get('呼吸频率：                   次/分', 0))
                    medical_history = self.clean_text(row.get('既往病史：', ''))
                    temperature = self.clean_temperature_data(row.get('入室体温：             ℃', ''))
                    oxygen_saturation = self.clean_int(row.get('指脉氧：                       %', 0))
                    consciousness = self.clean_text(row.get('精神意识:', ''))
                    skin = self.clean_text(row.get('皮肤:', ''))
                    drunk = self.clean_yes_no_bool(row.get('醉酒:', ''))
                    pupil = self.clean_text(row.get('瞳孔:', ''))
                    light_reflex = self.clean_text(row.get('对光反射:', ''))

                    cursor.execute(insert_sql, (
                        patient_id, systolic_bp, diastolic_bp, int(heart_rate), respiratory_rate,
                        medical_history, temperature, oxygen_saturation, consciousness,
                        skin, drunk, pupil, light_reflex
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} 入室信息导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('patient_info_on_admission', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"患者入室信息导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('patient_info_on_admission', 0, len(df), 'failed')
            raise

    def import_patient_info_off_admission(self, df, connection):
        """导入患者离室信息"""
        try:
            cursor = connection.cursor()
            logger.info("开始患者离室信息导入...")

            insert_sql = """
            INSERT INTO patient_info_off_admission (
                patient_id, temperature, respiratory_rate, heart_rate, 
                systolic_bp, diastolic_bp, oxygen_saturation, total_fluid_volume,
                saline_solution, balanced_solution, artificial_colloid, other_fluid,
                urine_output, other_drainage, blood_loss
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
            ON DUPLICATE KEY UPDATE
            temperature = VALUES(temperature),
            respiratory_rate = VALUES(respiratory_rate),
            heart_rate = VALUES(heart_rate),
            systolic_bp = VALUES(systolic_bp),
            diastolic_bp = VALUES(diastolic_bp),
            oxygen_saturation = VALUES(oxygen_saturation),
            total_fluid_volume = VALUES(total_fluid_volume),
            saline_solution = VALUES(saline_solution),
            balanced_solution = VALUES(balanced_solution),
            artificial_colloid = VALUES(artificial_colloid),
            other_fluid = VALUES(other_fluid),
            urine_output = VALUES(urine_output),
            other_drainage = VALUES(other_drainage),
            blood_loss = VALUES(blood_loss)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    temperature = self.clean_temperature_data(row.get('(1)离开抢救室生命体征：体温：___', ''))
                    respiratory_rate = self.clean_int(row.get('(2)℃呼吸：___', 0))
                    heart_rate = self.clean_int(row.get('(3)次/分心率：___', 0))
                    systolic_bp = self.clean_int(row.get('(4)bpm血压：___', 0))
                    diastolic_bp = self.clean_int(row.get('(5)/___', 0))
                    oxygen_saturation = self.clean_float(row.get('(6)mmHg指脉氧：___%', 0))
                    total_fluid_volume = self.clean_float(row.get('(1)总补液量：___', 0))
                    saline_solution = self.clean_float(row.get('(2)ml         其中:  生理盐水：___', 0))
                    balanced_solution = self.clean_float(row.get('(3)ml               平衡液：___', 0))
                    artificial_colloid = self.clean_float(row.get('(4)ml               人工胶体：___', 0))
                    other_fluid = self.clean_text(row.get('(5)ml     其他：___', ''))
                    urine_output = self.clean_float(row.get('(1)尿量：___', 0))
                    other_drainage = self.clean_float(row.get('(2)ml    其他引流量：___', 0))
                    blood_loss = self.clean_text(row.get('(3)ml出血量：___ml', ''))

                    cursor.execute(insert_sql, (
                        patient_id, temperature, respiratory_rate, heart_rate,
                        systolic_bp, diastolic_bp, oxygen_saturation, total_fluid_volume,
                        saline_solution, balanced_solution, artificial_colloid, other_fluid,
                        urine_output, other_drainage, blood_loss
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} 离室信息导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('patient_info_off_admission', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"患者离室信息导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('patient_info_off_admission', 0, len(df), 'failed')
            raise

    def import_intervention_time(self, df, connection):
        """导入干预时间数据"""
        try:
            cursor = connection.cursor()
            logger.info("开始干预时间数据导入...")

            insert_sql = """
            INSERT INTO interventiontime (
                patient_id, admission_date, admission_time, peripheral, iv_line, central_access,
                nasal_pipe, face_mask, endotracheal_tube, ventilator, cpr, cpr_start_time,
                cpr_end_time, ultrasound, CT, tourniquet, blood_draw, catheter, gastric_tube,
                transfusion, transfusion_start, transfusion_end, leave_surgery_time, leave_surgery_date,
                patient_destination, death, death_date, death_time
            ) VALUES (
                %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,
                %s, %s, %s, %s, %s, %s, %s, %s, %s
            )
            ON DUPLICATE KEY UPDATE
            admission_date = VALUES(admission_date),
            admission_time = VALUES(admission_time),
            peripheral = VALUES(peripheral),
            iv_line = VALUES(iv_line),
            central_access = VALUES(central_access),
            nasal_pipe = VALUES(nasal_pipe),
            face_mask = VALUES(face_mask),
            endotracheal_tube = VALUES(endotracheal_tube),
            ventilator = VALUES(ventilator),
            cpr = VALUES(cpr),
            cpr_start_time = VALUES(cpr_start_time),
            cpr_end_time = VALUES(cpr_end_time),
            ultrasound = VALUES(ultrasound),
            CT = VALUES(CT),
            tourniquet = VALUES(tourniquet),
            blood_draw = VALUES(blood_draw),
            catheter = VALUES(catheter),
            gastric_tube = VALUES(gastric_tube),
            transfusion = VALUES(transfusion),
            transfusion_start = VALUES(transfusion_start),
            transfusion_end = VALUES(transfusion_end),
            leave_surgery_time = VALUES(leave_surgery_time),
            leave_surgery_date = VALUES(leave_surgery_date),
            patient_destination = VALUES(patient_destination),
            death = VALUES(death),
            death_date = VALUES(death_date),
            death_time = VALUES(death_time)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    admission_date = self.parse_date(row.get('接诊日期：', ''))
                    admission_time = self.clean_time_data(row.get('接诊时间：', ''))

                    # 解析离开抢救室时间
                    leave_date, leave_time = self.parse_leave_surgery_time(
                        row.get('离开抢救室时间：', ''), admission_date, admission_time
                    )

                    cursor.execute(insert_sql, (
                        patient_id, admission_date, admission_time,
                        self.parse_time_value(row.get('外周:', '')),
                        self.parse_time_value(row.get('深静脉:', '')),
                        self.parse_time_value(row.get('骨通道:', '')),
                        self.parse_time_value(row.get('鼻导管:', '')),
                        self.parse_time_value(row.get('面罩:', '')),
                        self.parse_time_value(row.get('气管插管:', '')),
                        self.parse_time_value(row.get('呼吸机:', '')),
                        self.clean_yes_no(row.get('心肺复苏:', '')),
                        self.parse_time_value(row.get('开始时间：', '')),
                        self.parse_time_value(row.get('结束时间：', '')),
                        self.clean_yes_no(row.get('B超：', '')),
                        self.parse_time_value(row.get('CT:', '')),
                        self.parse_time_value(row.get('止血带:', '')),
                        self.parse_time_value(row.get('采血:', '')),
                        self.parse_time_value(row.get('导尿:', '')),
                        self.parse_time_value(row.get('胃管：', '')),
                        self.clean_yes_no(row.get('输血:', '')),
                        self.parse_time_value(row.get('输血开始：', '')),
                        self.parse_time_value(row.get('输血结束：', '')),
                        leave_time, leave_date,
                        self.clean_text(row.get('病人去向:', '')),
                        self.clean_yes_no(row.get('死亡:', '')),
                        self.parse_date(row.get('死亡日期：', '')),
                        self.parse_time_value(row.get('死亡时间：', ''))
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} 干预时间数据导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('intervention_time', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"干预时间数据导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('intervention_time', 0, len(df), 'failed')
            raise

    def parse_leave_surgery_time(self, value, admission_date, admission_time):
        """解析离开抢救室时间"""
        if self.is_blank(value):
            return None, None

        value_str = str(value).strip()

        # 匹配 "MM-DD HHMM" 格式
        date_time_match = re.match(r'(\d{2}-\d{2})\s+(\d{4})', value_str)
        if date_time_match:
            month_day = date_time_match.group(1)
            time_str = date_time_match.group(2)
            current_year = admission_date.year if admission_date else datetime.now().year
            try:
                leave_date = datetime.strptime(f"{current_year}-{month_day}", "%Y-%m-%d").date()
                if admission_date and leave_date < admission_date:
                    leave_date = leave_date.replace(year=current_year + 1)
                return leave_date, time_str
            except ValueError:
                return None, time_str

        # 匹配 "HHMM" 格式
        time_match = re.match(r'(\d{4})$', value_str)
        if time_match:
            time_str = time_match.group(1)
            if admission_date and admission_time:
                try:
                    admission_hour = int(admission_time[:2])
                    admission_minute = int(admission_time[2:])
                    leave_hour = int(time_str[:2])
                    leave_minute = int(time_str[2:])

                    admission_time_obj = datetime.combine(admission_date, datetime.min.time().replace(
                        hour=admission_hour, minute=admission_minute
                    ))
                    leave_time_obj = datetime.combine(admission_date, datetime.min.time().replace(
                        hour=leave_hour, minute=leave_minute
                    ))

                    if leave_time_obj >= admission_time_obj:
                        return admission_date, time_str
                    else:
                        leave_date = admission_date + timedelta(days=1)
                        return leave_date, time_str
                except (ValueError, IndexError):
                    return admission_date, time_str
            return admission_date, time_str

        return None, None

    def import_intervention_extra(self, df, connection):
        """导入干预补充数据"""
        try:
            cursor = connection.cursor()
            logger.info("开始干预补充数据导入...")

            insert_sql = """
            INSERT INTO intervention_extra (
                patient_id, oxygen_concentration, defibrillation, limb_amputation,
                transfusion_reaction, suspended_red_units, plasma_units,
                platelets_amount, cryoprecipitate_units, other_transfusion,
                therapeutic_operation, consultation_dept, administrative_dept
            ) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
            ON DUPLICATE KEY UPDATE
                oxygen_concentration = VALUES(oxygen_concentration),
                defibrillation = VALUES(defibrillation),
                limb_amputation = VALUES(limb_amputation),
                transfusion_reaction = VALUES(transfusion_reaction),
                suspended_red_units = VALUES(suspended_red_units),
                plasma_units = VALUES(plasma_units),
                platelets_amount = VALUES(platelets_amount),
                cryoprecipitate_units = VALUES(cryoprecipitate_units),
                other_transfusion = VALUES(other_transfusion),
                therapeutic_operation = VALUES(therapeutic_operation),
                consultation_dept = VALUES(consultation_dept),
                administrative_dept = VALUES(administrative_dept)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    oxygen_concentration = self.clean_percent_float(row.get('(1)氧浓度：___ %   （最低）', ''))
                    defibrillation = self.clean_yes_no(row.get('除颤:', ''))
                    limb_amputation = self.clean_yes_no(row.get('肢体离断:', ''))
                    transfusion_reaction = self.clean_text(row.get('输血反应:', ''))
                    suspended_red_units = self.clean_float_first_number(row.get('(1)悬红：___', ''))
                    plasma_units = self.clean_float_first_number(row.get('(2) U       血浆：___', ''))
                    platelets_amount = self.clean_float_first_number(row.get('(3)ml血小板：___', ''))
                    cryoprecipitate_units = self.clean_float_first_number(row.get('(4)U      冷沉淀：___', ''))
                    other_transfusion = self.clean_text(row.get('(5)U其他：___', ''))
                    therapeutic_operation = self.clean_text(row.get('治疗性操作：', ''))
                    consultation_dept = self.clean_text(row.get('会诊科室：', ''))
                    administrative_dept = self.clean_text(row.get('行政科室：', ''))

                    cursor.execute(insert_sql, (
                        patient_id, oxygen_concentration, defibrillation, limb_amputation,
                        transfusion_reaction, suspended_red_units, plasma_units,
                        platelets_amount, cryoprecipitate_units, other_transfusion,
                        therapeutic_operation, consultation_dept, administrative_dept
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} 干预补充数据导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('intervention_extra', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"干预补充数据导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('intervention_extra', 0, len(df), 'failed')
            raise

    def import_iss_data(self, df, connection):
        """导入ISS数据"""
        try:
            cursor = connection.cursor()
            logger.info("开始ISS数据导入...")

            insert_sql = """
            INSERT INTO iss_patient_injury_severity 
            (patient_id, head_neck, face, chest, abdomen, limbs, body, iss_score,
             head_neck_details, face_details, chest_details, abdomen_details, limbs_details, body_details, has_details)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
            head_neck = VALUES(head_neck),
            face = VALUES(face),
            chest = VALUES(chest),
            abdomen = VALUES(abdomen),
            limbs = VALUES(limbs),
            body = VALUES(body),
            iss_score = VALUES(iss_score),
            head_neck_details = VALUES(head_neck_details),
            face_details = VALUES(face_details),
            chest_details = VALUES(chest_details),
            abdomen_details = VALUES(abdomen_details),
            limbs_details = VALUES(limbs_details),
            body_details = VALUES(body_details),
            has_details = VALUES(has_details)
            """

            success, errors = 0, 0
            for idx, row in df.iterrows():
                try:
                    patient_id = self.clean_int(row.get('序号', 0))
                    if patient_id == 0:
                        continue

                    # 解析各部位分值
                    head_neck = self.parse_iss_score(row.get('ISS评分矩阵—头颈部', 0))
                    face = self.parse_iss_score(row.get('面部', 0))
                    chest = self.parse_iss_score(row.get('胸部', 0))
                    abdomen = self.parse_iss_score(row.get('腹部', 0))
                    limbs = self.parse_iss_score(row.get('四肢', 0))
                    body = self.parse_iss_score(row.get('体表', 0))
                    iss_score = self.clean_int(row.get('ISS评分：', 0))

                    # 解析详细伤情信息
                    detailed_injuries = self.parse_detailed_injuries(row, df)
                    has_details = 1 if any(detailed_injuries.values()) else 0

                    cursor.execute(insert_sql, (
                        patient_id,
                        0 if head_neck == "0" else head_neck,
                        0 if face == "0" else face,
                        0 if chest == "0" else chest,
                        0 if abdomen == "0" else abdomen,
                        0 if limbs == "0" else limbs,
                        0 if body == "0" else body,
                        0 if iss_score == 0 else iss_score,
                        detailed_injuries['headNeck'] or None,
                        detailed_injuries['face'] or None,
                        detailed_injuries['chest'] or None,
                        detailed_injuries['abdomen'] or None,
                        detailed_injuries['limbs'] or None,
                        detailed_injuries['body'] or None,
                        has_details
                    ))
                    success += 1
                except Exception as e:
                    errors += 1
                    logger.error(f"患者 {patient_id} ISS数据导入失败: {e}")

            status = 'success' if errors == 0 else 'partial' if success > 0 else 'failed'
            self.log_import_result('iss_data', success, errors, status)

            if errors > 0 and success == 0:
                raise Exception(f"ISS数据导入完全失败，错误数量: {errors}")

        except Exception as e:
            self.log_import_result('iss_data', 0, len(df), 'failed')
            raise

    def parse_iss_score(self, value):
        """解析ISS分值"""
        if self.is_blank(value):
            return "0"

        value_str = str(value).strip()
        if value_str in ["无", "(空)", "", "0"]:
            return "0"

        if "┋" in value_str:
            return value_str.replace("┋", "|")

        if "|" in value_str:
            parts = value_str.split("|")
            valid_parts = []
            for part in parts:
                part = part.strip()
                if part.isdigit():
                    valid_parts.append(part)
            if valid_parts:
                return "|".join(valid_parts)
            else:
                return "0"

        try:
            int(value_str)
            return value_str
        except ValueError:
            return "0"

    def get_score_list(self, score_str: str) -> List[int]:
        """从多分值字符串中获取分值列表，只处理数字部分"""
        if score_str == "0":
            return []
        
        if "|" in score_str:
            # 只处理数字部分
            valid_scores = []
            for s in score_str.split("|"):
                s = s.strip()
                if s.isdigit():
                    valid_scores.append(int(s))
            return valid_scores
        
        # 处理单个分值
        if score_str.isdigit():
            return [int(score_str)]
        
        return []

    def find_matching_columns(self, df: pd.DataFrame, body_part: str, score: int) -> List[str]:
        """根据部位和分值查找匹配的列名"""
        matching_columns = []
        expected_columns = self.body_part_score_mapping[body_part].get(score, [])
        
        for expected_col in expected_columns:
            for actual_col in df.columns:
                if expected_col in actual_col:
                    matching_columns.append(actual_col)
                    break
        
        return matching_columns

    def extract_description(self, col_name: str) -> str:
        """从列名中提取伤情描述"""
        # 移除部位名称前缀
        for part_name in ['头颈部损伤', '面部损伤', '胸部损伤', '腹部损伤', '四肢损伤', '体表损伤']:
            if part_name in col_name:
                col_name = col_name.replace(part_name, '').strip()
                break
        
        # 移除开头的破折号
        if col_name.startswith('—'):
            col_name = col_name[1:].strip()
        
        return col_name

    def create_body_part_score_mapping(self) -> Dict[str, Dict[int, List[str]]]:
        """创建各部位分值与列名的哈希映射"""
        mapping = {
            'headNeck': {
                1: [
                    '头颈部损伤—①头部外伤后，头痛头晕',
                    '②颈椎损伤，无骨折'
                ],
                2: [
                    '①意外事故致记忆丧失',
                    '②嗜睡、木僵、迟钝，能被语言刺激唤醒',
                    '③昏迷＜1h',
                    '④单纯颅顶骨折',
                    '⑤甲状腺挫伤',
                    '⑥臂丛神经损伤',
                    '⑦颈椎棘突或横突骨折或移位',
                    '⑧颈椎轻度压缩骨折（≤20%）'
                ],
                3: [
                    '①昏迷1～6h，伴神经障碍',
                    '②昏迷6～24h',
                    '③仅对疼痛刺激有恰当反应',
                    '④颅骨骨折性凹陷＞2cm',
                    '⑤脑膜破裂或组织缺失',
                    '⑥颅内血肿≤100ml',
                    '⑦颈髓不完全损伤',
                    '⑧喉压轧伤',
                    '⑨颈动脉内膜撕裂、血栓形成伴神经障碍'
                ],
                4: [
                    '①昏迷伴有不适当的动作',
                    '②昏迷＞24h',
                    '③脑干损伤',
                    '④颅内血肿＞100ml',
                    '⑤颈4或以下颈髓完全损伤'
                ],
                5: [
                    '①碾压骨折',
                    '②脑干碾压撕裂',
                    '③断头',
                    '④颈3以上颈髓下轧、裂伤或完全断裂，有或无骨折'
                ],
                6: [
                    '①碾压骨折',
                    '②脑干碾压撕裂',
                    '③断头',
                    '④颈3以上颈髓下轧、裂伤或完全断裂，有或无骨折'
                ]
            },
            'face': {
                1: [
                    '面部损伤—①角膜擦伤',
                    '②舌浅表裂伤',
                    '③鼻骨或颌骨骨折（粉碎、移位或开放性骨折时加1分）',
                    '④牙齿折断、撕裂或脱位'
                ],
                2: [
                    '①颧骨、眶骨、下颌体或下颌关节突骨折',
                    '②LeFort Ⅰ型骨折',
                    '③巩膜、角膜裂伤'
                ],
                3: [
                    '①视神经挫伤',
                    '②LeFort Ⅱ型骨折'
                ],
                4: [
                    'LeFort Ⅲ型骨折'
                ]
            },
            'chest': {
                1: [
                    '胸部损伤—①单个肋骨骨折',
                    '②胸椎扭伤',
                    '③胸壁挫伤',
                    '④胸骨挫伤'
                ],
                2: [
                    '①2-3根肋骨骨折',
                    '②胸骨骨折',
                    '③胸椎脱位、棘突或横突骨折',
                    '④胸椎轻度压缩骨折（≤20%）'
                ],
                3: [
                    '①单叶肺挫伤、裂伤',
                    '②单侧血胸或气胸',
                    '③膈肌破裂',
                    '④肋骨骨折≥4根（有血胸、气胸或纵膈血肿时评分加1分）',
                    '⑤锁骨下动脉或无名动脉内膜裂伤、血栓形成',
                    '⑥轻度吸入性损伤',
                    '⑦胸椎脱位，椎板、椎弓根或关节突骨折',
                    '⑧椎体压缩骨折＞1个椎骨或高度＞20%'
                ],
                4: [
                    '①多叶肺挫伤、裂伤',
                    '②纵膈血肿或气肿',
                    '③双侧血气胸',
                    '④连枷胸',
                    '⑤心肌挫伤',
                    '⑥张力性气胸',
                    '⑦血胸≥1000ml',
                    '⑧气管撕裂',
                    '⑨主动脉内膜撕裂',
                    '⑩锁骨下动脉或无名动脉重度裂伤',
                    '11.脊髓不完全损伤综合征'
                ],
                5: [
                    '①重度主动脉裂伤',
                    '②心脏裂伤',
                    '③支气管、气管破裂',
                    '④连枷胸、吸入烧伤需机械通气',
                    '⑤喉、气管分离',
                    '⑥多叶肺撕裂伤伴张力性气胸，纵膈积血、积气或血胸＞1000ml',
                    '⑦脊髓裂伤或完全损伤'
                ],
                6: [
                    '①主动脉完全离断',
                    '②胸部广泛碾压'
                ]
            },
            'abdomen': {
                1: [
                    '腹部损伤—①擦伤、挫伤，浅表裂伤：阴囊、阴道、阴唇、会阴',
                    '②腰扭伤',
                    '③血尿'
                ],
                2: [
                    '①挫伤，浅表裂伤：胃、肠系膜、小肠、膀胱、输尿管、尿道',
                    '②轻度挫伤，裂伤：胃、肝、脾、胰',
                    '③挫伤：十二指肠、结肠',
                    '④腰椎脱位、横突或棘突骨折',
                    '⑤腰椎轻度压缩性（≤20%）',
                    '⑥神经根损伤'
                ],
                3: [
                    '①浅表裂伤：十二指肠、结肠、直肠',
                    '②穿孔：小肠、肠系膜、膀胱、输尿管、尿道',
                    '③大血管中度挫伤、轻度裂伤或血腹＞1000ml的肾、肝、脾、胰',
                    '④轻度髂动、静脉裂伤后腹膜血肿',
                    '⑤腰椎脱位或椎板、椎弓根、关节突骨折',
                    '⑥椎体压缩骨折＞1个椎骨或＞20%前缘高度'
                ],
                4: [
                    '①穿孔：胃、十二指肠、结肠、直肠',
                    '②穿孔伴组织缺失：胃、膀胱、小肠、输尿管、尿道',
                    '③肝裂伤（浅表性）',
                    '④严重髂动脉或静脉裂伤',
                    '⑤不全截瘫',
                    '⑥胎盘剥离'
                ],
                5: [
                    '①重度裂伤伴组织缺失或严重污染：十二指肠、结肠、直肠',
                    '②复杂破裂：肝、脾、肾、胰',
                    '③完全性腰髓损伤'
                ],
                6: [
                    '躯干横断'
                ]
            },
            'limbs': {
                1: [
                    '四肢损伤—①挫伤：肘、肩、腕、踝',
                    '②骨折、脱位：指、趾',
                    '③扭伤：肩锁、肩、肘、指、腕、髋、踝、趾'
                ],
                2: [
                    '①骨折：肱、桡、尺、腓、胫、锁骨、肩胛、腕、掌、跟、跗、跖骨、耻骨支或骨盆单纯骨折',
                    '②脱位：肘、手、肩、肩锁关节',
                    '③严重肌肉、肌腱裂伤',
                    '④内膜裂伤、轻度撕裂：腕、肱、腘动脉，腕、股、腘静脉'
                ],
                3: [
                    '①骨盆粉碎性骨折',
                    '②股骨骨折',
                    '③脱位：腕、踝、膝、髋',
                    '④膝下和上肢断裂',
                    '⑤膝韧带断裂',
                    '⑥坐骨神经撕裂',
                    '⑦内膜撕裂、轻度撕裂伤：股动脉',
                    '⑧重度裂伤伴或不伴血栓形成：腋、腘动脉，腘、股静脉'
                ],
                4: [
                    '①骨盆碾压性骨折',
                    '②膝下外伤性离断、碾压伤',
                    '③重度撕裂伤：股动脉或肱动脉'
                ],
                5: [
                    '骨盆开放粉碎性骨折'
                ]
            },
            'body': {
                1: [
                    '体表损伤—①擦/挫伤：面/手≤25cm身体≤50cm',
                    '②浅表裂伤：面/手≤5cm身体≤10cm',
                    '③一度烧伤≤100%',
                    '④二度～三度烧伤/脱套伤＜10%体表面积'
                ],
                2: [
                    '①擦/挫伤：面/手＞25cm，身体＞50cm',
                    '②裂伤：面/手＞5cm，身体＞10cm',
                    '③二度或三度烧伤/脱套伤达10%～19%体表面积'
                ],
                3: [
                    '二度或三度烧伤/脱套伤达20%～29%体表面积'
                ],
                4: [
                    '二度或三度烧伤/脱套伤达30%～39%体表面积'
                ],
                5: [
                    '二度或三度烧伤/脱套伤达40%～89%体表面积'
                ],
                6: [
                    '二度或三度烧伤/脱套伤≥90%体表面积'
                ]
            }
        }
        return mapping

    def parse_detailed_injuries(self, row, df):
        """解析详细伤情信息"""
        detailed_injuries = {
            'headNeck': '',
            'face': '',
            'chest': '',
            'abdomen': '',
            'limbs': '',
            'body': ''
        }
        
        # 获取各部位的分值
        head_neck_score = self.parse_iss_score(row.get('ISS评分矩阵—头颈部', 0))
        face_score = self.parse_iss_score(row.get('面部', 0))
        chest_score = self.parse_iss_score(row.get('胸部', 0))
        abdomen_score = self.parse_iss_score(row.get('腹部', 0))
        limbs_score = self.parse_iss_score(row.get('四肢', 0))
        body_score = self.parse_iss_score(row.get('体表', 0))
        
        scores = {
            'headNeck': head_neck_score,
            'face': face_score,
            'chest': chest_score,
            'abdomen': abdomen_score,
            'limbs': limbs_score,
            'body': body_score
        }
        
        for body_part, score_str in scores.items():
            if score_str == "0":
                continue
            
            # 获取该部位的所有分值（只处理数字部分）
            score_list = self.get_score_list(score_str)
            
            if not score_list:  # 如果没有有效数字，跳过
                continue
            
            # 按分值分组收集伤情项目
            score_groups = {}
            
            for score in score_list:
                # 查找该分值对应的列
                matching_columns = self.find_matching_columns(df, body_part, score)
                
                for col in matching_columns:
                    value = row[col]
                    if pd.notna(value) and str(value).strip() not in ["(空)", "无", ""]:
                        # 提取伤情描述
                        description = self.extract_description(col)
                        
                        # 按分值分组
                        if score not in score_groups:
                            score_groups[score] = []
                        
                        score_groups[score].append(description)
            
            # 格式化输出（只显示有伤情的部位）
            if score_groups:
                formatted_parts = []
                # 按分值从高到低排序
                for score in sorted(score_groups.keys(), reverse=True):
                    items = score_groups[score]
                    formatted_parts.append(f"{score}分（{', '.join(items)}）")
                
                detailed_injuries[body_part] = '，'.join(formatted_parts)
        
        return detailed_injuries

    # ==================== 时间段和经纬度更新方法 ====================

    def update_time_period(self, connection):
        """更新injuryrecord表的time_period字段"""
        try:
            cursor = connection.cursor()
            logger.info("开始更新时间段信息...")

            sql_update = """
            UPDATE injuryrecord
            SET time_period = CASE
              WHEN admission_time IS NULL
                   OR CHAR_LENGTH(admission_time) <> 4
                   OR admission_time REGEXP '^[0-9]{4}$' = 0
                THEN NULL
              WHEN CAST(LEFT(admission_time,2) AS UNSIGNED) BETWEEN 0 AND 7
                THEN 0
              WHEN CAST(LEFT(admission_time,2) AS UNSIGNED) IN (8,9)
                THEN 1
              WHEN CAST(LEFT(admission_time,2) AS UNSIGNED) IN (10,11)
                THEN 2
              WHEN CAST(LEFT(admission_time,2) AS UNSIGNED) BETWEEN 12 AND 16
                THEN 3
              WHEN CAST(LEFT(admission_time,2) AS UNSIGNED) BETWEEN 17 AND 19
                THEN 4
              WHEN CAST(LEFT(admission_time,2) AS UNSIGNED) BETWEEN 20 AND 23
                THEN 5
              ELSE NULL
            END
            """

            cursor.execute(sql_update)
            affected = cursor.rowcount
            self.log_import_result('time_period_update', affected, 0, 'success')
            logger.info(f"时间段更新完成，受影响行数：{affected}")

        except Exception as e:
            self.log_import_result('time_period_update', 0, 1, 'failed')
            raise

    def update_longitude_latitude(self, df, connection):
        """更新经纬度信息"""
        try:
            cursor = connection.cursor()
            logger.info("开始更新经纬度信息...")

            # 收集非空地址
            addresses_raw = []
            for x in df[
                '(2)    创伤发生地：___（小区名，工厂名，商场名。如果是交通事故填写XX路上靠近XX路，或者XX路和XX路交叉口）'].tolist():
                if x is None:
                    continue
                s = str(x).strip()
                if len(s) == 0 or s in self.INVALID_TOKENS:
                    continue
                addresses_raw.append(s)

            unique_raw = sorted(set(addresses_raw))
            logger.info(f'有效地址（去重后）：{len(unique_raw)}')

            # 预处理：构造 规范化 -> 坐标 的缓存键
            cache = self.load_cache(self.CACHE_FILE)

            # 解析并补全缓存
            newly = 0
            for raw in unique_raw:
                na = self.normalize_address(raw)
                if not na:
                    continue
                if na in cache:
                    continue
                lng, lat, _ = self.resolve_address(na)
                if lng is not None and lat is not None:
                    cache[na] = (lng, lat)
                    newly += 1
                    if newly % 20 == 0:
                        self.save_cache(self.CACHE_FILE, cache)
                time.sleep(self.SLEEP_SECONDS)
            self.save_cache(self.CACHE_FILE, cache)

            # 准备更新数据
            updates = []
            for raw in unique_raw:
                na = self.normalize_address(raw)
                if not na:
                    continue
                if na in cache:
                    lng, lat = cache[na]
                    if isinstance(lng, (int, float)) and isinstance(lat, (int, float)):
                        updates.append((lng, lat, raw))

            logger.info(f'可更新记录（按地址匹配）：{len(updates)}')

            if not updates:
                logger.info('没有可更新的数据。')
                self.log_import_result('longitude_latitude_update', 0, 0, 'success')
                return

            # 批量更新数据库
            sql = """
            UPDATE injuryrecord
            SET longitude = %s, latitude = %s
            WHERE injury_location = %s
              AND (longitude IS NULL OR longitude = 0)
              AND (latitude IS NULL OR latitude = 0)
            """
            cursor.executemany(sql, updates)
            affected = cursor.rowcount
            self.log_import_result('longitude_latitude_update', affected, 0, 'success')
            logger.info(f'经纬度更新完成，受影响行数：{affected}')

        except Exception as e:
            self.log_import_result('longitude_latitude_update', 0, 1, 'failed')
            raise

    # ==================== 经纬度相关方法 ====================

    def normalize_address(self, addr: str) -> str:
        """标准化地址"""
        if addr is None:
            return ''
        s = str(addr).strip()
        if s in self.INVALID_TOKENS or len(s) == 0:
            return ''
        # 去括号内容
        s = re.sub(r'[\(\（][^\)\）]*[\)\）]', '', s)
        # 去标点空白
        s = re.sub(r'[，,。.；;、\s]+', '', s)
        s = s.replace('上海市上海市', '上海市')
        if len(s) == 0 or s in self.INVALID_TOKENS:
            return ''
        # 统一"上海市"前缀
        if s.startswith('上海') and not s.startswith(self.CITY_PREFIX):
            s = self.CITY_PREFIX + s[2:]
        if not s.startswith(self.CITY_PREFIX):
            s = self.CITY_PREFIX + s
        return s

    def load_cache(self, path: str) -> Dict[str, Tuple[float, float]]:
        """加载地理编码缓存"""
        cache = {}
        try:
            with open(path, 'r', encoding='utf-8-sig', newline='') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    a = row.get('address', '').strip()
                    lng = row.get('lng', '')
                    lat = row.get('lat', '')
                    if a and lng and lat:
                        try:
                            cache[a] = (float(lng), float(lat))
                        except:
                            pass
            logger.info(f'加载缓存：{len(cache)} 条')
        except FileNotFoundError:
            logger.info('未找到缓存文件，将新建。')
        return cache

    def save_cache(self, path: str, cache: Dict[str, Tuple[float, float]]):
        """保存地理编码缓存"""
        with open(path, 'w', encoding='utf-8-sig', newline='') as f:
            w = csv.DictWriter(f, fieldnames=['address', 'lng', 'lat'])
            w.writeheader()
            for k, (lng, lat) in cache.items():
                w.writerow({'address': k, 'lng': lng, 'lat': lat})
        logger.info(f'缓存已保存：{len(cache)} 条 -> {path}')

    def amap_geocode(self, address: str) -> Optional[Tuple[float, float]]:
        """高德地理编码API"""
        url = 'https://restapi.amap.com/v3/geocode/geo'
        params = {
            'key': self.API_KEY,
            'address': address,
            'city': self.CITY,
            'citylimit': 'true',
            'batch': 'false'
        }
        r = requests.get(url, params=params, timeout=8)
        data = r.json()
        if data.get('status') == '1' and int(data.get('count', '0')) > 0:
            loc = data['geocodes'][0].get('location')
            if loc:
                lng, lat = loc.split(',')
                return float(lng), float(lat)
        return None

    def amap_place_text(self, keywords: str) -> Optional[Tuple[float, float]]:
        """高德地点搜索API"""
        url = 'https://restapi.amap.com/v3/place/text'
        params = {
            'key': self.API_KEY,
            'keywords': keywords,
            'city': self.CITY,
            'citylimit': 'true',
            'offset': 1,
            'page': 1
        }
        r = requests.get(url, params=params, timeout=8)
        data = r.json()
        if data.get('status') == '1' and int(data.get('count', '0')) > 0:
            loc = data['pois'][0].get('location')
            if loc:
                lng, lat = loc.split(',')
                return float(lng), float(lat)
        return None

    def resolve_address(self, address: str) -> Tuple[Optional[float], Optional[float], str]:
        """解析地址获取坐标"""
        if not address:
            return None, None, 'none'

        # 1) geocode
        for _ in range(self.RETRY + 1):
            try:
                res = self.amap_geocode(address)
                if res:
                    return res[0], res[1], 'geocode'
            except Exception:
                pass
            time.sleep(self.SLEEP_SECONDS)

        # 2) place text（去掉"上海市"再搜）
        kw = address[len(self.CITY_PREFIX):] if address.startswith(self.CITY_PREFIX) else address
        for _ in range(self.RETRY + 1):
            try:
                res = self.amap_place_text(kw)
                if res:
                    return res[0], res[1], 'place'
            except Exception:
                pass
            time.sleep(self.SLEEP_SECONDS)

        return None, None, 'none'

    # ==================== 主导入方法（大事务版本）====================

    def import_all_data_from_excel(self, excel_file_path: str, sheet_name: Optional[Union[int, str]] = 0):
        """从Excel文件导入所有数据（大事务版本）"""
        connection = None
        try:
            logger.info(f"开始读取Excel文件: {excel_file_path}")
            df = pd.read_excel(excel_file_path, sheet_name=sheet_name)
            logger.info(f"Excel文件读取成功，共 {len(df)} 行数据")
            logger.info(f"Excel列名: {list(df.columns)}")

            # 获取数据库连接
            connection = self.get_database_connection()

            # 开始主事务
            self.start_master_transaction(connection)

            # 按顺序导入各个模块的数据
            logger.info("=" * 50)
            logger.info("开始导入患者基本信息...")
            self.import_patient_basic_info(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入病例记录...")
            self.import_injury_records(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入GCS评分...")
            self.import_gcs_scores(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入RTS评分...")
            self.import_rts_scores(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入患者入室信息...")
            self.import_patient_info_on_admission(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入患者离室信息...")
            self.import_patient_info_off_admission(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入干预时间数据...")
            self.import_intervention_time(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入干预补充数据...")
            self.import_intervention_extra(df, connection)

            logger.info("=" * 50)
            logger.info("开始导入ISS数据...")
            self.import_iss_data(df, connection)

            logger.info("=" * 50)
            logger.info("开始更新时间段信息...")
            self.update_time_period(connection)

            logger.info("=" * 50)
            logger.info("开始更新经纬度信息...")
            self.update_longitude_latitude(df, connection)

            # 检查是否有失败的表
            failed_tables = [name for name, result in self.import_results.items() if result['status'] == 'failed']

            if failed_tables:
                logger.error(f"发现失败的表: {failed_tables}")
                self.rollback_master_transaction(connection)
                self.print_final_summary()
                raise Exception(f"数据导入失败，已回滚所有操作。失败的表: {failed_tables}")
            else:
                # 提交主事务
                self.commit_master_transaction(connection)
                self.print_final_summary()
                logger.info("🎉 所有数据导入完成！")

        except Exception as e:
            if connection:
                try:
                    self.rollback_master_transaction(connection)
                except:
                    pass
            logger.error(f"数据导入失败: {e}")
            self.print_final_summary()
            raise
        finally:
            if connection:
                connection.close()


def main():
    """主函数"""
    import sys
    import os
    
    # 检查命令行参数
    if len(sys.argv) < 2:
        print("错误：请提供Excel文件路径")
        print("用法: python sum_import.py <excel_file_path>")
        sys.exit(1)
    
    excel_file_path = sys.argv[1]
    
    # 检查文件是否存在
    if not os.path.exists(excel_file_path):
        print(f"错误：文件不存在 - {excel_file_path}")
        sys.exit(1)
    
    # 数据库配置
    db_config = {
        'host': 'localhost',
        'user': 'root',
        'password': '123',
        'database': 'healthineersvisualization',
        'charset': 'utf8mb4'
    }

    # 创建综合导入器实例
    importer = ComprehensiveDataImporter(db_config)

    try:
        # 从Excel文件导入所有数据
        logger.info(f"开始处理Excel文件: {excel_file_path}")
        importer.import_all_data_from_excel(excel_file_path)
        print("SUCCESS: 数据导入完成")
        return True

    except Exception as e:
        logger.error(f"综合数据导入失败: {e}")
        print(f"ERROR: {str(e)}")
        return False


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)