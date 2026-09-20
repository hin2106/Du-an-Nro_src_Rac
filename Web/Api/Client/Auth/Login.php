<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
if (!empty($_POST['username']) && !empty($_POST['password'])) {

    $username = $CVH->antil_text($CVH->rewrite($CVH->FormatString($_POST['username'])));
    $password = $CVH->FormatString($_POST['password']);

    if (!$CVH->check_user($username, $password)) {

        $CVH->Ex(false, "Tài khoản hoặc mật khẩu không chính xác!");

    } else {

        $account = $CVH->get_account_by_username($username);

        if (!$CVH->player($account['id'])) {

            $CVH->Ex(false, "Vui lòng tạo nhân vật trước khi đăng nhập");

        } else if (!$account['ban'] == 0) {
            $CVH->Ex(false, "Tài khoản của bạn đang bị khóa");
        } else {

            $token = $CVH->Token($username, $password);
            $sngay = time() + (7 * 24 * 60 * 60);
            setcookie('token', $token, $sngay, '/');
            $CVH->Ex(true, "Đăng nhập thành công!");

        }
    }

} else {

    $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");

}