# Nền tảng học tập

Ứng dụng quản lý học tập full-stack gồm Spring Boot 3, React/Vite, PostgreSQL, Redis, JWT, Flyway, Docker Compose, Nginx và tích hợp DeepSeek API.

## Tài khoản khởi tạo

Hệ thống chỉ seed một tài khoản quản trị:

- Email: `admin@education.com`
- Mật khẩu ban đầu: giá trị `ADMIN_INITIAL_PASSWORD` trong `.env`

Mật khẩu seed chỉ được mã hóa khi database mới khởi tạo lần đầu. Sau khi tài khoản đã có mật khẩu BCrypt, đổi `ADMIN_INITIAL_PASSWORD` sẽ không reset mật khẩu hiện tại.

## Chạy production trên EC2

Yêu cầu EC2:

- Ubuntu 22.04/24.04 hoặc Amazon Linux 2023.
- Docker và Docker Compose plugin.
- Security Group chỉ cần mở `22` cho SSH và `80` cho HTTP. Không mở `5432`, `6379`, `8080`.

Triển khai:

```bash
git clone <repo-url> edu-platform
cd edu-platform
cp .env.example .env
nano .env
docker compose up -d --build
```

Các biến bắt buộc cần đổi trong `.env` trước khi chạy:

```bash
POSTGRES_PASSWORD=<mat-khau-db-manh>
JWT_SECRET=<chuoi-bi-mat-it-nhat-48-ky-tu-ngau-nhien>
ADMIN_INITIAL_PASSWORD=<mat-khau-admin-ban-dau>
CORS_ORIGINS=http://<EC2_PUBLIC_IP>,https://<DOMAIN_NEU_CO>
STORAGE_PROVIDER=r2
CLOUDFLARE_R2_ACCOUNT_ID=<account-id>
CLOUDFLARE_R2_ACCESS_KEY=<r2-access-key>
CLOUDFLARE_R2_SECRET_KEY=<r2-secret-key>
CLOUDFLARE_R2_BUCKET_NAME=<bucket-name>
CLOUDFLARE_R2_PUBLIC_URL=https://<public-r2-domain>
```

Truy cập:

- Web: `http://<EC2_PUBLIC_IP>` hoặc domain trỏ về EC2.
- API qua Nginx: `http://<EC2_PUBLIC_IP>/api/v1`.
- Swagger: `http://<EC2_PUBLIC_IP>/swagger-ui.html`.

## Lệnh vận hành

```bash
docker compose ps
docker compose logs -f backend
docker compose logs -f nginx
docker compose pull
docker compose up -d --build
docker compose down
```

## Lưu trữ video bằng Cloudflare R2

Ứng dụng dùng R2 Object Storage qua API S3-compatible. File video/hình ảnh được upload vào bucket R2, database chỉ lưu object key dạng `r2://<bucket>/media/<user-id>/<uuid>-<filename>`. Frontend vẫn xem file qua endpoint backend `/api/v1/media/stream/{id}` nên bucket không cần public.

Thiết lập trên Cloudflare:

1. Tạo R2 bucket.
2. Tạo R2 API Token có quyền đọc/ghi object cho bucket đó.
3. Lấy Account ID. Backend sẽ tự tạo endpoint `https://<account-id>.r2.cloudflarestorage.com`.
4. Điền các biến `CLOUDFLARE_R2_*` trong `.env`.

Cấu hình tương ứng trong Spring Boot:

```yaml
cloudflare:
  r2:
    account-id: ${CLOUDFLARE_R2_ACCOUNT_ID}
    access-key: ${CLOUDFLARE_R2_ACCESS_KEY}
    secret-key: ${CLOUDFLARE_R2_SECRET_KEY}
    bucket-name: ${CLOUDFLARE_R2_BUCKET_NAME}
    public-url: ${CLOUDFLARE_R2_PUBLIC_URL}
```

Nếu muốn chạy local không dùng R2, đặt:

```bash
STORAGE_PROVIDER=local
UPLOAD_DIR=/app/uploads
```

Khi `STORAGE_PROVIDER=local`, file được lưu trong Docker volume `uploads` tại `/app/uploads`.

Backup database:

```bash
docker compose exec postgres pg_dump -U education education > backup.sql
```

Restore database:

```bash
cat backup.sql | docker compose exec -T postgres psql -U education education
```

## Lưu ý dữ liệu seed

Flyway giữ nguyên migration V2 cũ để tránh lỗi checksum trên database đã chạy trước đó. Migration V3 sẽ xóa các tài khoản demo và chỉ giữ lại admin seed `admin@education.com`.

Nếu muốn reset toàn bộ môi trường local:

```bash
docker compose down -v
docker compose up -d --build
```

## Phát triển local

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Vite dev server đã proxy `/api` về `http://localhost:8080`.

## Endpoint chính

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET|POST|PUT|DELETE /api/v1/users`
- `POST /api/v1/media/upload`
- `GET /api/v1/media/stream/{id}`
- `GET /api/v1/media/student/{studentId}`
- `POST|PUT|DELETE /api/v1/evaluations`
- `GET /api/v1/evaluations/student/{studentId}`
- `POST /api/v1/ai/chat`
- `POST /api/v1/ai/suggest-evaluation`
- `GET /api/v1/admin/statistics`
- `GET|POST /api/v1/admin/pending-approvals`
