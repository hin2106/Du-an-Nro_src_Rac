# Triển khai NRO trên Windows Server 2012 R2

## Cổng công khai

- `14445/TCP`: Netty game proxy.
- `80/TCP`, `443/TCP`: website; ưu tiên chỉ dùng `443` sau khi HTTPS hoạt động.
- `14446/TCP`: backend game, mã nguồn tự bind `127.0.0.1`; không mở trên router/firewall.
- `3306/TCP`: MySQL phải bind `127.0.0.1`; không mở công khai.

Chạy PowerShell bằng quyền Administrator rồi chạy `Configure-NroFirewall.ps1`. Script chỉ bổ sung luật allow cần thiết, không thay đổi luật RDP hay chính sách mặc định.

## Apache

1. Sửa đường dẫn `C:/path/to/Web` trong `apache-nro-security.conf`.
2. Thêm vào cuối `httpd.conf`: `Include "C:/duong-dan/apache-nro-security.conf"`.
3. Kiểm tra cấu hình bằng `httpd.exe -t`, sau đó mới khởi động lại Apache.
4. Bật `mod_reqtimeout`, `mod_headers` và PHP OPcache.

Sau khi cập nhật source hoặc database, chạy migration đúng một lần trong thư mục `Web`:

`php migrate.php`

Migration không còn chạy trong từng request web để tránh khuếch đại tải lên MySQL khi bị request flood.

## Game server

Các giá trị triển khai đã được đặt trong `data/config/sever.properties`:

- `server.usenettyproxy=true`
- `server.backendport=14446`
- `server.behindfirewall=false`
- `server.maxperip=20`

Không đổi `behindfirewall` thành `true`: proxy mới truyền IP thật về backend nên việc bỏ qua giới hạn IP vừa không cần thiết vừa làm yếu bảo vệ.

## Giới hạn thực tế

Các lớp trong ứng dụng bảo vệ tốt trước reconnect flood, slow connection, packet spam và lạm dụng API ở mức vừa. Chúng không thể cứu đường truyền khi băng thông Internet đã bị lấp đầy. Website nên đặt sau dịch vụ reverse proxy/CDN; cổng TCP game cần nhà cung cấp VPS có chống DDoS tầng mạng nếu bị tấn công lớn.
