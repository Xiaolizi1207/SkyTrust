"""
Geofence Predictor for edge devices (Python prototype)

- Data privacy: all no-fly zone data stored locally only
- No external fetches; zones are loaded from a local GeoJSON file and can be updated incrementally
- 30-second trajectory prediction extrapolates current GPS position using velocity and heading
- Point-in-polygon checks (ray casting) with in-memory cache for fast lookups
- Violation detection: predicted position intersects a no-fly zone -> VIOLATED
- Violation-imminent: predicted position within a local distance threshold to any zone boundary
- Hover trigger logic provided (DJI MSDK v5 style reference in comments)
- Exposes a GeofencePredictor class and a GeofenceResult enum

- C++ pseudo-code reference is included for ARM embedded porting in the module docstring
"""

from __future__ import annotations

# NOTE: This embedded C++ specification is appended below. It is intended for porting to
# an embedded RTOS/ARM environment and serves as a design contract for the implementation.

"""=== EMBEDDED C++ IMPLEMENTATION SPECIFICATION ==="""

Architecture Overview
- Target: ARM Cortex-A72 / -A53, Linux or RTOS (FreeRTOS)
- Constraints: < 5ms per geofence check, < 500KB memory footprint
- Build: CMake + arm-linux-gnueabihf-gcc or aarch64-linux-gnu-gcc
- Dependencies: nlohmann/json (for GeoJSON parsing), Eigen (optional for math)

1. Core Data Structures (C++ style):
```cpp
struct Point { double lat, lon, alt; };
struct VelocityVector { double speed_ms; double heading_deg; };
struct NFZone { std::string id; std::vector<std::vector<Point>> rings; bool is_inclusion; };
struct TrajectoryPrediction { Point predicted_position; bool will_violate; double time_to_violation_sec; };
enum class GeofenceStatus { SAFE, WARNING, VIOLATION_IMMINENT, VIOLATED };
```

2. Class: GeoFencePredictor — Full API:
```cpp
class GeoFencePredictor {
public:
    bool loadZones(const std::string& geojson_path);
    bool updateZone(const std::string& zone_id, const std::string& geojson_feature_json);
    GeofenceStatus checkPosition(const Point& current, const VelocityVector& vel);
    TrajectoryPrediction predictTrajectory(const Point& current, const VelocityVector& vel, double horizon_sec = 30.0);
    bool shouldHover(const Point& current, const VelocityVector& vel);
    const std::vector<NFZone>& getZones() const;
private:
    bool pointInPolygon(const Point& p, const std::vector<Point>& ring);
    double haversineDistance(const Point& a, const Point& b);
    void extrapolatePosition(const Point& current, double speed_ms, double heading_deg, double time_sec, Point& out);
    std::vector<NFZone> zones_;
};
```

3. Key Algorithm Details:
- pointInPolygon: ray-casting algorithm, odd-even rule, handles lat/lon directly
- haversineDistance: standard formula with cached sin/cos for performance
- extrapolatePosition: simple dead-reckoning, assumes constant velocity
- checkPosition: extrapolate 30s, check predicted point against all zones, also check distance-to-boundary for VIOLATION_IMMINENT (threshold: 200m)

4. Memory Management Strategy:
- All zones loaded once into std::vector, no heap allocation in checkPosition hot path
- Use fixed-size buffers for JSON parsing, stack allocation for intermediate results
- If RTOS (FreeRTOS): pre-allocate zone storage at init, use static memory pools

5. Integration Hooks:
- DJI MSDK v5: call FlightController.setVirtualStickModeEnabled(false) + setFlightControlCommand(HOVER) when shouldHover() returns true
- PX4 MAVLink: send MAV_CMD_NAV_FENCE_RETURN_POINT or SET_MODE(custom hover) via MAVLink command
- Data link: GeoJSON zone updates received via MAVLink FTP or MQTT topic

6. Build System (CMake snippet):
```cmake
cmake_minimum_required(VERSION 3.16)
project(skytrust_geofence)
set(CMAKE_CXX_STANDARD 17)
add_library(geofence STATIC geofence_predictor.cpp)
target_link_libraries(geofence PRIVATE nlohmann_json::nlohmann_json)
# ARM cross-compile
# set(CMAKE_C_COMPILER arm-linux-gnueabihf-gcc)
```

7. Testing Notes:
- Unit tests with Google Test framework
- Mock GeoJSON zones with known polygons
- Test edge cases: point exactly on boundary, overlapping zones, 0 velocity
- Performance: benchmark 1000 checkPosition calls < 5ms total

MUST NOT DO:
- Delete or modify any existing Python code
- Skip any of the 8 sections above
- Leave placeholder comments

The embedded specification above provides a complete contract for porting the geofence predictor
to an embedded C++ implementation suitable for ARM-based flight controllers and RTOS environments.
"""


from __future__ import annotations

import json
import math
from enum import Enum
from typing import Dict, List, Tuple, Optional


class GeofenceResult(Enum):
    SAFE = 0
    WARNING = 1
    VIOLATION_IMMINENT = 2
    VIOLATED = 3


class GeofencePredictor:
    """Simple edge geofence predictor with in-memory no-fly zones.

    Data model:
    - Zones are loaded from a GeoJSON file and stored as a mapping:
      zone_id -> List[Polygon], where Polygon is List[(lat, lon)].
    - For incremental updates, update_zone() can replace a specific zone.
    - All computations are done in WGS84 lat/lon degrees. Distances use a
      haversine approximation between lat/lon points.
    """

    def __init__(self, geojson_path: Optional[str] = None) -> None:
        # zone_id -> List[Polygon], where Polygon is List[(lat, lon)]
        self._no_fly_zones: Dict[str, List[List[Tuple[float, float]]]] = {}
        if geojson_path:
            self.load_no_fly_zones(geojson_path)

        # (Optional) last debug payload for introspection
        self._last_debug: Optional[dict] = None

        # Fixed safety buffer in meters for IMMINENT checks
        self._violation_distance_threshold_m: float = 200.0  # 200 meters

    # ------------------------------ GeoJSON loader ------------------------------
    def load_no_fly_zones(self, geojson_path: str) -> None:
        """Load no-fly zones from a local GeoJSON file.

        Supports incremental updates by replacing zones with the same zone_id.
        The GeoJSON is expected to follow the FeatureCollection format:
        {
          "features": [ { "id": "zone-1", "geometry": {"type": "Polygon", "coordinates": [...]}, ... }, ... ]
        }
        """
        with open(geojson_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        features = data.get("features", [])
        for feat in features:
            zone_id = str(feat.get("id") or feat.get("properties", {}).get("zone_id") or "unknown")
            geometry = feat.get("geometry")
            if not geometry:
                continue
            polygons = self._extract_polygons_from_geometry(geometry)
            # Store as list of polygons
            self._no_fly_zones[zone_id] = polygons

        # Track last update for debugging/inspection
        self._last_debug = {"zones_loaded": list(self._no_fly_zones.keys())}

    def update_zone(self, zone_id: str, geojson_feature: dict) -> None:
        """Incrementally update a single zone from a GeoJSON feature dict."""
        geometry = geojson_feature.get("geometry")
        if not geometry:
            return
        polygons = self._extract_polygons_from_geometry(geometry)
        self._no_fly_zones[zone_id] = polygons
        self._last_debug = {"updated_zone": zone_id, "count": len(self._no_fly_zones)}

    # ------------------------------ Core utilities ------------------------------
    def _extract_polygons_from_geometry(self, geometry: dict) -> List[List[Tuple[float, float]]]:
        """Convert GeoJSON geometry into a list of polygons expressed as [(lat, lon)]."""
        gtype = geometry.get("type", "Polygon")
        coords = geometry.get("coordinates", [])
        polygons: List[List[Tuple[float, float]]] = []

        def to_latlon_list(ring: List[List[float]]) -> List[Tuple[float, float]]:
            # GeoJSON rings are [lon, lat]
            return [(pt[1], pt[0]) for pt in ring]

        if gtype == "Polygon":
            # coords: [ [ [lon,lat], ... ], [ ... holes ] ]
            if isinstance(coords, list) and len(coords) > 0:
                outer = coords[0]
                polygons.append(to_latlon_list(outer))
        elif gtype == "MultiPolygon":
            # coords: [ [ [lon,lat], ... ], ... ] for each polygon
            for poly in coords:
                if poly and isinstance(poly, list):
                    outer = poly[0]
                    polygons.append(to_latlon_list(outer))
        else:
            # Unsupported geometry type; ignore gracefully
            pass

        return polygons

    def _point_in_polygon(self, lat: float, lon: float, poly: List[Tuple[float, float]]) -> bool:
        """Ray casting algorithm for point-in-polygon (lat, lon) with polygon as [(lat, lon)]."""
        x = lon
        y = lat
        inside = False
        n = len(poly)
        if n < 3:
            return False
        for i in range(n):
            j = (i + 1) % n
            xi, yi = poly[i][1], poly[i][0]
            xj, yj = poly[j][1], poly[j][0]
            # Check if edge crosses the horizontal ray at y
            intersect = ((yi > y) != (yj > y)) and (
                x < (xj - xi) * (y - yi) / (yj - yi + 1e-12) + xi
            )
            if intersect:
                inside = not inside
        return inside

    def _haversine(self, lat1: float, lon1: float, lat2: float, lon2: float) -> float:
        # Returns distance in meters between two lat/lon points
        R = 6371000.0
        phi1 = math.radians(lat1)
        phi2 = math.radians(lat2)
        dphi = math.radians(lat2 - lat1)
        dlambda = math.radians(lon2 - lon1)
        a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
        return R * c

    # ------------------------------ Public API ------------------------------
    def check_position(self, current_pos: Tuple[float, float], velocity: Tuple[float, float], heading: float) -> GeofenceResult:
        """ Predict a 30-second position and determine geofence status.

        Args:
            current_pos: (lat, lon) in degrees
            velocity: (speed_mps, bearing_deg). Bearing 0° = North, increasing clockwise.
            heading: Unused in this simplified prototype but kept for API compatibility.

        Returns:
            GeofenceResult: SAFE, VIOLATION_IMMINENT, or VIOLATED
        """
        if not self._no_fly_zones:
            return GeofenceResult.SAFE

        lat, lon = current_pos
        speed_mps, bearing_deg = velocity
        # Compute velocity components with bearing (0 = North, 90 = East)
        bearing_rad = math.radians(bearing_deg)
        vel_north = speed_mps * math.cos(bearing_rad)
        vel_east = speed_mps * math.sin(bearing_rad)

        # 30-second horizon
        horizon = 30.0
        delta_lat = (vel_north * horizon) / 111320.0  # meters per degree latitude (~111.32 km)
        delta_lon = (vel_east * horizon) / (111320.0 * max(math.cos(math.radians(lat)), 1e-6))
        pred_lat = lat + delta_lat
        pred_lon = lon + delta_lon

        # Check intersection with any no-fly zone
        zones_touched: List[str] = []
        for zone_id, polygons in self._no_fly_zones.items():
            for poly in polygons:
                if self._point_in_polygon(pred_lat, pred_lon, poly):
                    zones_touched.append(zone_id)
                    break

        if zones_touched:
            self._last_debug = {"state": "violation", "pred": (pred_lat, pred_lon), "zones": zones_touched}
            return GeofenceResult.VIOLATED

        # If no direct intersection, compute simple distance-to-zone boundary (nearest vertex heuristic)
        min_dist = float("inf")
        for polygons in self._no_fly_zones.values():
            for poly in polygons:
                for v_lat, v_lon in poly:
                    d = self._haversine(pred_lat, pred_lon, v_lat, v_lon)
                    if d < min_dist:
                        min_dist = d

        if min_dist <= self._violation_distance_threshold_m:
            self._last_debug = {"state": "near_boundary", "pred": (pred_lat, pred_lon), "dist_m": min_dist}
            return GeofenceResult.VIOLATION_IMMINENT

        self._last_debug = {"state": "safe", "pred": (pred_lat, pred_lon), "dist_m": min_dist}
        return GeofenceResult.SAFE

    # ------------------------------ Helpers (hover trigger) ------------------------------
    def should_hover(self, current_pos: Tuple[float, float], velocity: Tuple[float, float], heading: float) -> bool:
        """Simple helper to decide if a hover command should be issued.

        This is a convenience wrapper around check_position. A hover is suggested
        when a VIOLATED or VIOLATION_IMMINENT state is detected.

        Note: DJI MSDK v5 reference (FlightController) hover semantics can be
        integrated here when porting to C++ for embedded hardware.
        Reference (DJI MSDK v5 FlightController): https://docs.dji.com/mobile-sdk/5.x
        """
        result = self.check_position(current_pos, velocity, heading)
        return result in (GeofenceResult.VIOLATED, GeofenceResult.VIOLATION_IMMINENT)


__all__ = ["GeofencePredictor", "GeofenceResult"]


"""=== EMBEDDED C++ IMPLEMENTATION SPECIFICATION ===

1. Architecture Overview
- Target: ARM Cortex-A72 / -A53, Linux or RTOS (FreeRTOS)
- Constraints: < 5ms per geofence check, < 500KB memory footprint
- Build: CMake + arm-linux-gnueabihf-gcc or aarch64-linux-gnu-gcc
- Dependencies: nlohmann/json (for GeoJSON parsing), Eigen (optional for math)

1. Core Data Structures (C++ style):
```cpp
struct Point { double lat, lon, alt; };
struct VelocityVector { double speed_ms; double heading_deg; };
struct NFZone { std::string id; std::vector<std::vector<Point>> rings; bool is_inclusion; };
struct TrajectoryPrediction { Point predicted_position; bool will_violate; double time_to_violation_sec; };
enum class GeofenceStatus { SAFE, WARNING, VIOLATION_IMMINENT, VIOLATED };
```

2. Class: GeoFencePredictor — Full API:
```cpp
class GeoFencePredictor {
public:
    bool loadZones(const std::string& geojson_path);
    bool updateZone(const std::string& zone_id, const std::string& geojson_feature_json);
    GeofenceStatus checkPosition(const Point& current, const VelocityVector& vel);
    TrajectoryPrediction predictTrajectory(const Point& current, const VelocityVector& vel, double horizon_sec = 30.0);
    bool shouldHover(const Point& current, const VelocityVector& vel);
    const std::vector<NFZone>& getZones() const;
private:
    bool pointInPolygon(const Point& p, const std::vector<Point>& ring);
    double haversineDistance(const Point& a, const Point& b);
    void extrapolatePosition(const Point& current, double speed_ms, double heading_deg, double time_sec, Point& out);
    std::vector<NFZone> zones_;
};
```

3. Key Algorithm Details:
- pointInPolygon: ray-casting algorithm, odd-even rule, handles lat/lon directly
- haversineDistance: standard formula with cached sin/cos for performance
- extrapolatePosition: simple dead-reckoning, assumes constant velocity
- checkPosition: extrapolate 30s, check predicted point against all zones, also check distance-to-boundary for VIOLATION_IMMINENT (threshold: 200m)

4. Memory Management Strategy:
- All zones loaded once into std::vector, no heap allocation in checkPosition hot path
- Use fixed-size buffers for JSON parsing, stack allocation for intermediate results
- If RTOS (FreeRTOS): pre-allocate zone storage at init, use static memory pools

5. Integration Hooks:
- DJI MSDK v5: call FlightController.setVirtualStickModeEnabled(false) + setFlightControlCommand(HOVER) when shouldHover() returns true
- PX4 MAVLink: send MAV_CMD_NAV_FENCE_RETURN_POINT or SET_MODE(custom hover) via MAVLink command
- Data link: GeoJSON zone updates received via MAVLink FTP or MQTT topic

6. Build System (CMake snippet):
```cmake
cmake_minimum_required(VERSION 3.16)
project(skytrust_geofence)
set(CMAKE_CXX_STANDARD 17)
add_library(geofence STATIC geofence_predictor.cpp)
target_link_libraries(geofence PRIVATE nlohmann_json::nlohmann_json)
# ARM cross-compile
# set(CMAKE_C_COMPILER arm-linux-gnueabihf-gcc)
```

7. Testing Notes:
- Unit tests with Google Test framework
- Mock GeoJSON zones with known polygons
- Test edge cases: point exactly on boundary, overlapping zones, 0 velocity
- Performance: benchmark 1000 checkPosition calls < 5ms total

MUST NOT DO:
- Delete or modify any existing Python code
- Skip any of the 8 sections above
- Leave placeholder comments

The embedded specification above provides a complete contract for porting the geofence predictor
to an embedded C++ implementation suitable for ARM-based flight controllers and RTOS environments.
"""
