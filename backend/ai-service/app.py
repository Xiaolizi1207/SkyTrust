"""
SkyTrust AI服务 - 模拟AI服务
提供调度算法、视觉检测、风险评估、动态定价等功能
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
from datetime import datetime
import random
import logging

app = Flask(__name__)
CORS(app)  # 允许跨域请求

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/health', methods=['GET'])
def health():
    """健康检查接口"""
    return jsonify({
        'status': 'healthy',
        'service': 'skytrust-ai-service',
        'timestamp': datetime.now().isoformat(),
        'version': '1.0.0'
    })

@app.route('/api/v1/scheduling/optimize', methods=['POST'])
def optimize_scheduling():
    """
    优化无人机调度
    输入: 设备列表、用户位置、需求列表
    输出: 最优调度方案
    """
    try:
        data = request.get_json()

        # 模拟调度算法
        devices = data.get('devices', [])
        user_location = data.get('user_location', {})
        demands = data.get('demands', [])

        # 简单的最邻近算法示例
        results = []
        for demand in demands:
            # 找到最近的可用设备
            available_devices = [d for d in devices if d.get('status') == 'available']

            if available_devices:
                # 计算距离并排序
                for device in available_devices:
                    device_lat = device.get('latitude', 0)
                    device_lng = device.get('longitude', 0)
                    user_lat = user_location.get('latitude', 0)
                    user_lng = user_location.get('longitude', 0)

                    # 简单距离计算（实际应使用Haversine公式）
                    distance = ((device_lat - user_lat) ** 2 + (device_lng - user_lng) ** 2) ** 0.5
                    device['distance'] = distance

                # 按距离排序
                available_devices.sort(key=lambda x: x['distance'])

                # 选择最近的设备
                selected_device = available_devices[0]

                results.append({
                    'demand_id': demand.get('id'),
                    'device_id': selected_device.get('id'),
                    'device_name': selected_device.get('name'),
                    'estimated_distance': selected_device['distance'],
                    'estimated_time': selected_device['distance'] * 2,  # 简单估算时间
                    'confidence': 0.85
                })

        return jsonify({
            'success': True,
            'data': {
                'schedule_id': f'sch-{datetime.now().strftime("%Y%m%d%H%M%S")}',
                'results': results,
                'total_devices_assigned': len(results),
                'algorithm': 'nearest_neighbor',
                'processing_time': 0.15
            }
        })

    except Exception as e:
        logger.error(f"调度优化失败: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/v1/vision/detect', methods=['POST'])
def vision_detect():
    """
    视觉检测
    输入: 图像数据或URL
    输出: 检测结果
    """
    try:
        data = request.get_json()
        image_url = data.get('image_url')
        image_data = data.get('image_data')  # Base64编码

        # 模拟视觉检测结果
        detections = [
            {
                'object': 'drone',
                'confidence': 0.95,
                'bbox': [100, 150, 200, 250],  # [x1, y1, x2, y2]
                'status': 'flying'
            },
            {
                'object': 'person',
                'confidence': 0.88,
                'bbox': [300, 200, 350, 300],
                'status': 'standing'
            },
            {
                'object': 'obstacle',
                'confidence': 0.75,
                'bbox': [400, 100, 450, 150],
                'status': 'static'
            }
        ]

        # 风险评估
        risk_score = 0.2  # 低风险
        if any(d['object'] == 'person' and d['confidence'] > 0.8 for d in detections):
            risk_score = 0.6  # 中风险
        if any(d['object'] == 'obstacle' and d['confidence'] > 0.7 for d in detections):
            risk_score = min(risk_score + 0.3, 1.0)  # 增加风险

        return jsonify({
            'success': True,
            'data': {
                'detection_id': f'det-{datetime.now().strftime("%Y%m%d%H%M%S")}',
                'detections': detections,
                'risk_assessment': {
                    'risk_score': risk_score,
                    'risk_level': 'low' if risk_score < 0.4 else 'medium' if risk_score < 0.7 else 'high',
                    'recommendations': ['保持安全距离', '注意周边环境'] if risk_score > 0.5 else ['环境安全']
                },
                'processing_time': 0.25
            }
        })

    except Exception as e:
        logger.error(f"视觉检测失败: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/v1/risk/assess', methods=['POST'])
def assess_risk():
    """
    风险评估
    输入: 用户信息、设备信息、环境信息
    输出: 风险评分和建议
    """
    try:
        data = request.get_json()
        user_info = data.get('user_info', {})
        device_info = data.get('device_info', {})
        environment_info = data.get('environment_info', {})

        # 模拟风险评估模型
        risk_factors = []

        # 用户信用评分
        credit_score = user_info.get('credit_score', 100)
        credit_risk = max(0, 1 - credit_score / 100)
        risk_factors.append(('credit', credit_risk))

        # 设备状况
        device_age = device_info.get('age_months', 0)
        device_risk = min(1.0, device_age / 60)  # 超过5年风险高
        risk_factors.append(('device_age', device_risk))

        # 天气条件
        weather = environment_info.get('weather', 'clear')
        weather_risk = 0.1 if weather == 'clear' else 0.5 if weather == 'cloudy' else 0.8
        risk_factors.append(('weather', weather_risk))

        # 时间段风险（夜间风险高）
        hour = datetime.now().hour
        time_risk = 0.3 if 6 <= hour < 18 else 0.7
        risk_factors.append(('time', time_risk))

        # 计算综合风险
        weights = {'credit': 0.4, 'device_age': 0.3, 'weather': 0.2, 'time': 0.1}
        total_risk = sum(risk * weights[factor] for factor, risk in risk_factors)

        # 风险等级
        if total_risk < 0.3:
            risk_level = 'low'
            recommendation = '风险较低，可正常租赁'
        elif total_risk < 0.6:
            risk_level = 'medium'
            recommendation = '中等风险，建议加强监控'
        else:
            risk_level = 'high'
            recommendation = '高风险，建议拒绝或增加押金'

        return jsonify({
            'success': True,
            'data': {
                'assessment_id': f'risk-{datetime.now().strftime("%Y%m%d%H%M%S")}',
                'risk_score': total_risk,
                'risk_level': risk_level,
                'risk_factors': [
                    {'factor': factor, 'score': score, 'weight': weights.get(factor, 0)}
                    for factor, score in risk_factors
                ],
                'recommendation': recommendation,
                'suggested_deposit_multiplier': min(2.0, 1.0 + total_risk * 2),  # 押金倍数
                'processing_time': 0.12
            }
        })

    except Exception as e:
        logger.error(f"风险评估失败: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/v1/pricing/dynamic', methods=['POST'])
def dynamic_pricing():
    """
    动态定价
    输入: 设备信息、需求信息、市场状况
    输出: 推荐价格
    """
    try:
        data = request.get_json()
        device_info = data.get('device_info', {})
        demand_info = data.get('demand_info', {})
        market_info = data.get('market_info', {})

        # 基础价格
        base_price = device_info.get('base_price_per_hour', 50.0)

        # 设备因素
        device_age = device_info.get('age_months', 0)
        device_factor = max(0.7, 1.0 - device_age / 120)  # 设备越新，价格因子越高

        battery_level = device_info.get('battery_level', 100)
        battery_factor = battery_level / 100

        # 需求因素
        demand_level = demand_info.get('demand_level', 'normal')  # low, normal, high, peak
        demand_factors = {'low': 0.8, 'normal': 1.0, 'high': 1.3, 'peak': 1.6}
        demand_factor = demand_factors.get(demand_level, 1.0)

        # 时间因素
        hour = datetime.now().hour
        if 7 <= hour < 9 or 17 <= hour < 19:
            time_factor = 1.3  # 早晚高峰
        elif 22 <= hour or hour < 6:
            time_factor = 1.5  # 夜间加价
        else:
            time_factor = 1.0

        # 天气因素
        weather = market_info.get('weather', 'clear')
        weather_factors = {'clear': 1.0, 'cloudy': 1.1, 'rainy': 1.4, 'windy': 1.3}
        weather_factor = weather_factors.get(weather, 1.0)

        # 计算最终价格
        final_price = base_price * device_factor * battery_factor * demand_factor * time_factor * weather_factor

        # 价格范围限制
        min_price = base_price * 0.6
        max_price = base_price * 2.0
        final_price = max(min_price, min(final_price, max_price))

        # 价格解释
        factors = [
            {'name': '设备新旧', 'factor': device_factor, 'impact': f'{((device_factor-1)*100):+.1f}%'},
            {'name': '电池电量', 'factor': battery_factor, 'impact': f'{((battery_factor-1)*100):+.1f}%'},
            {'name': '需求水平', 'factor': demand_factor, 'impact': f'{((demand_factor-1)*100):+.1f}%'},
            {'name': '时间段', 'factor': time_factor, 'impact': f'{((time_factor-1)*100):+.1f}%'},
            {'name': '天气条件', 'factor': weather_factor, 'impact': f'{((weather_factor-1)*100):+.1f}%'},
        ]

        return jsonify({
            'success': True,
            'data': {
                'price_id': f'price-{datetime.now().strftime("%Y%m%d%H%M%S")}',
                'base_price': base_price,
                'final_price': round(final_price, 2),
                'price_factors': factors,
                'suggested_price_range': {
                    'min': round(min_price, 2),
                    'max': round(max_price, 2)
                },
                'market_insights': {
                    'demand_trend': demand_level,
                    'recommended_action': '降价促销' if demand_level == 'low' else '维持价格' if demand_level == 'normal' else '适当提价',
                    'competition_level': market_info.get('competition', 'medium')
                },
                'processing_time': 0.08
            }
        })

    except Exception as e:
        logger.error(f"动态定价失败: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/api/v1/ai/batch', methods=['POST'])
def batch_processing():
    """
    批量处理接口
    可同时处理多个AI任务
    """
    try:
        data = request.get_json()
        tasks = data.get('tasks', [])

        results = []
        for task in tasks:
            task_type = task.get('type')
            task_data = task.get('data', {})

            # 根据任务类型调用相应的处理函数
            if task_type == 'scheduling':
                # 这里简化处理，实际应调用optimize_scheduling逻辑
                result = {'type': 'scheduling', 'status': 'processed', 'result': 'scheduled'}
            elif task_type == 'risk':
                result = {'type': 'risk', 'status': 'processed', 'result': 'assessed'}
            elif task_type == 'pricing':
                result = {'type': 'pricing', 'status': 'processed', 'result': 'calculated'}
            else:
                result = {'type': task_type, 'status': 'unsupported', 'error': f'未知任务类型: {task_type}'}

            results.append(result)

        return jsonify({
            'success': True,
            'data': {
                'batch_id': f'batch-{datetime.now().strftime("%Y%m%d%H%M%S")}',
                'total_tasks': len(tasks),
                'processed_tasks': len([r for r in results if r['status'] == 'processed']),
                'results': results,
                'processing_time': 0.05 * len(tasks)
            }
        })

    except Exception as e:
        logger.error(f"批量处理失败: {str(e)}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)