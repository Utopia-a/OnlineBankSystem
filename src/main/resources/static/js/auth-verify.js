/**
 * 邮箱验证相关 API（注册后 / 登录前）
 */

const EMAIL_VERIFY_TYPE = 'EMAIL_VERIFY';
let resendTimer = null;

async function submitEmailVerify(target, code) {
    return apiPost('/api/auth/verify-email', {
        target,
        code,
        type: EMAIL_VERIFY_TYPE
    });
}

async function submitResendOtp(target) {
    return apiPost('/api/auth/resend-otp', {
        target,
        type: EMAIL_VERIFY_TYPE
    });
}

function startResendCooldown(button, seconds) {
    if (resendTimer) clearInterval(resendTimer);
    let remaining = seconds;
    button.disabled = true;
    button.textContent = remaining + 's 后重发';
    resendTimer = setInterval(() => {
        remaining--;
        if (remaining <= 0) {
            clearInterval(resendTimer);
            resendTimer = null;
            button.disabled = false;
            button.textContent = '重新发送';
        } else {
            button.textContent = remaining + 's 后重发';
        }
    }, 1000);
}
