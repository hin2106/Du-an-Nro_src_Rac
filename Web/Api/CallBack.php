<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if (isset($_GET['status']) && isset($_GET['request_id'])) {
    $tranid = $_GET['request_id'];
    $status = $_GET['status'];
    $am_real = $_GET['amount'];

    $conn = $CVH->connect_db();

    $stmt = $conn->prepare("SELECT * FROM `cvh_recharge` WHERE `tranid` = ?");
    $stmt->bind_param("s", $tranid);
    $stmt->execute();
    $result = $stmt->get_result();
    $get = $result->fetch_assoc();

    if ($result->num_rows > 0) {
        $account_id = $get['account_id'];
        $monney = $get['amount'];

        if ($status == 1) {
            // Quy đổi VND nhận được sang ngọc (1000 VND = 1 ngọc)
            $gems = max(1, intval($am_real / 1000));

            // Ghi lịch sử nạp ngọc
            $ins_history = $conn->prepare(
                "INSERT INTO lichsu_napngoc (username, cash_spent, ngoc_received, created_at, status)
                 SELECT a.username, ?, ?, NOW(), 'processing' FROM account a WHERE a.id = ?"
            );
            $ins_history->bind_param("iii", $monney, $gems, $account_id);
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

            // Cập nhật tổng nạp (VND) để thống kê doanh thu
            $update_tongnap = $conn->prepare("UPDATE `account` SET `tongnap` = `tongnap` + ? WHERE `id` = ?");
            $update_tongnap->bind_param("di", $monney, $account_id);
            $update_tongnap->execute();
            $update_tongnap->close();

            $update2 = $conn->prepare("UPDATE `cvh_recharge` SET `status` = '1', `amount_real` = ? WHERE `tranid` = ?");
            $update2->bind_param("ds", $am_real, $tranid);
            $update2->execute();
            $update2->close();
        } else {
            $update2 = $conn->prepare("UPDATE `cvh_recharge` SET `status` = '2', `amount_real` = 0 WHERE `tranid` = ?");
            $update2->bind_param("s", $tranid);
            $update2->execute();
            $update2->close();
        }
    }

    $stmt->close();
    $conn->close();
}
?>