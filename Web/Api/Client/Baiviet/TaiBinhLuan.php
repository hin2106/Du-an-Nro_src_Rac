<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

$postId = $_POST['postId'];
$page = max(1, $_POST['page']);
$num_show = $_POST['num_show'];

$sql = "SELECT comments, poster FROM cvh_baiviet WHERE id = $postId";
$result = $CVH->query($sql);
$row = $result->fetch_assoc();

$commentsArray = json_decode($row["comments"], true);

$start = ($page - 1) * $num_show;
$pagedComments = array_slice($commentsArray, $start, $num_show);

foreach ($pagedComments as &$comment) {
    $playerData = $CVH->player($comment["account_id"]);
    $comment["head"] = $playerData["head"];
    $comment["name"] = $playerData["name"];
}

echo json_encode($pagedComments);
?>