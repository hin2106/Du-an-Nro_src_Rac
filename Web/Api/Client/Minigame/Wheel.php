<?php
header('Content-Type: application/json');
$typeRoll = $_POST['typeRoll'];
$data = array(
    "msg" => array(
        "name" => "Bạn đã trúng 600",
        "pos" => rand(1,8),
        "locale" => "0",
        "num_roll_remain" => "1",
        "image" => null
    ),
    "typeRoll" => "Quay Thưởng",
    "arr_gift" => array(
        array("title" => "Bạn đã trúng 800"),
        array("title" => "Bạn đã trúng 400"),
        array("title" => "Bạn đã trúng 400"),
        array("title" => "Bạn đã trúng 400"),
        array("title" => "Bạn đã trúng 600"),
        array("title" => "Bạn đã trúng 400"),
        array("title" => "Bạn đã trúng 600"),
        array("title" => "Bạn đã trúng 600"),
        array("title" => "Bạn đã trúng 600"),
        array("title" => "Bạn đã trúng 600")
    ),
    "total" => "5,400",
    "price" => "200,000"
);
echo json_encode($data);
?>