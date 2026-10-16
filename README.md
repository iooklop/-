# 资源共享平台

一个参考 **CSDN 运营模式**的资源分享网站：用户上传资源 → 管理员审核 → 用户检索 / 查看 / 下载，并支持 **点赞、收藏、评论、打赏（微信支付）** 等互动功能。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17 · Spring Boot 4.0.6 · Spring Data JPA · Spring Security + JWT · MySQL · Lombok · springdoc-openapi |
| 前端 | Vue 3 · Vite · TypeScript · Pinia · Vue Router · Axios · lucide-vue-next · SCSS |

## 目录结构

```
resource-share/
├── backend/            # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/resourceshare/
│       │   ├── controller/    # 接口层
│       │   ├── service/       # 业务层
│       │   ├── repository/    # 数据访问层
│       │   ├── entity/        # 实体
│       │   ├── dto/           # 请求对象
│       │   ├── config/        # 安全/初始化配置
│       │   ├── common/        # Result 等通用类
│       │   ├── exception/     # 异常处理
│       │   └── util/          # JWT / 文件存储工具
│       └── resources/
│           ├── application.yml
│           └── schema.sql     # 建表脚本（启动自动执行）
├── frontend/           # Vue 3 前端
│   └── src/
│       ├── pages/      # 前台 + 后台页面
│       ├── components/ # 公共组件
│       ├── store/      # Pinia 状态
│       ├── utils/      # axios / storage / format
│       └── style/      # 全局样式与变量
└── docker-compose.yml  # MySQL 8 容器
```

## 快速开始

### 1. 启动 MySQL

```bash
# 方式一：Docker（推荐）
docker compose up -d

# 方式二：本机已安装 MySQL 8，创建 root/123456 账号即可
```

> 数据库 `resource_share` 由后端连接串 `createDatabaseIfNotExist=true` 自动创建；表结构由 `schema.sql` 自动执行。首次启动会自动写入演示数据（见「默认账号」）。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行于 `http://localhost:9090`，接口文档（Swagger）见 `http://localhost:9090/swagger-ui.html`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行于 `http://localhost:3000`，开发环境已配置代理将 `/api`、`/uploads` 转发到后端。

> **启动前必读（最常见报错）**：后端启动时若 MySQL 未就绪，会因无法连接 `localhost:3306` 直接退出（`HikariPool ... Connection refused`）。请先确认数据库可用：
> - 用 Docker 时，先启动 **Docker Desktop** 再执行 `docker compose up -d`；
> - 或本机已装 MySQL 8，确认有 `root/123456` 账号（端口 3306）。
>
> 可执行 `docker ps` 或 `netstat -ano | findstr 3306` 快速判断 3306 是否在监听。

## 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin` |
| 普通用户 | `user` | `123456` |

> 初始数据包含 12 个资源分类、4 条已通过示例资源、1 条待审核资源。示例资源仅含元数据（无实际文件），点击下载会提示「暂无可下载文件」，可自行上传带文件的资源验证下载流程。

## 核心功能

- **资源检索**：关键词搜索 + 分类筛选 + 排序（最新 / 下载 / 浏览 / 点赞）
- **上传审核**：用户上传封面与文件 → 状态为「待审核」→ 管理员通过 / 驳回（附原因）
- **互动**：点赞、收藏、评论（可删除自己的评论）
- **打赏**：微信支付 Native 扫码打赏（默认模拟模式）
- **个人中心**：资料修改、密码修改、我的上传 / 下载 / 收藏
- **管理后台**：数据概览、资源审核、用户管理（禁用/启用）、分类管理

## 微信支付配置

打赏默认走**模拟模式**（`app.wechat.enabled: false`），前端展示「模拟支付成功」按钮，便于本地联调。

接入真实微信支付时，修改 `backend/src/main/resources/application.yml`：

```yaml
app:
  wechat:
    enabled: true
    appid:            # 公众号/小程序 AppID
    mchid:            # 商户号
    serial-no:        # 商户 API 证书序列号
    private-key-path: # 商户私钥 apiclient_key.pem 路径
    api-v3-key:       # API v3 密钥（32 位）
    notify-url:       # 支付回调地址，需公网可达
```

切换为真实模式后，前端会自动展示微信支付二维码，支付结果由 `/api/pay/wechat/notify` 回调落库。

## 主要接口约定

- 统一返回体：`{ code: "200" | "500", msg, data }`
- 分页返回 Spring Data `Page`：`{ content, totalElements, totalPages, number }`
- 鉴权：请求头 `Authorization: Bearer <token>`，登录返回 `{ token, role, user }`
