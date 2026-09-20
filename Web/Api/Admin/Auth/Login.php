<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
if (!empty($_POST['password'])) {

    $password = $CVH->FormatString($_POST['password']);
    
    // Kiểm tra user đã được xác định chưa
    if (!isset($user) || empty($user['username'])) {
        $CVH->Ex(false, "Phiên đăng nhập không hợp lệ!");
        exit;
    }

    if (!$CVH->LoginAD($user['username'], $password)) {
        $CVH->Ex(false, "Tài khoản hoặc mật khẩu không chính xác!");
    } else {
        $token = $CVH->Token($user['username'], $password);
        $sngay = time() + (7 * 24 * 60 * 60);
        $_SESSION['admin'] = $token;
        $CVH->Ex(true, "Đăng nhập thành công!");
    }

} else {

    $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");

}