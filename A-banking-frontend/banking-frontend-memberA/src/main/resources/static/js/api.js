/**
 * api.js - 前端 HTTP 请求工具函数
 * Member A: 前端 & 集成
 *
 * 封装 fetch API，自动处理：
 * - JWT Token 注入
 * - 统一错误处理
 * - 401 自动跳转登录
 */

/**
 * 通用 GET 请求
 * @param {string} url
 * @returns {Promise<any>} 解包后的 data 字段
 */
async function apiGet(url) {
    return apiFetch(url, { method: 'GET' });
}

/**
 * 通用 POST 请求
 * @param {string} url
 * @param {object|null} body
 * @returns {Promise<any>}
 */
async function apiPost(url, body) {
    return apiFetch(url, {
        method: 'POST',
        body: body != null ? JSON.stringify(body) : undefined
    });
}

/**
 * 通用 PUT 请求
 */
async function apiPut(url, body) {
    return apiFetch(url, {
        method: 'PUT',
        body: body != null ? JSON.stringify(body) : undefined
    });
}

/**
 * 通用 DELETE 请求
 */
async function apiDelete(url) {
    return apiFetch(url, { method: 'DELETE' });
}

/**
 * 核心请求函数
 */
async function apiFetch(url, options = {}) {
    const token = localStorage.getItem('accessToken');

    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    let response;
    try {
        response = await fetch(url, {
            ...options,
            headers
        });
    } catch (networkError) {
        throw new Error('网络连接失败，请检查网络后重试');
    }

    // 处理 401 - 未登录或 Token 过期
    if (response.status === 401) {
        localStorage.clear();
        window.location.href = '/login?reason=expired';
        throw new Error('登录已过期，请重新登录');
    }

    let data;
    try {
        data = await response.json();
    } catch (e) {
        throw new Error('服务器返回格式异常');
    }

    // 业务错误
    if (data.code !== 200) {
        const err = new Error(data.message || '操作失败');
        err.code = data.code;
        throw err;
    }

    return data.data;
}
