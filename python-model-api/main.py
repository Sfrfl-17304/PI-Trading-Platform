from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import warnings
import statsmodels.api as sm

# Ignore statsmodels convergence warnings for lightweight brevity
warnings.filterwarnings("ignore")

app = FastAPI(title="Trading Prediction Model API")

class PredictRequest(BaseModel):
    symbol: str
    prices: List[float]

@app.post("/predict")
def predict_price(request: PredictRequest):
    prices = request.prices
    
    # We require a small amount of data points to model an ARIMA trend
    if len(prices) < 5:
        return {"action": "HOLD", "confidence": 0.0}

    try:
        # A simple ARIMA model (AutoRegressive Integrated Moving Average)
        # Assuming order (1, 1, 0) for a light random-walk based time series trend
        model = sm.tsa.ARIMA(prices, order=(1, 1, 0))
        fitted_model = model.fit()

        # Forecast exactly 1 step into the future
        forecast = fitted_model.forecast(steps=1)
        predicted_price = forecast.iloc[0] if hasattr(forecast, "iloc") else forecast[0]
        
        last_price = prices[-1]
        
        # Calculate expected percentage change
        pct_change = (predicted_price - last_price) / last_price
        
        # Define a threshold for Buy/Sell signals (e.g., 0.1% forecasted change)
        threshold = 0.001 
        
        confidence = min(abs(pct_change) * 100, 1.0) 
        
        if pct_change > threshold:
            action = "BUY"
        elif pct_change < -threshold:
            action = "SELL"
        else:
            action = "HOLD"
            confidence = 1.0 - (abs(pct_change) / threshold)
            
        return {
            "action": action, 
            "confidence": round(float(confidence), 2)
        }
        
    except Exception as e:
        print(f"Prediction Error for {request.symbol}: {e}")
        return {"action": "HOLD", "confidence": 0.0}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
