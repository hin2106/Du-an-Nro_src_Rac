<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

$title = $_POST["title"];
$eff_load = $_POST["eff_load"];
$author = $_POST["author"];
$description = $_POST["description"];
$keywords = $_POST["keywords"];
$nhanqua = $_POST["nhanqua"];
$vongquay = $_POST["vongquay"];
$amount_mtv = $_POST["amount_mtv"];
$thongbao = $_POST["thongbao"];
$favicon = $_POST["favicon"];
$logo = $_POST["logo"];
$nd_thongbao = $_POST["nd_thongbao"];
$background = $_POST["background"];
$size_logo = $_POST["size_logo"];

if (!empty($size_logo) &&!empty($background) && !empty($favicon) && isset($logo) &&!empty($title) && isset($eff_load) && !empty($author) && !empty($description) && !empty($keywords) && isset($nhanqua) && isset($vongquay) && !empty($amount_mtv)) {

    $data = array(
        "size_logo" => $size_logo,
        "background" => $background,
        "favicon" => $favicon,
        "logo" => $logo,
        "title" => $title,
        "eff_load" => $eff_load,
        "author" => $author,
        "description" => $description,
        "keywords" => $keywords,
        "nhanqua" => $nhanqua,
        "vongquay" => $vongquay,
        "amount_mtv" => $amount_mtv,
        "thongbao" => $thongbao,
        "nd_thongbao" => $nd_thongbao
    );
    $where = 'id = 1';


    $table = "cvh_setting";
    $CVH->update($table, $data, $where);
    $CVH->Ex(true, "Cập nhật dữ liệu thành công!");
} else {
    $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");
}

//$CVH->Ex(false, "Sửa con cặc!");
?>