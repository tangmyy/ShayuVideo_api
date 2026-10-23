# ShayuVideo 后端配置与启动

后端采用 Java 8、Spring Boot 2.5.4 和 Maven 父子聚合工程。

## 本地配置

在后端根目录执行：

```powershell
Copy-Item .env.example .env
```

填写 `.env`。该文件被 Git 忽略，禁止提交；`.env.example` 是不含真实凭据的公开模板。已有本地 `.env` 时不要覆盖。

`.env` 使用每行 `KEY=VALUE` 格式，不加引号、不写行尾注释、不进行转义，密码中的 `$`、`#`、`=` 按原样填写。变量由 `with-env.ps1` 按文本读取，不会作为 PowerShell 代码执行。MongoDB URI 中的用户名、密码如果包含 URI 特殊字符，需要进行百分号编码。

| 变量 | 说明 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` 或 `prod`，决定使用哪组配置 |
| `DEV_*` / `PROD_*` | 对应开发 / 生产环境；只需准备要运行环境的一组完整配置 |
| `*_SERVER_PORT` | 后端 HTTP 端口 |
| `*_DB_URL` / `*_DB_USERNAME` / `*_DB_PASSWORD` | MySQL JDBC 地址、账号和密码 |
| `*_REDIS_HOST` / `*_REDIS_PORT` / `*_REDIS_DATABASE` / `*_REDIS_PASSWORD` | Redis 地址、端口、库编号和密码 |
| `*_MONGODB_URI` / `*_MONGODB_DATABASE` | MongoDB 连接串（包含认证信息）和数据库名 |
| `*_RABBITMQ_HOST` / `*_RABBITMQ_PORT` / `*_RABBITMQ_USERNAME` / `*_RABBITMQ_PASSWORD` / `*_RABBITMQ_VIRTUAL_HOST` | RabbitMQ 连接配置 |
| `*_MINIO_ENDPOINT` / `*_MINIO_FILE_HOST` / `*_MINIO_BUCKET` | MinIO 接口地址、文件访问地址和桶名 |
| `*_MINIO_ACCESS_KEY` / `*_MINIO_SECRET_KEY` | MinIO 访问凭据 |
| `NACOS_CONFIG_ADDR` | Nacos 配置中心地址，bootstrap 阶段使用 |
| `*_NACOS_DISCOVERY_ADDR` | 对应环境的 Nacos 注册地址 |
| `TENCENT_SECRET_ID` / `TENCENT_SECRET_KEY` | 腾讯云短信访问密钥 |
| `GENERATOR_DB_URL` / `GENERATOR_DB_USERNAME` / `GENERATOR_DB_PASSWORD` | 代码生成器使用的独立数据库连接配置 |

表中 `*` 代表 `DEV` 或 `PROD`。完整变量清单见 `.env.example`。数据库需先初始化，MongoDB 用户认证库、RabbitMQ virtual host 权限、MinIO 桶及腾讯云短信服务需按实际环境准备；本次没有将这些外部依赖改成可选服务。

## 构建与启动（PowerShell）

```powershell
# 在后端根目录构建可执行 JAR
mvn -pl shayu-api -am package -DskipTests

# 加载 .env 后启动；当前项目版本为 v2.5.0
.\with-env.ps1 java -jar .\shayu-api\target\shayu-api_v2.5.0.jar
```

脚本会覆盖子进程中的同名变量，结束后恢复调用环境。脚本不输出配置值。

Spring Boot 不会自动读取此项目的 `.env`。直接从 IDEA 启动时，在运行配置中设置相同环境变量；不要把带真实值的运行配置共享到 Git。Nacos 的凭据和外部配置内容也需要单独管理，本地脱敏不会清理配置中心。

## 代码生成器

`generatorConfig.xml` 通过 MyBatis Generator 的配置变量机制引用 `GENERATOR_DB_*`，`GeneratorDisplay` 已将进程环境变量传入解析器。

从 IDEA 执行生成器时，设置这三个环境变量，并将工作目录设为后端根目录；也可在当前 PowerShell 中执行 `. .\with-env.ps1` 加载变量后，从该终端启动生成器所需的 Java 命令。生成器会写入模型和 Mapper 文件，不要将生成操作作为普通启动检查。

## 重置凭据后

先在数据库、中间件或云平台轮换凭据，再修改本地 `.env` 对应值并重启应用。开发、生产和生成器若复用同一账号，需同步更新所有引用；腾讯云替换成功后禁用旧密钥。

提交前确认 `git check-ignore .env` 命中规则，且 `git ls-files .env` 无输出。公开 YAML、properties、XML 中只保留变量引用。旧提交中已有的凭据不会因本次修改而消失，仍需轮换及另行处理历史。
