<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if ($user) {
    if (isset($_POST['username']) !== $user['username']) {
        if ($user['active'] == 0) {
            // Yêu cầu nạp tối thiểu amount_mtv VND (= amount_mtv/1000 ngọc) để kích hoạt
            $gemsRequired = intval($setting['amount_mtv'] / 1000);

            if ($user['napngoc'] >= $gemsRequired) {
                $CVH->update('account', ['active' => 1], 'username = "' . $user['username'] . '"');
                $CVH->Ex(true, "Bạn đã kích hoạt tài khoản thành công!");
            } else {
                $CVH->Ex(false, "Bạn cần nạp ít nhất " . number_format($gemsRequired) . " ngọc để kích hoạt tài khoản!");
            }

        } else {
            $CVH->Ex(false, "Có lỗi xảy ra, vui lòng thử lại sau!");
        }
    } else {
        $CVH->Ex(false, "Có lỗi xảy ra, vui lòng thử lại sau!");
    }
} else {
    $CVH->Ex(false, "Bạn chưa đăng nhập vui lòng đăng nhập để thực hiện thao tác này!");
}