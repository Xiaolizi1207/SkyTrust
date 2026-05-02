AI Geofence Module Specification (SkyTrust) - Comprehensive (Draft)

Sections:
- System architecture: 机载端 vs 云端
- Real-time geofence flow
- NFZ JSON format
- Geofence predictor: C++/Python pseudo-code (DJI MSDK v5)
- TF Lite model I/O spec
- Flow diagram (Mermaid)
- Data privacy & incremental sync
- Command priority rules
- Data models
- Verification & testing plan

1) System architecture
- 机载端（On-board）负责低时延地理围栏检测、局部 NFZ 缓存、快速 hover 控制，以及断网保护。
- 云端（Cloud）维护全局 NFZ 数据、模型更新、策略下发、日志分析。
- 双端协作：设备端执行核心决策，云端提供增量同步与策略管理。数据传输采用 TLS，最小化隐私暴露。
- 数据生命周期：NFZ 数据版本化，设备端缓存本地副本，云端负责 diff 与 full sync 的切换。
- 架构要点：事件驱动、低功耗模式、容错设计、可观测性。 

2) Real-time geofence flow
- 数据输入：GPS、IMU、Velocities 提供当前状态。
- 步骤：
  1. NFZ 区域匹配（快速近邻判定）
  2. 30s 预测：评估未来轨迹是否进入 NFZ
  3. Hover 触发：若风险为真，立即 Hover
  4. 遥控冲突处理：Hover 优先，遥控输入被抑制，等待安全解除
  5. 正常状态：无风险时继续执行遥控指令
- 延迟目标：目标端到端延迟 < 100 ms；30s 预测用于风险预判。
- 流程控制点：阈值、缓冲距离、预测不确定性处理。

3) GeoJSON NFZ format
- 规范：FeatureCollection -> Features -> properties + geometry
- 示例坐标系：WGS84（经纬度，单位度）
- NFZ 属性：id, name, effective_from, confidence
- geometry: Polygon，coordinates 为多边形点序列
- 版本与同步：通过 version 与 diff 推送增量变更，设备端增量应用。
- 安全审计：NFZ 变更日志、版本历史、以及签名校验。

示例（Sample）:
```json
{
  "type": "FeatureCollection",
  "version": "20260501.01",
  "features": [
    {
      "type": "Feature",
      "properties": {
        "id": "NFZ-004",
        "name": "Downtown No-Fly Zone",
        "effective_from": "2026-01-01T00:00:00Z",
        "confidence": 0.95
      },
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [
            [-122.421, 37.774],
            [-122.421, 37.780],
            [-122.412, 37.780],
            [-122.412, 37.774],
            [-122.421, 37.774]
          ]
        ]
      }
    },
    {
      "type": "Feature",
      "properties": {
        "id": "NFZ-012",
        "name": "Airport Airspace Adjacency",
        "effective_from": "2025-06-01T00:00:00Z",
        "confidence": 0.88
      },
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [
            [-74.0059, 40.7128],
            [-74.0060, 40.7138],
            [-74.0049, 40.7142],
            [-74.0045, 40.7130],
            [-74.0059, 40.7128]
          ]
        ]
      }
    }
  ]
}
```

4) Geofence predictor（C++/Python）
- 目标：给定当前位姿与速度，预测未来 30 秒是否进入 NFZ，并给出 Hover 指令。
- 参考实现要点：对接 DJI MSDK v5 FlightController 的回调与指令接口。

### 4.1 C++ 伪代码
```cpp
// Pseudo-code: Geofence predictor using DJI MSDK v5 FlightController reference
#include <DJI_FlightController.hpp> // placeholder

struct Pose { double lat; double lon; double alt_m; };
struct Vec3 { double x; double y; double z; };

class GeofencePredictor {
public:
  GeofencePredictor(const char* zones_geojson_path);
  bool predictHover(const Pose& pos, const Vec3& vel, double horizon_sec);
private:
  void loadZones(const char* path);
  bool insideNFZ(double lat, double lon, double alt, double t_sec = 0.0);
  // internal spatial index (abstracted)
};

GeofencePredictor::GeofencePredictor(const char* zones_geojson_path) { loadZones(zones_geojson_path); }

bool GeofencePredictor::predictHover(const Pose& pos, const Vec3& vel, double horizon_sec) {
  if (insideNFZ(pos.lat, pos.lon, pos.alt_m)) return true;
  double meters_per_deg_lat = 111320.0;
  double next_lat = pos.lat + (vel.y * horizon_sec) / meters_per_deg_lat;
  double next_lon = pos.lon + (vel.x * horizon_sec) / (meters_per_deg_lat * cos(pos.lat * M_PI/180.0));
  double next_alt = pos.alt_m + vel.z * horizon_sec;
  if (insideNFZ(next_lat, next_lon, next_alt)) return true;
  return false;
}

void GeofencePredictor::loadZones(const char* path) {
  // parse GeoJSON and build internal NFZ polygons (placeholder)
}

bool GeofencePredictor::insideNFZ(double lat, double lon, double alt, double t_sec) {
  // point-in-polygon check against loaded NFZ polygons (placeholder)
  return false;
}
```

### 4.2 Python 伪代码
```python
# Pseudo-code: Geofence predictor using DJI MSDK v5 FlightController reference (Python-like)
import json
import math

class GeofencePredictor:
    def __init__(self, zones_geojson_path):
        with open(zones_geojson_path) as f:
            self.zones = json.load(f)["features"]

    def inside_nfuzz(self, lat, lon, alt):
        for f in self.zones:
            poly = f['geometry']['coordinates'][0]
            if self.point_in_polygon((lon, lat), poly):
                return True
        return False

    def point_in_polygon(self, pt, poly):
        x, y = pt
        inside = False
        n = len(poly)
        for i in range(n):
            x1, y1 = poly[i]
            x2, y2 = poly[(i+1)%n]
            if ((y1 > y) != (y2 > y)) and (x < (x2-x1)*(y-y1)/(y2-y1) + x1):
                inside = not inside
        return inside

    def predict_hover(self, lat, lon, alt, vx, vy, vz, horizon=30.0):
        if self.inside_nfuzz(lat, lon, alt):
            return True
        next_lat = lat + (vy * horizon) / 111000.0
        next_lon = lon + (vx * horizon) / (111000.0 * math.cos(lat * math.pi/180.0))
        next_alt = alt + vz * horizon
        return self.inside_nfuzz(next_lat, next_lon, next_alt)
```

> 注：以上为伪代码，实际实现应对接 DJI MSDK v5 FlightController 的具体回调与指令接口（悬停指令、航向/速度控制等）。

5) TF Lite obstacle avoidance model I/O spec
- Input: image tensor [H, W, 3] uint8 or float32 [0,1]
- Output: depth_map [1, H, W] float32 or avoidance_vector [1,3] float32
- Depth map unit: meters; Avoidance vector: 3D in device frame
- Supported resolutions: 160x120, 320x180, 640x360; quantized int8 variant recommended for runtime; floating variant kept for accuracy

6) Flow diagram (Mermaid)
```mermaid
flowchart TD
  GPS[GPS Data] --> NFZCheck{NFZ Proximity?}
  NFZCheck -->|Yes| Predict[30s Geofence Predictor]
  NFZCheck -->|No| Continue[Continue Normal Operation]
  Predict --> Hover{30s Hover Risk?}
  Hover -->|Yes| HoverCmd[Hover Command Issued]
  Hover -->|No| Continue
  HoverCmd --> FC[Flight Controller (DJI MSDK v5) – Hover]
  Continue --> Controller[Control Loop / Remote Input]
  Controller --> Log[Event Log]
```

7) Data privacy & incremental sync strategy
- NFZ 数据本地缓存，云端提供版本化增量更新。
- Incremental Sync：变更仅推送差异，设备端应用增量更新。
- 加密与访问控制：TLS 传输、AES-256 本地存储。
- 日志与审计：对地理数据变更与推理决策进行审计。
- 风险控制：在离线状态下，仍可对 NFZ 进行本地检测与 Hover。

8) Command priority rules（指令优先级规则）
- Hover 为最高优先级，覆盖遥控指令、任务调度、以及自动任务。
- 风险预测显示高风险时，立即进入 Hover，直到风险解除。
- 若无 NFZ 风险，遥控输入维持正常执行。

9) Data models（数据模型定义）
- NFZFeature：{ id, name, effective_from, confidence }
- NFZGeometry：{ type: Polygon, coordinates: [[[lon, lat], ...]] }
- GeofencePredictionResult：{ will_enter_nf: bool, risk_score: float, horizon_sec: int }
- ModelConfig：{ use_depth_map: bool, output_resolution: (H, W) }

10) Verification & testing plan
- 单元测试、集成测试、性能测试、以及安全测试的完整清单。

11) 版本与变更历史
- 记录修改日期、变更内容、影响范围。

11) Glossary & references（术语表与参考）
- NFZ: No-Fly Zone，禁飞区边界定义区域。
- MSDK: DJI Mobile Software Development Kit，版本参考 v5 FlightController。
- GPS: Global Positioning System，全球定位系统。
- hover: 悬停，保持当前位置。
- incremental sync: 增量同步，只传输差异变更。
- latency: 延迟，单位毫秒（ms）。

12) API mock examples（接口模拟示例）
- NFZ local fetch：GET /api/nfz/local?version=20260501.01
- Incremental diff：POST /api/nfz/diff with payload {"diff":"..."}
- Hover command 下发：POST /api/drone/{drone_id}/command with payload {"command":"hover"}

13) Testing & validation approach（测试与验证方法）
- 单元测试：NFZ 点是否在多边形内、30s 预测正确率、边界边界点测试。
- 集成测试：GPS 流、NFZ 数据下发、预测模块、航线执行之间的完整链路。
- 性能测试：预测调用时延、总延迟、资源占用、以及深度学习模型推理时间。
- 安全测试：断网场景、数据加密、以及回滚策略。
- 回归测试计划：版本变更后的回归覆盖率。

14) Implementation notes（实现笔记）
- 伪代码与接口文档分离，确保不同实现语言的可移植性。
- 设备端的本地缓存应有版本控制和错误处理策略，避免数据不一致。
- 云端应提供统一的 diff 格式以便设备端快速、可靠地增量更新 NFZ。
- 本地和云端之间的通信应尽可能在高可用状态下工作，必要时支持离线模式。

结论扩展：本节为 Geofence 规格的扩展，确保团队在实现阶段具备清晰的参考与执行路径。 

15) Final notes & appendix（尾注与补充）
- 如需联调，请参考 DJI MSDK v5 FlightController 的官方文档中的 Command API、Mission Planner、以及 Hover 指令的具体字段。
- Appendix A：sample command sequence for hover-triggered emergency stop could be added in future版本。
- Appendix B：部署要点、可观测性指标、以及回滚策略。
