import axios from 'axios';

const apiCall = axios.create({
    baseURL: 'http://localhost:8080',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

apiCall.interceptors.request.use((config) => {
    const username = "medilabo";
    const password = "medilabo123";
    const auth = btoa(`${username}:${password}`);
    config.headers.Authorization = `Basic ${auth}`;
    return config;
});

function handleError(error: unknown) {
    let message = "An error occurred";
    let errorDescription = null;
    if (axios.isAxiosError(error)) {
        message = (error.response?.data?.message) ?? message;
        errorDescription = (error.response?.data?.error) ?? errorDescription;
    } else if (error instanceof Error) {
        message = error.message;
    }
    return {
        success: false,
        message,
        error: errorDescription,
    }
}

export async function get(url: string, params?: object) {
    try {
        const response = await apiCall.get(url, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}

export async function post(url: string, data: object, params?: object) {
    try {
        const response = await apiCall.post(url, data, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}

export async function put(url: string, data: object, params?: object) {
    try {
        const response = await apiCall.put(url, data, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}

export async function del(url:string, params?: object) {
    try {
        const response = await apiCall.delete(url, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}