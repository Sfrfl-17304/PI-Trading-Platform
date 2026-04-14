import axios from 'axios';

const api_market = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  }
});

export default api_market;
