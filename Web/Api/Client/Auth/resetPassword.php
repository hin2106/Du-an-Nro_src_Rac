<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Database.php";
header("Content-Type: application/json; charset=utf-8");

// Lấy dữ liệu POST
$email = $_POST['email'] ?? '';
$token = $_POST['token'] ?? '';
$new_pass = $_POST['new_password'] ?? '';

// Debug: Kiểm tra dữ liệu nhận được
error_log("Reset Password Debug - Email: $email, Token: $token, New Pass: $new_pass");

// Kiểm tra dữ liệu đầu vào
if (empty($email) || empty($token) || empty($new_pass)) {
    $CVH->Ex(false, "Thiếu thông tin! Vui lòng kiểm tra lại.");
    exit;
}

// Kết nối database
$conn = new mysqli($DB['SERVER'], $DB['USERNAME'], $DB['PASSWORD'], $DB['TABLE']);
if ($conn->connect_error) {
    $CVH->Ex(false, "Kết nối database thất bại: " . $conn->connect_error);
    exit;
}

// Mã hoá token để so sánh (giống với cách lưu trong database)
$hashed_token = hash('sha256', $token);

// Kiểm tra token hợp lệ & chưa quá 30 phút
$stmt = $conn->prepare("
    SELECT * FROM password_reset 
    WHERE email = ? 
      AND token = ? 
      AND created_at > DATE_SUB(NOW(), INTERVAL 30 MINUTE)
      AND used = 0
");
$stmt->bind_param("ss", $email, $hashed_token);
$stmt->execute();
$result = $stmt->get_result();
$token_row = $result->fetch_assoc();
$stmt->close();

if (!$token_row) {
    $CVH->Ex(false, "Link đặt lại mật khẩu không hợp lệ, đã hết hạn hoặc đã được sử dụng!");
    exit;
}

// MÃ HÓA MẬT KHẨU MỚI TRƯỚC KHI LƯU
// $hashed_password = password_hash($new_pass, PASSWORD_DEFAULT);

// Cập nhật mật khẩu mới (đã mã hóa)
$stmt = $conn->prepare("UPDATE account SET password = ? WHERE email = ?");
$stmt->bind_param("ss", $new_pass, $email);
$stmt->execute();

if ($stmt->affected_rows === 0) {
    $stmt->close();
    $conn->close();
    $CVH->Ex(false, "Không tìm thấy tài khoản với email này!");
    exit;
}
$stmt->close();

// Đánh dấu token đã sử dụng (thay vì xóa)
$stmt = $conn->prepare("UPDATE password_reset SET used = 1 WHERE email = ? AND token = ?");
$stmt->bind_param("ss", $email, $hashed_token);
$stmt->execute();
$stmt->close();

// Đóng kết nối
$conn->close();

// Phản hồi kết quả
$CVH->Ex(true, "Mật khẩu đã được đặt lại thành công! Bạn có thể đăng nhập với mật khẩu mới.");
exit;