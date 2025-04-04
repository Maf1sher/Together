import apiClient from './apiClient';

export const login = async (credentials) => {
    const response = await apiClient.post('/auth/login', credentials);
    return response.data; // Backend powinien ustawić JWT w cookie HTTP-only
};

export const register = async (userData) => {
    const response = await apiClient.post('/auth/register', userData);
    return response.data;
};

export const logout = async () => {
    const response = await apiClient.post('/auth/logout');
    return response.data;
};