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

export interface ApiError {
    success: false;
    message: string;
    error: string | null;
    errors: Record<string, string> | null;
}

function handleError(error: unknown): ApiError {
    let message = "An error occurred";
    let errorDescription: string | null = null;
    let errors: Record<string, string> | null = null;
    if (axios.isAxiosError(error)) {
        message = (error.response?.data?.message) ?? message;
        errorDescription = (error.response?.data?.error) ?? null;
        errors = (error.response?.data?.errors) ?? null;
        // If backend provided detailed validation errors, append them to the message for convenience
        if (errors && Object.keys(errors).length > 0) {
            const details = Object.entries(errors).map(([k, v]) => `${k}: ${v}`).join(", ");
            message = `${message}${details ? `: ${details}` : ''}`;
        }
    } else if (error instanceof Error) {
        message = error.message;
    }
    return {
        success: false,
        message,
        error: errorDescription,
        errors,
    };
}

export type ApiResult<T> = T | ApiError;

export async function get<T = unknown>(url: string, params?: Record<string, unknown>): Promise<ApiResult<T>> {
    try {
        const response = await apiCall.get<T>(url, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}

export async function post<T = unknown>(url: string, data: unknown, params?: Record<string, unknown>): Promise<ApiResult<T>> {
    try {
        const response = await apiCall.post<T>(url, data, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}

export async function put<T = unknown>(url: string, data: unknown, params?: Record<string, unknown>): Promise<ApiResult<T>> {
    try {
        const response = await apiCall.put<T>(url, data, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}

export async function del<T = unknown>(url: string, params?: Record<string, unknown>): Promise<ApiResult<T>> {
    try {
        const response = await apiCall.delete<T>(url, {params});
        return response.data;
    } catch (error) {
        return handleError(error);
    }
}