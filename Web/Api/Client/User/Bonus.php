<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
if ($user) {

    if ($CVH->checkNhanqua($user['username'])) {
        $response = array(
            "status" => 'OK',
            "msg" => '
        <div class="text-center">
        <img width="70%" src="/Assets/images/icon/giftbox.gif"/>
        <h5>Vui Chơi Cùng Ngọc Rồng VN</h5>
        <b>Bạn Đã Nhận Quà Trước Đó Rồi</b>
        <p>Chúc Các Bạn Chơi Game Vui Vẻ!</p>
        </div>'
        );
    } else {
        // Random ngọc: 99% nhận 1-10 ngọc, 1% jackpot 100 ngọc
        $rand = mt_rand(1, 100);
        $ngocThuong = ($rand === 1) ? 100 : mt_rand(1, 10);

        $account = $CVH->get_account_by_username($user['username']);
        if (!empty($account)) {
            $account_id = intval($account['id']);
            $conn = $CVH->connect_db();

            // Ghi lịch sử
            $ins_history = $conn->prepare(
                "INSERT INTO lichsu_napngoc (username, cash_spent, ngoc_received, created_at, status)
                 VALUES (?, 0, ?, NOW(), 'processing')"
            );
            $ins_history->bind_param("si", $user['username'], $ngocThuong);
            $ins_history->execute();
            $history_id = $conn->insert_id;
            $ins_history->close();

            // Đưa vào hàng đợi game server
            $ins_reward = $conn->prepare(
                "INSERT INTO pending_rewards (account_id, item_id, quantity, processed, history_id)
                 VALUES (?, 0, ?, 0, ?)"
            );
            $ins_reward->bind_param("iii", $account_id, $ngocThuong, $history_id);
            $ins_reward->execute();
            $ins_reward->close();
        }

        $CVH->addNhanqua($user['username']);

        $response = array(
            "status" => 'OK',
            "msg" => '
        <div class="text-center">
        <img width="70%" src="/Assets/images/icon/giftbox.gif"/>
        <h5>Ngọc Rồng VN</h5>
        <b>Bạn Nhận Được <span style="color: #FF0000;">' . $ngocThuong . ' ngọc</span> vào game!</b>
        <p>Chúc Các Bạn Chơi Game Vui Vẻ!</p>
        </div>'
        );
    }
} else {
    $response = array(
        "status" => 'LOGIN',
        "msg" => '
        <div class="text-center">
        <b>Vui Lòng Đăng Nhập</b>
        </div>'
    );
}

header('Content-Type: application/json');
echo json_encode($response);
?>