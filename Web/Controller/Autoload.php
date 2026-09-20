<?php


require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/RequestGuard.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Setting.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Database.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Core.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/hide_err.php";
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/PayosConfig.php";

if (PHP_VERSION_ID >= 70300) {
    session_set_cookie_params([
        'lifetime' => 0,
        'path' => '/',
        'secure' => !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off',
        'httponly' => true,
        'samesite' => 'Lax',
    ]);
}
session_start();
$CVH = new System;
$setting = $CVH->setting(1);
if (isset($_COOKIE['token'])) {
    $token = $_COOKIE['token'];
    $user = $CVH->Check($token);
    $tokenz = $CVH->Token($user['username'], $user['password']);

    if ($token == $tokenz) {

        $user = $CVH->Check($token);
        if ($CVH->player($user['id'])) {

            $player = $CVH->player($user['id']);
        } else {
            $player = null;
        }

    } else {

        unset($_COOKIE['token']);
        setcookie('token', '', time() - (7 * 24 * 60 * 60), '/');
        $user = null;
    }
} else {
    $user = null;
}

function RandomString($data)
{
    $String = $data[array_rand($data)];
    return $String;
}

function FULL_URL($path)
{
    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? 'https' : 'http';
    $host = $_SERVER['HTTP_HOST'];
    return $protocol . "://" . $host . $path;
}


function getCurrentURL()
{
    $protocol = isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] === 'on' ? "https" : "http";
    $host = $_SERVER['HTTP_HOST'];
    $uri = $_SERVER['REQUEST_URI'];

    $url = $protocol . "://" . $host . $uri;

    return $url;
}

function checkGender($genderCode)
{
    switch ($genderCode) {
        case 0:
            return "Trái Đất";
        case 1:
            return "Namec";
        case 2:
            return "Xayda";
        case 3:
            return "Null";
        default:
            return "Không xác định";
    }
}

function checkAmount($amount_real)
{
    switch ($amount_real) {
        case -1:
            return "Chưa cập nhật";
        default:
            return number_format($amount_real) . "đ";
    }
}

function getStatus($stt)
{
    switch ($stt) {
        case 0:
            return '<span class="badge bg-light-primary text-primary">Chờ Duyệt</span>';
        case 1:
            return '<span class="badge bg-light-success text-success">Thành Công</span>';
        case 2:
            return '<span class="badge bg-light-danger text-danger">Thẻ Sai</span>';
        case 3:
            return '<span class="badge bg-light-warning text-warning">Sai Mệnh Giá</span>';
        default:
            return '<span class="badge bg-light-secondary text-secondary">Chưa Xác Định</span>';
    }
}

function getBand($stt)
{
    switch ($stt) {
        case 0:
            return '<span class="badge bg-success">Đang mở</span>';
        case 1:
            return '<span class="badge bg-danger">Đã khóa</span>';
        default:
            return '<span class="badge bg-secondary">Chưa Xác Định</span>';
    }
}


function getActive($stt)
{
    switch ($stt) {
        case 0:
            return '<span class="badge bg-danger">Chưa kích hoạt</span>';
        case 1:
            return '<span class="badge bg-success">Đã kích hoạt</span>';
        default:
            return '<span class="badge bg-secondary">Chưa Xác Định</span>';
    }
}

function fNumber($value)
{
    if ($value > 1000000000) {
        return number_format($value / 1000000000, 1, '.', '') . 'tỷ';
    } elseif ($value > 1000000) {
        return number_format($value / 1000000, 1, '.', '') . 'triệu';
    } elseif ($value >= 1000) {
        return number_format($value / 1000, 1, '.', '') . 'k';
    } else {
        return number_format($value, 0, '.', ',');
    }
}

function sendTele($message)
{

    $tele_token = "6489628478:AAFf6SaDrpEcNLSTi2ODsjqUl2v9tjI4MXE";
    $tele_chatid = "5031862312";

    $data = http_build_query([
        'chat_id' => $tele_chatid,
        'text' => $message,
    ]);

    $url = 'https://api.telegram.org/bot' . $tele_token . '/sendMessage';

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_REFERER, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_USERAGENT, 'Mozilla/5.0 (Win) AppleWebKit/1000.0 (KHTML, like Gecko) Chrome/65.663 Safari/1000.01');
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    if ($data) {
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
    }
    $result = curl_exec($ch);
    curl_close($ch);
    return $result;
}

function templateTele($content)
{
    return "🔔 THÔNG BÁO\n📝 Nội dung: " . $content . "\n🕒 Thời gian: " . date('d/m/Y H:i:s');
}


function loc($string)
{
    // Chuyển tất cả các từ ngữ không phù hợp thành dấu "*"
    $text = array("lồn", "cặc", "địt", "dm", "dmm", "đm", "mẹ", "bố", "cha", "như lồn", "như cc", "game cc", "admin cc", "ngu");
    $ketqua = str_replace($text, str_repeat('*', strlen($text[0])), $string);

    return $ketqua;
}

function extractImageSrc($input)
{
    preg_match_all('/<img[^>]+>/i', $input, $matches);

    $imageSrcs = array();
    foreach ($matches[0] as $imgTag) {
        preg_match('/src="([^"]+)"/i', $imgTag, $src);
        if (isset($src[1])) {
            $imageSrcs[] = $src[1];
        }
    }

    return $imageSrcs;
}

function getAdminPosts($chose)
{
    switch ($chose) {
        default:
            $if_admin = array(
                "name" => "ADMIN",
                "avatar" => "/assets/images/avatar/admin/".$chose.".png"
            );
            return json_encode($if_admin);
    }
}

function rand_string($length = 10) {
    $characters = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    $charactersLength = strlen($characters);
    $randomString = '';
    for ($i = 0; $i < $length; $i++) {
        $randomString .= $characters[rand(0, $charactersLength - 1)];
    }
    return $randomString;
}
?>
