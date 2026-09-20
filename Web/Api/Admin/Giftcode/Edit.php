<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
header('Content-Type: application/json; charset=utf-8');

if (!$user['is_admin']) {
    echo json_encode(['status' => false, 'message' => 'Bạn không có quyền thực hiện thao tác này!']);
    exit;
} else {
    if ($_POST['type'] == 'EditCount') {
        $id = abs(intval($_POST['id']));
        $count_left = abs(intval($_POST['number']));
        
        if ($id > 0 && $count_left >= 0) {
            $db = $CVH->connect_db();
            $giftcodeTable = mysqli_query($db, "SHOW TABLES LIKE 'giftcode'");
            $legacyTable = mysqli_query($db, "SHOW TABLES LIKE 'cvh_giftcode'");

            if ($giftcodeTable && mysqli_num_rows($giftcodeTable) > 0) {
                $table = 'giftcode';
                $data = array(
                    "count_left" => $count_left
                );
                $where = 'id = "' . $id . '"';
                $CVH->update($table, $data, $where);
                $CVH->Ex(true, "Cập nhật số lượt thành công!");
                return;
            }

            if ($legacyTable && mysqli_num_rows($legacyTable) > 0) {
                $table = 'cvh_giftcode';
                $data = array(
                    "luot" => $count_left
                );
                $where = 'id = "' . $id . '"';
                $CVH->update($table, $data, $where);
                $CVH->Ex(true, "Cập nhật số lượt thành công!");
                return;
            }

            $CVH->Ex(false, "Không tìm thấy bảng giftcode phù hợp!");
        } else {
            $CVH->Ex(false, "Dữ liệu không hợp lệ!");
        }
    } else {
        $CVH->Ex(false, "Loại thao tác không hợp lệ!");
    }
}
?>