import apiClient from './apiClient';

export const login = async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data;
};

export const register = async (userData) => {
    const response = await apiClient.post('/auth/register', userData);
    return response.data;
};

export const logout = async () => {
    const response = await apiClient.post('/auth/logout');
    return response.data;
};

export const checkAuthStatus = async () => {
    const response = await apiClient.get('/auth/check');
    return response.data;
}