<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if (!$user['is_admin']) {
    header("Location: /");
    exit;
} else {
    if ($_POST['type'] == 'Del_Post') {
        $id = abs($_POST['id']);
        if (isset($id)) {
            mysqli_query($CVH->connect_db(), "DELETE FROM `cvh_baiviet` WHERE `id`='" . $id . "'");
            $CVH->Ex(true, "Xóa bài viết có ID #". $id ." thành công!");
        } else {
            $CVH->Ex(true, "Bài viết không tồn tại!");
        }
    }
}
?>