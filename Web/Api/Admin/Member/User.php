<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
$type = abs($_GET['type']);
if ($type == '1') {
    $type = $_POST['type'];
    $username = $_POST['username'];
    if ($type == 'active') {

        if ($CVH->check_user_register($username)) {
            $CVH->Ex(true, "Kích hoạt tài khoản " . $username . " thành công!");
            $table = 'account';
            $data = array(
                "active" => 1
            );
            $where = 'username = "' . $username . '"';
            $CVH->update($table, $data, $where);
        } else {
            $CVH->Ex(false, "Tài khoản không tồn tại!");
        }

    } else if ($type == "unactive") {

        if ($CVH->check_user_register($username)) {
            $CVH->Ex(true, "Hủy kích hoạt tài khoản " . $username . " thành công!");
            $table = 'account';
            $data = array(
                "active" => 0
            );
            $where = 'username = "' . $username . '"';
            $CVH->update($table, $data, $where);
        } else {
            $CVH->Ex(false, "Tài khoản không tồn tại!");
        }
    } else {
        $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");
    }

} else if ($type == '2') {
    $gems = abs(intval($_POST['money']));
    $username = $_POST['username'];
    if ($gems <= 0) {
        $CVH->Ex(false, "Số ngọc không hợp lệ!");
    } elseif ($CVH->check_user_register($username)) {
        $account = $CVH->get_account_by_username($username);
        if (empty($account)) {
            $CVH->Ex(false, "Tài khoản không tồn tại!");
        } else {
            $account_id = intval($account['id']);
            $conn = $CVH->connect_db();
            // Ghi lịch sử nạp ngọc (admin tặng)
            $ins_history = $conn->prepare(
                "INSERT INTO lichsu_napngoc (username, cash_spent, ngoc_received, created_at, status)
                 VALUES (?, 0, ?, NOW(), 'processing')"
            );
            $ins_history->bind_param("si", $username, $gems);
            $ins_history->execute();
            $history_id = $conn->insert_id;
            $ins_history->close();
            // Đưa vào hàng đợi game server
            $ins_reward = $conn->prepare(
                "INSERT INTO pending_rewards (account_id, item_id, quantity, processed, history_id)
                 VALUES (?, 0, ?, 0, ?)"
            );
            $ins_reward->bind_param("iii", $account_id, $gems, $history_id);
            $ins_reward->execute();
            $ins_reward->close();
            $CVH->Ex(true, "Đã thêm " . number_format($gems) . " ngọc vào hàng đợi cho tài khoản " . $username);
        }
    } else {
        $CVH->Ex(false, "Tài khoản không tồn tại!");
    }

} else if ($type == '3') {

    $type = $_POST['type'];
    $username = $_POST['username'];
    if ($type == 'band') {

        if ($CVH->check_user_register($username)) {
            $CVH->Ex(true, "Khóa tài khoản " . $username . " thành công!");
            $table = 'account';
            $data = array(
                "ban" => 1
            );
            $where = 'username = "' . $username . '"';
            $CVH->update($table, $data, $where);
        } else {
            $CVH->Ex(false, "Tài khoản không tồn tại!");
        }

    } else if ($type == "unband") {

        if ($CVH->check_user_register($username)) {
            $CVH->Ex(true, "Mở khóa hoạt tài khoản " . $username . " thành công!");
            $table = 'account';
            $data = array(
                "ban" => 0
            );
            $where = 'username = "' . $username . '"';
            $CVH->update($table, $data, $where);
        } else {
            $CVH->Ex(false, "Tài khoản không tồn tại!");
        }
    } else {
        $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");
    }

} else {

    $CVH->Ex(false, "Lỗi rồi địt mẹ mày!");

}