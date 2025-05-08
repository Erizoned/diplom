import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class AxiosService {

  constructor(private router: Router) {
    axios.defaults.baseURL = "http://localhost:8081";
    axios.interceptors.response.use(
      response => response,
      error => {
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
          localStorage.removeItem('auth_token');
          this.router.navigate(['/login']);
          console.log("Пользователь не авторизирован")
        }
        return Promise.reject(error);
      }
    )
  }

  getAuthToken(): string | null {
    return window.localStorage.getItem("auth_token");
  }

  setAuthToken(token: string | null): void {
    if (token !== null){

      window.localStorage.setItem("auth_token", token);
    } else {
      window.localStorage.removeItem("auth_token");
    }
  }

  request(method: string, url: string, data: any): Promise<any> {
    let headers: any = {};
    const token = this.getAuthToken();
    if (token && token.split('.').length === 3 && url !== '/login' && url !== '/register') {
      headers = { "Authorization": "Bearer " + token };
    }
    
    if (data instanceof FormData) {
      headers['Content-Type'] = 'multipart/form-data';
    } else {
      headers['Content-Type'] = 'application/json';
    }
    
    return axios({
      method: method,
      url: url,
      data: data,
      headers: headers,
    });
  }
}
