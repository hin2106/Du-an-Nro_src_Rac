<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if (!$user['is_admin']) {
    header("Location: /");
    exit;
} else {
    if ($_POST['type'] == 'Del_Gift') {
        $id = abs($_POST['id']);
        if (isset($id)) {
            $db = $CVH->connect_db();
            $giftcodeTable = mysqli_query($db, "SHOW TABLES LIKE 'giftcode'");
            $legacyTable = mysqli_query($db, "SHOW TABLES LIKE 'cvh_giftcode'");

            if ($giftcodeTable && mysqli_num_rows($giftcodeTable) > 0) {
                mysqli_query($db, "DELETE FROM `giftcode` WHERE `id`='" . $id . "'");
                $CVH->Ex(true, "Xóa giftcode thành công!");
                return;
            }

            if ($legacyTable && mysqli_num_rows($legacyTable) > 0) {
                mysqli_query($db, "DELETE FROM `cvh_giftcode` WHERE `id`='" . $id . "'");
                $CVH->Ex(true, "Xóa giftcode thành công!");
                return;
            }

            $CVH->Ex(false, "Không tìm thấy bảng giftcode phù hợp!");
        } else {
            $CVH->Ex(true, "Giftcode không tồn tại!");
        }
    }
}
?>