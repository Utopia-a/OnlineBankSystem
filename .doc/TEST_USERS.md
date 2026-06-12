# 测试用户说明

> 数据脚本：`project/back/sql/seed-test-data.sql`  
> 导入命令：`mysql -uroot -p banking_db < project/back/sql/seed-test-data.sql`

## 管理员（schema 自带，不在种子脚本中重复插入）

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | Admin@123 | 管理员 | 后台管理、用户监控 |

## 普通测试用户

| 用户名 | 密码 | 真实姓名 | 邮箱 | 手机号 | 状态 | 用途 |
|--------|------|----------|------|--------|------|------|
| zhangsan | Password1 | 张三 | zhangsan@test.com | 13800000001 | ACTIVE | 主测试账号，登录/转账/查账单 |
| lisi | Password2 | 李四 | lisi@test.com | 13800000002 | ACTIVE | 作为收款方测试转账 |
| wangwu | Password3 | 王五 | wangwu@test.com | 13800000003 | ACTIVE | 低余额，测试余额不足 |
| zhaoliu | Password4 | 赵六 | zhaoliu@test.com | 13800000004 | PENDING_VERIFY | 未验证邮箱，登录应被拒绝 |
| sunqi | Password5 | 孙七 | sunqi@test.com | 13800000005 | LOCKED | 账户锁定，登录应被拒绝 |
| disabled_user | Password6 | 周八 | disabled@test.com | 13800000006 | DISABLED | 已禁用，登录应被拒绝 |

> 密码按用户顺序依次为 **Password1** ~ **Password6**（至少 8 位，含大小写字母和数字）。

## 测试账户

| 账户号 | 所属用户 | 类型 | 余额（元） | 说明 |
|--------|----------|------|------------|------|
| ACC202606120000001 | zhangsan | CHECKING（活期） | 10,100.00 | 张三主账户，付款账户 |
| ACC202606120000002 | zhangsan | SAVINGS（储蓄） | 5,000.00 | 张三第二账户 |
| ACC202606120000003 | lisi | CHECKING（活期） | 8,000.00 | 李四账户，常用收款方 |
| ACC202606120000004 | wangwu | CHECKING（活期） | 100.00 | 王五账户，余额较低 |
| ACC202606120000005 | sunqi | CHECKING（活期） | 1,000.00 | 孙七账户（用户本身锁定） |

## 推荐测试场景

| 场景 | 操作 |
|------|------|
| 正常登录 | zhangsan / Password1 |
| 正常转账 | zhangsan 从 ACC...001 向 ACC...003 转 1000 元 |
| 余额不足 | wangwu / Password3 账户取款或转出超过 100 元 |
| 自转账拒绝 | 同一 ACC 账户互转 |
| 流水号误填 | 收款方填 TXN 开头应提示格式错误 |
| 交易记录 | zhangsan 登录后查看 ACC...001 历史（含 2 条样例） |
| 管理员 | admin / Admin@123 进入后台 |

## 注意事项

- 脚本可重复执行，会先删除上述测试用户及其账户、令牌、交易再重建。
- **不会删除** `admin` 及非列表中的其他用户。
- 密码须满足系统规则：至少 8 位，含大小写字母和数字（Password1 ~ Password6 均符合）。
