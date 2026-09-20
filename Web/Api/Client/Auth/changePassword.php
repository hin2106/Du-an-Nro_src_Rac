<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if (!empty($_POST['old_password']) && !empty($_POST['new_password']) && !empty($_POST['renew_password'])) {

    $username = $user['username'];
    $old_password = $CVH->FormatString($_POST['old_password']);
    $new_password = $CVH->FormatString($_POST['new_password']);
    $renew_password = $CVH->FormatString($_POST['renew_password']);

    // kiểm tra mật khẩu cũ
    if ($CVH->check_user_password($username, $old_password) == false) {
        $CVH->Ex(false, "Mật khẩu cũ không chính xác!");
    } 
    // kiểm tra mật khẩu mới có khớp nhau
    else if ($new_password != $renew_password) {
        $CVH->Ex(false, "Hai mật khẩu mới bạn nhập chưa giống nhau!");
    } 
    // kiểm tra mật khẩu mới có giống mật khẩu cũ
    else if ($old_password == $renew_password || $old_password == $new_password) {
        $CVH->Ex(false, "Mật khẩu mới không nên để giống mật khẩu cũ!");
    }
    // kiểm tra độ dài mật khẩu mới
    else if ($CVH->LimitString($new_password, 4, 9) == false) {
        $CVH->Ex(false, "Mật khẩu mới bắt buộc phải từ 4 tới 9 ký tự!");
    } 
    // update mật khẩu mới
    else {
        $table = "account";
		$password = $new_password;
        $data = array(
            "password" => $new_password
        );
        $where = 'username = "' . $username . '"';
		$CVH->update($table, $data, $where);
		$token = $CVH->Token($username, $password);
		$CVH->Ex(true, "Đổi mật khẩu thành công!");
    }

} else {
    $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");
}