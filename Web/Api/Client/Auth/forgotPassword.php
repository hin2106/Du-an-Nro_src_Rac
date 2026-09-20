<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

require_once $_SERVER['DOCUMENT_ROOT'] . "/PHPMailer/src/Exception.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/PHPMailer/src/PHPMailer.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/PHPMailer/src/SMTP.php";

if (empty($_POST['email'])) {
    $CVH->Ex(false, "Vui lòng nhập email!");
	exit;
}

$email = trim($_POST['email']); // <-- thêm dòng này

// Include file database config
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Database.php";

// Tạo kết nối
$conn = new mysqli($DB['SERVER'], $DB['USERNAME'], $DB['PASSWORD'], $DB['TABLE']);

// Kiểm tra kết nối
if ($conn->connect_error) {
    $CVH->Ex(false, "Kết nối database thất bại: " . $conn->connect_error);
	exit;
}

// Kiểm tra email có tồn tại
$stmt = $conn->prepare("SELECT username FROM account WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();
$user = $result->fetch_assoc();
$stmt->close();

if (!$user) {
    $CVH->Ex(false, "Email không tồn tại trong hệ thống!");
	exit;
}

// Giới hạn spam
$hour_limit = 3;
$day_limit = 10;

// Đếm số lần gửi trong 1 giờ qua
$stmt = $conn->prepare("SELECT COUNT(*) FROM password_reset WHERE email = ? AND created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->bind_result($hour_count);
$stmt->fetch();
$stmt->close();

// Đếm số lần gửi trong 24 giờ qua
$stmt = $conn->prepare("SELECT COUNT(*) FROM password_reset WHERE email = ? AND created_at > DATE_SUB(NOW(), INTERVAL 1 DAY)");
$stmt->bind_param("s", $email);
$stmt->execute();
$stmt->bind_result($day_count);
$stmt->fetch();
$stmt->close();

if ($hour_count >= $hour_limit) {
    $CVH->Ex(false, "Bạn chỉ có thể gửi tối đa $hour_limit mail trong 1 giờ!");
	exit;
}

if ($day_count >= $day_limit) {
    $CVH->Ex(false, "Bạn chỉ có thể gửi tối đa $day_limit mail mỗi ngày!");
	exit;
}

// Tạo token bảo mật
$token = bin2hex(random_bytes(32));
$hashed_token = hash('sha256', $token);

// Lưu token
$stmt = $conn->prepare("INSERT INTO password_reset (email, token, created_at) VALUES (?, ?, NOW())");
$stmt->bind_param("ss", $email, $hashed_token);
$stmt->execute();
$stmt->close();

// Link reset
$reset_link = FULL_URL("/dat-lai-mat-khau?token=" . urlencode($token) . "&email=" . urlencode($email));

// Gửi mail
$mail = new PHPMailer(true);
try {
    $mail->isSMTP();
    $mail->Host = 'smtp.gmail.com';
    $mail->SMTPAuth = true;
    $mail->Username = 'fistartonline@gmail.com';
    $mail->Password = 'ejxm arwg uitg qucx';
    $mail->SMTPSecure = PHPMailer::ENCRYPTION_STARTTLS;
    $mail->Port = 587;
    $mail->CharSet = 'UTF-8';

    $mail->setFrom('fistartonline@gmail.com', 'Fist Art Online');
    $mail->addAddress($email);
    $mail->isHTML(true);
    $mail->Subject = 'Khôi phục mật khẩu Nro FistArtOnline';
    $mail->Body = "
        <h3>Xin chào {$user['username']},</h3>
        <p>Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào link dưới đây để tiếp tục:</p>
        <p><a href='{$reset_link}'>{$reset_link}</a></p>
        <p>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>
        <hr><p>Link có hiệu lực trong 30 phút.</p>
    ";

    $mail->send();
    $CVH->Ex(true, "Link đặt lại mật khẩu đã được gửi đến email của bạn!");
	exit;
} catch (Exception $e) {
    $CVH->Ex(false, "Gửi mail thất bại: {$mail->ErrorInfo}");
	exit;
}

// Đóng kết nối
$conn->close();
?>