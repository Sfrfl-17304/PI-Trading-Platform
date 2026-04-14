import axios from 'axios';

const api_user = axios.create({
  baseURL: 'http://localhost:8081',
  headers: {
    'Content-Type': 'application/json',
  }
});

export default api_user;
