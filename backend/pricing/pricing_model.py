"""
Pricing model module
=================================

- Feature engineering class with at least 12 features (see features list below).
- Model training class that supports two model types: LSTM-like (surrogate using MLPRegressor)
  and XGBoost, exposed with a sklearn-compatible interface.
- Price adjustment logic implementing bounds:
  final_price = clamp(base_price * demand_multiplier, min_price, max_price)
- Dynamic pricing interface:
  def predict_price(features: Dict[str, float], base_id: str, timestamp: datetime) -> float
- Model persistence (save/load) for both model types.
- Type hints throughout and thorough docstrings.
- REST API input/output example included in docstrings.
"""

from __future__ import annotations

import json
import os
import pickle
from datetime import datetime, timedelta
from typing import Dict, Iterable, List, Optional, Tuple

import numpy as np
from sklearn.base import BaseEstimator, RegressorMixin
from sklearn.neural_network import MLPRegressor
import xgboost as xgb


# ------------------------- Feature Engineering -------------------------
class FeatureEngineering:
    """Engineering of pricing features for demand forecasting.

    The class exposes a single static method to compute a fixed set of features
    from raw sensor-like inputs and a timestamp. The implementation is designed
    to be dependency-light (no pandas required) and to be deterministic.

    Features produced (at least 12):
    - tourist_heat_index
    - wind_speed
    - rainfall
    - visibility
    - temperature
    - hour_of_day
    - day_of_week
    - is_holiday
    - historical_rental_count
    - time_to_next_peak
    - weather_score
    - season_index
    """

    @staticmethod
    def compute_features(raw: Dict[str, float | int | bool],
                         timestamp: datetime) -> Dict[str, float]:
        """Compute engineered features from raw input and a timestamp.

        Parameters
        - raw: raw input dictionary containing core features. Keys are optional
          and will be defaulted if missing.
        - timestamp: the current timestamp to derive time-based features.

        Returns
        - A dictionary with all engineered features as floats (and booleans
          coerced to 0/1 where appropriate).
        """

        temperature = float(raw.get("temperature", 20.0))
        wind_speed = float(raw.get("wind_speed", 5.0))
        rainfall = float(raw.get("rainfall", 0.0))
        visibility = float(raw.get("visibility", 10.0))

        hour_of_day = int(raw.get("hour_of_day", timestamp.hour))
        day_of_week = int(raw.get("day_of_week", int(timestamp.weekday())))
        is_holiday = int(bool(raw.get("is_holiday", False)))
        historical_rental_count = int(raw.get("historical_rental_count", 0))
        time_to_next_peak = float(raw.get("time_to_next_peak", 0.0))
        weather_score = float(raw.get("weather_score", 0.5))
        season_index = int(raw.get("season_index", 0))

        # Simple, deterministic heuristic for heat index.
        tourist_heat_index = max(0.0,
                                 temperature * 0.8 - rainfall * 0.25 - wind_speed * 0.15)

        features: Dict[str, float] = {
            "tourist_heat_index": tourist_heat_index,
            "wind_speed": wind_speed,
            "rainfall": rainfall,
            "visibility": visibility,
            "temperature": temperature,
            "hour_of_day": float(hour_of_day),
            "day_of_week": float(day_of_week),
            "is_holiday": float(is_holiday),
            "historical_rental_count": float(historical_rental_count),
            "time_to_next_peak": time_to_next_peak,
            "weather_score": weather_score,
            "season_index": float(season_index),
        }

        return features


# ------------------------- Model Training & Persistence -------------------------
class LSTMRegressor(BaseEstimator, RegressorMixin):
    """A lightweight LSTM-like regressor compatible with sklearn API.

    This project cannot depend on heavy deep learning stacks in every
    environment. To provide an sklearn-compatible API for an "LSTM-like"
    model, this class wraps an MLPRegressor and accepts input shaped either as
    (n_samples, n_features) or as (n_samples, timesteps, features) which is
    flattened internally. This is a practical surrogate that can capture
    sequential-like information via engineered features (lookback contiguity).
    """

    def __init__(self, hidden_layer_sizes: Tuple[int, ...] = (64, 32),
                 random_state: int = 42, max_iter: int = 500, lookback: int = 3):
        self.hidden_layer_sizes = hidden_layer_sizes
        self.random_state = random_state
        self.max_iter = max_iter
        self.lookback = lookback
        self._model: Optional[MLPRegressor] = None

    def _prepare(self, X: np.ndarray) -> np.ndarray:
        if X.ndim == 2:
            return X
        if X.ndim == 3:
            # Flatten timesteps x features per timestep
            n_samples, t, f = X.shape
            return X.reshape(n_samples, t * f)
        raise ValueError("Unsupported input shape for LSTMRegressor: {}".format(X.shape))

    def fit(self, X: np.ndarray, y: np.ndarray) -> 'LSTMRegressor':
        Xp = self._prepare(X)
        self._model = MLPRegressor(hidden_layer_sizes=self.hidden_layer_sizes,
                                 random_state=self.random_state,
                                 max_iter=self.max_iter)
        self._model.fit(Xp, y)
        return self

    def predict(self, X: np.ndarray) -> np.ndarray:
        if self._model is None:
            raise ValueError("Model has not been fitted yet.")
        Xp = self._prepare(X)
        return self._model.predict(Xp)


class PricingModelTrainer:
    """Train and serve pricing models with a sklearn-compatible interface.

    Supports two backends:
    - 'XGBoost': xgboost.XGBRegressor
    - 'LSTM': LSTMRegressor (surrogate, see class docstring)
    """

    def __init__(self, model_type: str = "XGBoost", model_path: str = None, **kwargs):
        model_type = (model_type or "XGBoost").strip()
        self.model_type = model_type
        self._model = None  # type: ignore
        self.model_path = model_path or os.path.join(
            os.path.dirname(__file__), "pricing_model.bin"
        )
        self._kwargs = kwargs

        if self.model_type == "XGBoost":
            self._model = xgb.XGBRegressor(
                objective="reg:squarederror",
                n_estimators=200,
                max_depth=6,
                learning_rate=0.05,
                subsample=0.8,
                colsample_bytree=0.8,
                random_state=self._kwargs.get("random_state", 42),
            )
        elif self.model_type == "LSTM":
            self._model = LSTMRegressor(
                hidden_layer_sizes=(64, 32), random_state=self._kwargs.get("random_state", 42),
                max_iter=self._kwargs.get("max_iter", 500), lookback=self._kwargs.get("lookback", 3)
            )
        else:
            raise ValueError("Unsupported model_type: {}".format(self.model_type))

    def train(self, X: np.ndarray, y: np.ndarray) -> None:
        """Train the underlying model on provided data.

        Parameters
        - X: 2D array-like of shape (n_samples, n_features)
        - y: 1D array-like of shape (n_samples,)
        """
        if self.model_type == "XGBoost":
            self._model.fit(X, y)
        else:
            self._model.fit(X, y)

    def predict(self, X: np.ndarray) -> np.ndarray:
        if self._model is None:
            raise ValueError("Model has not been fitted yet.")
        return self._model.predict(X)

    def save(self, path: Optional[str] = None) -> None:
        path = path or self.model_path
        with open(path, "wb") as f:
            pickle.dump(self._model, f)

    def load(self, path: Optional[str] = None) -> None:
        path = path or self.model_path
        with open(path, "rb") as f:
            self._model = pickle.load(f)  # type: ignore

    def is_trained(self) -> bool:
        return self._model is not None

    # --------------------- Convenience API ---------------------
    def __repr__(self) -> str:  # pragma: no cover
        return f"PricingModelTrainer(model_type={self.model_type}, trained={self.is_trained()})"

    # Exposed for dynamic price usage
    def estimate_price_from_features(self, features_vector: np.ndarray) -> float:
        pred = self.predict(features_vector.reshape(1, -1))
        return float(pred[0])

    # --------------------- Persistence helpers ---------------------
    @staticmethod
    def _default_base_price(base_id: str) -> float:
        # A tiny in-memory base price map. In production this should pull from a
        # config service or database.
        base_prices = {
            "base_A": 100.0,
            "base_B": 90.0,
            "default": 80.0,
        }
        return base_prices.get(base_id, base_prices["default"])


def _get_base_price(base_id: str) -> float:
    return PricingModelTrainer._default_base_price(base_id)


def _clamp(value: float, min_value: float, max_value: float) -> float:
    return max(min(value, max_value), min_value)


def _to_vector(features: Dict[str, float]) -> np.ndarray:
    # Fixed order of features as expected by training data
    order = [
        "tourist_heat_index",
        "wind_speed",
        "rainfall",
        "visibility",
        "temperature",
        "hour_of_day",
        "day_of_week",
        "is_holiday",
        "historical_rental_count",
        "time_to_next_peak",
        "weather_score",
        "season_index",
    ]
    vec = [float(features.get(k, 0.0)) for k in order]
    return np.asarray(vec, dtype=float).reshape(1, -1)


def dynamic_price_adjustment(base_price: float, demand_multiplier: float, cost_price: float) -> float:
    """Apply dynamic pricing with bounds.

    - min price is cost_price * 1.2
    - max price is base_price * 3.0
    Returns the price clamped to the [min_price, max_price] interval.
    """
    raw = base_price * demand_multiplier
    min_price = cost_price * 1.2
    max_price = base_price * 3.0
    return _clamp(raw, min_price, max_price)


def predict_price(features: Dict[str, float], base_id: str, timestamp: datetime) -> float:
    """Dynamic pricing entry-point.

    This function loads a trained pricing model if available and uses it to
    estimate a demand-driven price. If no model is available, it falls back to a
    simple base price heuristic.

    Parameters
    - features: raw feature dictionary (expects the engineered features as keys)
    - base_id: identifier for which base price should be used
    - timestamp: current timestamp used for time-based features

    Returns
    - A single float price, already bounded by min/max rules.

    REST API example (JSON):
    {
      "base_id": "base_A",
      "timestamp": "2026-05-02T12:00:00Z",
      "features": {
        "temperature": 22.5,
        "wind_speed": 4.1,
        ...
      }
    }
    """
    # First compute engineered features from the raw input using the feature
    # engineering helper, so callers can pass raw sensor-like values.
    engineered = FeatureEngineering.compute_features(features, timestamp)
    vec = _to_vector(engineered)

    model_path = os.path.join(os.path.dirname(__file__), "pricing_model.bin")
    if os.path.exists(model_path):
        trainer = PricingModelTrainer(model_type="XGBoost", model_path=model_path)
        try:
            trainer.load(model_path)
            predicted = trainer.predict(vec)[0]
        except Exception:
            predicted = _get_base_price(base_id)
    else:
        predicted = _get_base_price(base_id)

    base_price = _get_base_price(base_id)
    cost_price = base_price * 0.7  # assumed cost factor for lower bound
    return dynamic_price_adjustment(base_price, float(predicted) / base_price, cost_price)


def save_model(obj: PricingModelTrainer, path: Optional[str] = None) -> None:
    """Helper to save a trained model to disk."""
    obj.save(path)


def load_model(path: Optional[str] = None) -> PricingModelTrainer:
    """Helper to load a trained model from disk.

    Returns a PricingModelTrainer instance with the loaded model.
    """
    trainer = PricingModelTrainer(model_type="XGBoost")
    trainer.load(path)
    return trainer


__all__ = [
    "FeatureEngineering",
    "LSTMRegressor",
    "PricingModelTrainer",
    "predict_price",
    "dynamic_price_adjustment",
    "save_model",
    "load_model",
]


# ---------------------------------------------------------------------------
"""REST API example (JSON format) for pricing.

Example input (POST /pricing/predict):
{
  "base_id": "base_A",
  "timestamp": "2026-05-02T12:00:00Z",
  "features": {
    "temperature": 24.0,
    "wind_speed": 3.5,
    "rainfall": 0.0,
    "visibility": 9.8,
    "hour_of_day": 12,
    "day_of_week": 0,
    "is_holiday": false,
    "historical_rental_count": 150,
    "time_to_next_peak": 2.0,
    "weather_score": 0.85,
    "season_index": 2
  }
}

Expected output:
{
  "price": 106.3
}
"""
