import apiClient from './apiClient';

export const sendRequest = async (nickname) => {
    const response = await apiClient.post(`/friends/requests/${nickname}`);
    return response.data;
};

export const acceptRequest = async (nickname) => {
    const response = await apiClient.post(`/friends/requests/${nickname}/accept`);
    return response.data;
};

export const rejectRequest = async (nickname) => {
    const response = await apiClient.post(`/friends/requests/${nickname}/reject`);
    return response.data;
};

export const getFriends = async (pageable) => {
    const response = await apiClient.get('/friends/list', {
        params: {
            page: pageable.page,
            size: pageable.size,
            sort: pageable.sort
        }
    });
    return response.data;
};

export const getReceivedRequests = async (pageable) => {
    const response = await apiClient.get('/friends/requests/received', {
        params: {
            page: pageable.page,
            size: pageable.size,
            sort: pageable.sort
        }
    });
    return response.data;
};

export const searchUsers = async (query, pageable) => {
    const response = await apiClient.get('/friends/search', {
        params: {
            query: query,
            page: pageable.page,
            size: pageable.size,
            sort: pageable.sort
        }
    });
    return response.data;
};