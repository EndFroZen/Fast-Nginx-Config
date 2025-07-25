# 🚀 FastNginx - Green Sci-Fi Edition

```
    ▄▀█ █▀▀ ▄▀█ █▀ ▀█▀   █▄░█ █▀▀ █ █▄░█ ▀▄ ▄▀   ▀▀█▀▀ █▀█ █▀█ █░░   
    █▀▀ █▄▄ █▀█ ▄█ ░█░   █░▀█ █▄█ █ █░▀█ ░▀▄▀░   ░░█░░ █▄█ █▄█ █▄▄   
                    [ NEURAL NETWORK PROXY MANAGER ]                  
```

**FastNginx** เป็นเครื่องมือจัดการ Nginx Proxy Configuration แบบ Interactive สำหรับ Linux ที่ออกแบบมาเพื่อความง่ายและรวดเร็ว พร้อมด้วยธีม Green Sci-Fi ที่สวยงาม

## 🖥️ ความต้องการระบบ

- **ระบบปฏิบัติการ**: Linux เท่านั้น (Ubuntu, Debian, CentOS, RHEL, Arch Linux)
- **Java**: OpenJDK 11+ หรือ Oracle JDK 11+
- **Nginx**: ติดตั้งและเรียกใช้งานได้
- **Sudo privileges**: สำหรับการจัดการ Nginx และไฟล์ระบบ

## 📦 การติดตั้ง

### 1. ติดตั้ง Prerequisites

#### Ubuntu/Debian:
```bash
# อัพเดท package list
sudo apt update

# ติดตั้ง Java และ Nginx
sudo apt install openjdk-11-jdk nginx

# เปิดใช้งาน Nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

#### CentOS/RHEL:
```bash
# ติดตั้ง Java และ Nginx
sudo yum install java-11-openjdk-devel nginx

# เปิดใช้งาน Nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

#### Arch Linux:
```bash
# ติดตั้ง Java และ Nginx
sudo pacman -S jdk11-openjdk nginx

# เปิดใช้งาน Nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 2. ดาวน์โหลดและคอมไพล์

```bash
# Clone หรือดาวน์โหลด source code
git clone <repository-url>
cd fastnginx

# คอมไพล์โปรแกรม
javac FastNginx.java

# หรือใช้ jar file (ถ้ามี)
java -jar FastNginx.jar
```

### 3. ตั้งค่าสิทธิ์

```bash
# เพิ่มผู้ใช้ปัจจุบันเข้ากลุม sudo (ถ้ายังไม่ได้ทำ)
sudo usermod -aG sudo $USER

# ตรวจสอบสิทธิ์ nginx
sudo nginx -t
```

## 🚀 การใช้งาน

### เริ่มต้นการใช้งาน

```bash
# รันโปรแกรม
java FastNginx
```

เมื่อเริ่มต้นครั้งแรก โปรแกรมจะขอให้คุณกำหนด **System Path** สำหรับเก็บข้อมูลการกำหนดค่า

```
┌─[Initialize system path]
└──➤ /home/username/fastnginx-data
```

### หน้าจอหลัก

โปรแกรมจะแสดงเมนูหลักสไตล์ Sci-Fi:

```
╔═══════════════════════════════════════╗
║  NEURAL COMMAND INTERFACE             ║
╠═══════════════════════════════════════╣
║  [1] Deploy Proxy Configuration       ║
║  [2] Manage Configurations            ║
║  [3] System Diagnostics               ║
║  [Q] Terminate Session                ║
╚═══════════════════════════════════════╝
```

## 🔧 ฟีเจอร์หลัก

### 1. Deploy Proxy Configuration (เมนู 1)

สร้าง Nginx proxy configuration ใหม่:

- **Service Protocol**: ป้อน `proxy` (ปัจจุบันรองรับเฉพาะ proxy)
- **Target Domain**: ชื่อโดเมน เช่น `example.local`, `api.myapp.com`
- **Backend Port**: พอร์ตของแอปพลิเคชัน เช่น `3000`, `8080`
- **Add to /etc/hosts**: เลือกเพิ่มโดเมนเข้า hosts file

**ตัวอย่าง:**
```
Target Domain: myapp.local
Backend Port: 3000
Add to /etc/hosts: y
IP Address: 127.0.0.1
```

### 2. Manage Configurations (เมนู 2)

จัดการ configuration ที่มีอยู่:

```
╔═══ CONFIGURATION MATRIX ═══╗
[01] ● myapp.local          → :3000 (proxy)
[02] ● api.example.com      → :8080 (proxy)
╚═══════════════════════════╝
```

**การดำเนินการ:**
- **[E]dit**: แก้ไขโดเมนหรือพอร์ต
- **[D]elete**: ลบ configuration
- **[T]oggle**: เปิด/ปิดการใช้งาน

### 3. System Diagnostics (เมนู 3)

ตรวจสอบสถานะระบบ:
- สถานะ Nginx Service
- ตรวจสอบ Configuration Syntax
- Scan พอร์ตที่ใช้งาน

## 📂 โครงสร้างไฟล์

```
fastnginx-data/
├── nginx_data/
│   └── config_index          # ดัชนี configuration ทั้งหมด
├── .fastnginx_config         # การตั้งค่าหลัก
/etc/nginx/
├── sites-available/
│   ├── myapp.local           # Configuration files
│   └── api.example.com
└── sites-enabled/            # Symbolic links
    ├── myapp.local -> ../sites-available/myapp.local
    └── api.example.com -> ../sites-available/api.example.com
```

## ⚙️ Configuration Template

โปรแกรมจะสร้าง Nginx configuration ดังนี้:

```nginx
server {
    listen 80;
    server_name example.local;
    
    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Proxy configuration
    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        
        # WebSocket support
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_cache_bypass $http_upgrade;
        
        # Standard proxy headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Health check endpoint
    location /nginx-health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

## 🔍 การแก้ปัญหา

### ปัญหาที่พบบ่อย

1. **Permission Denied**
   ```bash
   # ตรวจสอบสิทธิ์ sudo
   sudo -l
   
   # เพิ่มผู้ใช้เข้ากลุม sudo
   sudo usermod -aG sudo $USER
   ```

2. **Nginx Configuration Error**
   ```bash
   # ตรวจสอบ syntax
   sudo nginx -t
   
   # ดู error log
   sudo tail -f /var/log/nginx/error.log
   ```

3. **Port Already in Use**
   ```bash
   # ตรวจสอบพอร์ตที่ใช้งาน
   sudo ss -tlnp | grep :80
   
   # หยุด service ที่ขัดแย้ง
   sudo systemctl stop apache2  # ถ้ามี Apache
   ```

4. **Java Not Found**
   ```bash
   # ตรวจสอบ Java version
   java -version
   
   # ตั้งค่า JAVA_HOME (ถ้าจำเป็น)
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
   ```

### การตรวจสอบสถานะ

```bash
# ตรวจสอบสถานะ Nginx
sudo systemctl status nginx

# ตรวจสอบ configuration
sudo nginx -t

# ดู active connections
sudo ss -tlnp | grep nginx

# ตรวจสอบ log
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## 📋 ข้อกำหนดความปลอดภัย

- โปรแกรมต้องการสิทธิ์ `sudo` เพื่อ:
  - แก้ไขไฟล์ในโฟลเดอร์ `/etc/nginx/`
  - รีโหลด Nginx service
  - แก้ไขไฟล์ `/etc/hosts`

- การตั้งค่าจะถูกเก็บใน:
  - System configuration: `~/.fastnginx_config`
  - Data directory: ตามที่ผู้ใช้กำหนด


## 🤝 การสนับสนุน

- **ระบบปฏิบัติการ**: Linux เท่านั้น
- **สถาปัตยกรรม**: x86_64, ARM64
- **Nginx version**: 1.14+

## ⚠️ ข้อควรระวัง

1. **Linux Only**: โปรแกรมนี้ออกแบบมาสำหรับ Linux เท่านั้น ไม่รองรับ Windows หรือ macOS
2. **Sudo Required**: ต้องมีสิทธิ์ sudo ในการจัดการ Nginx
3. **Backup Configs**: แนะนำให้สำรองข้อมูล configuration ก่อนใช้งาน
4. **Port Conflicts**: ตรวจสอบให้แน่ใจว่าพอร์ตที่ใช้ไม่มีบริการอื่นใช้งานอยู่

## 📜 License

MIT License - ใช้งานได้อย่างอิสระสำหรับโครงการส่วนตัวและเชิงพาณิชย์

---

**พัฒนาด้วย ❤️ สำหรับ Linux Community**
