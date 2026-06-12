/**
 * admin-api.js - 管理后台 API 封装
 */

async function adminFetch(url, options = {}) {
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
        response = await fetch(url, { ...options, headers });
    } catch (e) {
        throw new Error('网络连接失败');
    }

    if (response.status === 401) {
        localStorage.clear();
        window.location.href = '/login?reason=expired';
        throw new Error('登录已过期');
    }
    if (response.status === 403) {
        throw new Error('无管理员权限');
    }

    const data = await response.json();
    if (data.code !== 200) {
        throw new Error(data.message || '操作失败');
    }
    return data.data;
}

async function adminGet(url) {
    return adminFetch(url, { method: 'GET' });
}

async function adminPost(url, body) {
    return adminFetch(url, {
        method: 'POST',
        body: body != null ? JSON.stringify(body) : undefined
    });
}

async function adminPut(url, body) {
    return adminFetch(url, {
        method: 'PUT',
        body: body != null ? JSON.stringify(body) : undefined
    });
}

async function adminDelete(url) {
    return adminFetch(url, { method: 'DELETE' });
}

async function adminPatch(url, body) {
    return adminFetch(url, {
        method: 'PATCH',
        body: body != null ? JSON.stringify(body) : undefined
    });
}

function normalizePageResult(data) {
    if (!data) return { records: [], total: 0, page: 1, size: 10, totalPages: 0 };
    if (data.records) return data;
    if (data.content) {
        return {
            records: data.content,
            total: data.totalElements,
            page: data.number + 1,
            size: data.size,
            totalPages: data.totalPages
        };
    }
    return data;
}

function statusLabel(status) {
    const map = {
        ACTIVE: '正常', FROZEN: '冻结', LOCKED: '冻结',
        DISABLED: '禁用', PENDING_VERIFY: '待验证',
        SUCCESS: '成功', FAILED: '失败', PENDING: '处理中', CANCELLED: '已撤销'
    };
    return map[status] || status;
}

function roleLabel(role) {
    return role === 'ADMIN' || role === 'ROLE_ADMIN' ? '管理员' : '普通用户';
}

function buildQuery(params) {
    const q = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
        if (v !== null && v !== undefined && v !== '') q.append(k, v);
    });
    return q.toString();
}

function requireAdmin() {
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (!localStorage.getItem('accessToken')) {
        window.location.href = '/login';
        return false;
    }
    if (info.role !== 'ROLE_ADMIN') {
        window.location.href = '/dashboard';
        return false;
    }
    return true;
}

async function adminLogout() {
    const token = localStorage.getItem('accessToken');
    if (token) {
        try {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token }
            });
        } catch (e) { /* ignore */ }
    }
    localStorage.clear();
    window.location.href = '/login';
}

function initAdminNav() {
    if (!requireAdmin()) return;
    const info = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const el = document.getElementById('adminUserName');
    if (el) el.textContent = info.realName || info.username || '管理员';
    const path = window.location.pathname;
    document.querySelectorAll('.admin-nav-link').forEach(link => {
        link.classList.toggle('active', link.getAttribute('data-path') === path);
    });
}

function renderPagination(containerId, pageData, onPage) {
    const el = document.getElementById(containerId);
    if (!el) return;
    const totalPages = pageData.totalPages || 1;
    const page = pageData.page || 1;
    el.innerHTML = `
        <button class="btn btn-sm" ${page <= 1 ? 'disabled' : ''} onclick="(${onPage.name})(${page - 1})">上一页</button>
        <span class="page-info">第 ${page} / ${totalPages} 页，共 ${pageData.total || 0} 条</span>
        <button class="btn btn-sm" ${page >= totalPages ? 'disabled' : ''} onclick="(${onPage.name})(${page + 1})">下一页</button>
    `;
}
