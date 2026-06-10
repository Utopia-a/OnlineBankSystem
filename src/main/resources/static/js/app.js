/**
 * app.js - 前端公共工具函数
 * Member A: 前端 & 集成
 */

/**
 * 格式化金额，保留两位小数，千分位分隔
 * @param {number|string} amount
 * @returns {string}
 */
function formatAmount(amount) {
    if (amount == null) return '0.00';
    return parseFloat(amount).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

/**
 * 账户号脱敏：显示前4位和后4位，中间用 **** 代替
 * @param {string} accountNo
 * @returns {string}
 */
function maskAccountNo(accountNo) {
    if (!accountNo || accountNo.length < 8) return accountNo;
    return accountNo.slice(0, 4) + ' **** **** ' + accountNo.slice(-4);
}

/**
 * 格式化日期时间
 * @param {string} datetime - ISO 格式或 yyyy-MM-dd HH:mm:ss
 * @returns {string}
 */
function formatDate(datetime) {
    if (!datetime) return '-';
    try {
        const d = new Date(datetime);
        if (isNaN(d)) return datetime;
        const pad = n => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} `
             + `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    } catch (e) {
        return datetime;
    }
}

/**
 * 交易类型中文映射
 */
function formatTxnType(type) {
    const map = {
        'TRANSFER': '转账',
        'DEPOSIT':  '存款',
        'WITHDRAW': '取款'
    };
    return map[type] || type;
}

/**
 * 交易状态中文映射
 */
function formatStatus(status) {
    const map = {
        'SUCCESS': '成功',
        'FAILED':  '失败',
        'PENDING': '处理中',
        'CANCELLED': '已撤销',
        'ROLLED_BACK': '已回滚'
    };
    return map[status] || status;
}

/**
 * 防抖函数
 * @param {Function} fn
 * @param {number} delay
 * @returns {Function}
 */
function debounce(fn, delay = 300) {
    let timer;
    return function (...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
}

/**
 * 简单的 Toast 提示
 * @param {string} msg
 * @param {'success'|'error'|'info'} type
 */
function toast(msg, type = 'info') {
    const el = document.createElement('div');
    el.textContent = msg;
    el.style.cssText = `
        position: fixed; top: 80px; left: 50%; transform: translateX(-50%);
        background: ${type === 'error' ? '#c62828' : type === 'success' ? '#2e7d32' : '#1a3a5c'};
        color: #fff; padding: 12px 24px; border-radius: 8px;
        font-size: 14px; z-index: 9999; box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        animation: fadeIn 0.2s ease;
    `;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 3000);
}
