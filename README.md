# Báo Cáo Bài Tập 2 (Session 8): Hoàn Thiện Dockerfile Multi-stage Build

---

## 1. Thông Tin Chung

- **Học viên:** Nguyễn Dương
- **Hệ thống:** QuickBite Microservices Platform
- **Dịch vụ triển khai:** `cart-service` (Dịch vụ giỏ hàng)
- **Công nghệ cốt lõi:** Java 17, Spring Boot 3.2.3, Gradle 8.5, Docker Multi-stage Build
- **Repository GitHub:** [Session8-BTVN-Devops-B2](https://github.com/NguyenDuong1509/Session8-BTVN-Devops-B2)

---

## 2. Bối Cảnh Tình Huống & Phân Tích Điền Khuyết

### 2.1. Đoạn mã Dockerfile bị khuyết của đề bài

```dockerfile
# Stage 1: Build mã nguồn
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

# Stage 2: Runtime
FROM [___]
WORKDIR /app
[___]
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 2.2. Phân tích chi tiết các vị trí điền khuyết

#### 1. Vị trí khuyết 1: `FROM [___]` tại Stage 2
* **Giá trị điền:** `eclipse-temurin:17-jre-alpine`
* **Lý do kỹ thuật:**
  * **Sự khác biệt giữa JDK và JRE:**
    * **JDK (Java Development Kit):** Bao gồm trình biên dịch (`javac`), công cụ gỡ lỗi, tài liệu, Gradle runner và mã nguồn thư viện $\rightarrow$ Image nền `17-jdk-alpine` có dung lượng lên đến **~389MB**.
    * **JRE (Java Runtime Environment):** Chỉ bao gồm Java Virtual Machine (JVM) và các thư viện cốt lõi tối thiểu cần thiết để thực thi bytecode $\rightarrow$ Image nền `17-jre-alpine` chỉ nặng khoảng **~120MB**.
  * Ở Stage 2 (Runtime Production), file `.jar` đã được biên dịch xong ở Stage 1, ứng dụng **hoàn toàn không cần các công cụ phát triển của JDK**. Việc chọn `eclipse-temurin:17-jre-alpine` giúp cắt giảm ngay lập tức gần **270MB** dung lượng cơ sở.

#### 2. Vị trí khuyết 2: `[___]` tại Stage 2
* **Giá trị điền:** `COPY --from=builder /app/build/libs/*.jar app.jar`
* **Lý do kỹ thuật:**
  * **Cú pháp `COPY --from=...` trong Docker Multi-stage:** Cho phép copy trực tiếp sản phẩm đầu ra từ một stage trước đó (ở đây là stage mang bí danh `builder`) sang stage hiện tại.
  * Đường dẫn `/app/build/libs/*.jar` là thư mục mặc định mà Gradle tạo ra file thực thi Spring Boot executable JAR sau lệnh `bootJar`.
  * Đặt tên đích là `app.jar` để khớp chính xác với chỉ thị khởi chạy `ENTRYPOINT ["java", "-jar", "app.jar"]`.
  * **Lợi ích bảo mật:** Toàn bộ mã nguồn `.java`, file cấu hình nhạy cảm, cache Gradle và các công cụ trung gian ở Stage 1 đều bị loại bỏ hoàn toàn, không lọt vào image cuối cùng.

---

## 3. Nội Dung Tệp Tin `Dockerfile` Hoàn Chỉnh

```dockerfile
# Stage 1: Build mã nguồn
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew bootJar -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 4. Đánh Giá Hiệu Quả & So Sánh Dung Lượng Thực Tế

### 4.1. Bảng so sánh dung lượng (`docker images`)

| Phương Pháp Đóng Gói | Base Image Sử Dụng | Thành Phần Bên Trong Image | Dung Lượng Thực Tế | Tỷ Lệ Tối Ưu |
| :--- | :--- | :--- | :--- | :--- |
| **Single-stage Build** | `eclipse-temurin:17-jdk-alpine` | Full JDK + Source Code + Gradle Wrapper + Build Cache + File JAR | **486 MB** | Gốc (100%) |
| **Multi-stage Build** | `eclipse-temurin:17-jre-alpine` | Chỉ có JRE 17 Alpine + File `app.jar` (19.1MB) | **139 MB** | **Giảm 71.4% (-347 MB)** |

> **Đạt yêu cầu đề bài:** Dung lượng thực tế của `cart-service:multi-stage` là **139MB** (đạt tiêu chuẩn dưới 150MB).

---

### 4.2. Minh Chứng So Sánh Bằng Lệnh `docker images`

Ảnh chụp màn hình terminal kiểm chứng dung lượng giữa 2 phương pháp đóng gói:

![Docker Images Size Comparison](homework/session_08/exercise_02/docker_images_comparison.png)

---

### 4.3. Minh Chứng Quá Trình Build Multi-stage Hoàn Chỉnh

Ảnh chụp màn hình terminal ghi lại quá trình Docker chuyển giao dữ liệu giữa Stage 1 (`builder`) sang Stage 2 (`runtime`):

![Multi-stage Build Process](homework/session_08/exercise_02/multistage_build_process.png)

---

## 5. Tầm Quan Trọng Của Multi-stage Build Trong Thực Tế

1. **Tăng tốc triển khai (Deployment Speed):** Kích thước image giảm từ gần 500MB xuống còn 139MB giúp việc `docker push` lên Container Registry và `docker pull` trên các cụm máy chủ Kubernetes/ECS nhanh hơn gấp 3.5 lần.
2. **Khởi tạo Pod/Container thần tốc:** Đặc biệt quan trọng đối với cơ chế Auto-scaling khi lưu lượng truy cập hệ thống QuickBite tăng đột biến.
3. **Bảo mật tối đa (Attack Surface Minimization):**
   * Không chứa mã nguồn thô trong image Production, phòng ngừa rò rỉ mã nguồn khi container bị tấn công.
   * Không chứa các trình biên dịch hoặc package quản lý code có thể bị kẻ tấn công lợi dụng để tải mã độc hoặc compile exploit payload.

---

## 6. Hướng Dẫn Nộp Bài Lên Portal

1. **Link Repository:**
   ```text
   https://github.com/NguyenDuong1509/Session8-BTVN-Devops-B2
   ```
2. **File nộp kèm (nếu Portal yêu cầu nộp file/ảnh):**
   * File: `Dockerfile`
   * Ảnh so sánh dung lượng `docker images`: `homework/session_08/exercise_02/docker_images_comparison.png`
