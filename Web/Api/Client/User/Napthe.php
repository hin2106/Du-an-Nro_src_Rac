<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if ($user) {

    $player = $CVH->player($user['id']);

    $mathe = $_POST["code"];
    $serial = $_POST["serial"];
    $loaithe = $_POST["type"];
    $menhgia = $_POST["amount"];


    if ($loaithe && $menhgia && $mathe && $serial) {

        $tranid = rand(100000, 999999);

        $huydepzaii = $CVH->post_card($tranid, $loaithe, $mathe, $serial, $menhgia, $config['partner_id'], $config['partner_key']);


        if ($huydepzaii['status'] == 99) {
            $table = "cvh_recharge";
            $data = array(
                "id" => null,
                "account_id" => $user['id'],
                "code" => $mathe,
                "serial" => $serial,
                "amount" => $menhgia,
                "type" => $loaithe,
                "amount_real" => '-1',
                "status" => 0,
                "tranid" => $tranid,
                "time" => date("H:i:s d/m/Y")
            );
            $CVH->insert($table, $data);

            $CVH->Ex(true, "Nạp thẻ thành công vui lòng chờ duyệt!");

        } else {

            $CVH->Ex(false, $huydepzaii['message']);

        }

    } else {

        $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");

    }


} else {

    $CVH->Ex(false, "Bạn chưa đăng nhập vui lòng đăng nhập để thực hiện thao tác này!");

}
;