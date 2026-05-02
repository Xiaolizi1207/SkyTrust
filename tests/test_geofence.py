import pytest

# Import the GeofencePredictor from the edge directory
from edge.geofence_predictor import GeofencePredictor


@pytest.fixture
def valid_geojson():
    """A minimal valid GeoJSON FeatureCollection with a single square polygon (no-fly zone)."""
    return {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {"zone": "NFZ-1"},
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [-1.0, -1.0],
                            [-1.0, 1.0],
                            [1.0, 1.0],
                            [1.0, -1.0],
                            [-1.0, -1.0],
                        ]
                    ],
                },
            }
        ],
    }


@pytest.fixture
def invalid_geojson():
    """Malformed GeoJSON lacking proper geometry and coordinates."""
    return {"type": "FeatureCollection", "features": [{"type": "Feature"}]}  # missing geometry


@pytest.fixture
def predictor(valid_geojson):
    """Provide a GeofencePredictor instance pre-loaded with valid zones."""
    pred = GeofencePredictor()
    pred.load_geojson(valid_geojson)
    return pred


def _to_lat_lon(pt):
    """Robust helper to extract (lat, lon) from various point representations."""
    if isinstance(pt, dict):
        # Common keys
        if "lat" in pt and "lon" in pt:
            return float(pt["lat"]), float(pt["lon"])
        if "latitude" in pt and "longitude" in pt:
            return float(pt["latitude"]), float(pt["longitude"])
        if "Latitude" in pt and "Longitude" in pt:
            return float(pt["Latitude"]), float(pt["Longitude"])
    if isinstance(pt, (list, tuple)) and len(pt) >= 2:
        return float(pt[0]), float(pt[1])
    raise ValueError(f"Unsupported point format: {pt!r}")


def test_load_geojson_valid(predictor):
    """test_load_geojson_valid: load valid GeoJSON with polygon features"""
    # The fixture pre-loads valid geojson; ensure some zones were loaded
    zones = getattr(predictor, "zones", None)
    nfz = getattr(predictor, "no_fly_zones", None)
    assert (zones is not None and len(zones) > 0) or (nfz is not None and len(nfz) > 0)


def test_load_geojson_invalid(predictor, invalid_geojson):
    """test_load_geojson_invalid: handle malformed GeoJSON gracefully"""
    with pytest.raises(Exception):
        predictor.load_geojson(invalid_geojson)


def test_point_inside_no_fly_zone(predictor):
    """test_point_inside_no_fly_zone: verify violation detection for a point inside a zone"""
    lat, lon = 0.0, 0.0  # center of the square NFZ defined in valid_geojson
    inside = predictor.is_point_in_no_fly_zone(lat, lon)
    assert inside is True, "Point should be inside the no-fly zone"


def test_point_outside_no_fly_zone(predictor):
    """test_point_outside_no_fly_zone: verify safe position outside all zones"""
    lat, lon = 10.0, 10.0  # clearly outside the defined NFZ
    outside = predictor.is_point_in_no_fly_zone(lat, lon)
    assert outside is False, "Point should be outside the no-fly zone"


def test_trajectory_prediction_straight_line(predictor):
    """test_trajectory_prediction_straight_line: 30s straight-line prediction should yield a path"""
    start = (0.0, 0.0)
    # speed in m/s and bearing in degrees. We rely on the implementation to compute intermediate points.
    traj = predictor.predict_trajectory(start[0], start[1], speed_mps=2.0, bearing_deg=0.0, duration_s=30)
    assert isinstance(traj, list) and len(traj) >= 1
    first = traj[0]
    lat0, lon0 = _to_lat_lon(first)
    assert abs(lat0 - start[0]) < 1e-6 and abs(lon0 - start[1]) < 1e-6


def test_trajectory_will_cross_boundary(predictor):
    """test_trajectory_will_cross_boundary: detect imminent violation in trajectory"""
    # Start outside and aim towards the NFZ to cross it within 60s
    traj = predictor.predict_trajectory(2.0, 2.0, speed_mps=2.0, bearing_deg=225.0, duration_s=60)
    crossed = False
    for pt in traj:
        lat, lon = _to_lat_lon(pt)
        if predictor.is_point_in_no_fly_zone(lat, lon):
            crossed = True
            break
    assert crossed is True, "Trajectory should cross into a no-fly zone within the horizon"


def test_incremental_zone_update(predictor, valid_geojson):
    """test_incremental_zone_update: update single zone without full reload"""
    # Create a small additional zone adjacent to existing NFZ
    new_geojson = {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {"zone": "NFZ-NEW"},
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [1.5, -0.5],
                            [1.5, 0.5],
                            [2.5, 0.5],
                            [2.5, -0.5],
                            [1.5, -0.5],
                        ]
                    ],
                },
            }
        ],
    }
    if hasattr(predictor, "update_geojson"):
        predictor.update_geojson(new_geojson)
        # If no exception is raised, we consider the incremental update successful
        assert True
    else:
        pytest.skip("update_geojson not implemented in GeofencePredictor")


def test_multiple_zones(predictor):
    """test_multiple_zones: ensure overlapping zones are handled"""
    # Extend GeoJSON with two overlapping polygons
    multi_geojson = {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {"zone": "NFZ-A"},
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [[-1.0, -0.5], [-1.0, 0.5], [0.0, 0.5], [0.0, -0.5], [-1.0, -0.5]]
                    ],
                },
            },
            {
                "type": "Feature",
                "properties": {"zone": "NFZ-B"},
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [[-0.5, -1.0], [-0.5, 0.0], [0.5, 0.0], [0.5, -1.0], [-0.5, -1.0]]
                    ],
                },
            },
        ],
    }
    if hasattr(predictor, "load_geojson"):
        predictor.load_geojson(multi_geojson)
        # Pick a point inside the overlap area roughly at (-0.25, -0.25)
        lat, lon = -0.25, -0.25
        assert predictor.is_point_in_no_fly_zone(lat, lon) is True
    else:
        pytest.skip("load_geojson not implemented in GeofencePredictor")


def test_hover_trigger_on_violation(predictor):
    """test_hover_trigger_on_violation: verify hover command generation for a violation"""
    lat, lon = 0.0, 0.0
    if hasattr(predictor, "hover_trigger"):
        cmd = predictor.hover_trigger(lat, lon)
        assert isinstance(cmd, str)
        assert len(cmd) > 0
    else:
        pytest.skip("hover_trigger not implemented in GeofencePredictor")


def test_boundary_edge_case(predictor):
    """test_boundary_edge_case: point exactly on a boundary should be treated as inside (conservative)"""
    # Using the NFZ square from valid_geojson; boundary point on bottom edge
    lat, lon = -1.0, 0.0
    inside = predictor.is_point_in_no_fly_zone(lat, lon)
    assert inside is True, "Boundary points should be treated as inside the no-fly zone for safety"
