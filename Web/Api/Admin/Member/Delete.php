<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if (!$user['is_admin']) {
    header("Location: /");
    exit;
} else {
    if ($_POST['type'] == 'Del_Mem') {
        $id = abs($_POST['id']);
        if (isset($id)) {
            mysqli_query($CVH->connect_db(), "DELETE FROM `player` WHERE `account_id`='" . $id . "'");
            mysqli_query($CVH->connect_db(), "DELETE FROM `account` WHERE `id` = '" . $id . "' ");
            $CVH->Ex(true, "Xóa tài khoản và nhân vật thành công!");
        } else {
            $CVH->Ex(true, "Tài khoản không tồn tại!");
        }
    }
}
?>