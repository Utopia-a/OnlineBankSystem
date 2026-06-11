# 前端（Front）

Thymeleaf 页面与静态资源，由后端在构建时打包进 classpath。

## 目录

```
front/
├── templates/     # HTML 页面（login、dashboard 等）
└── static/
    ├── css/       # 样式
    └── js/        # 脚本
```

## 开发说明

- 修改本目录文件后，在 `project/back` 下执行 `mvn compile` 或重启应用使变更生效
- 静态资源（CSS/JS）开发时也可通过 `file:../front/static/` 热加载
