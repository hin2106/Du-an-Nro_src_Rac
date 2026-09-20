<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
if ($user) {
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $postId = $_POST['postId'];
        $action = $_POST['action'];


        if ($action === 'like') {
            $CVH->addLike($postId, $user["username"]);
        } elseif ($action === 'unlike') {
            $CVH->unLike($postId, $user["username"]);
        }
        $totalLikes = $CVH->getTotalLikes($postId);

        $response = array(
            "status" => true,
            "message" => "Thao tác thành công",
            "total" => $totalLikes
        );
        echo json_encode($response);
    } else {
        $CVH->Ex(false, "Thao tác thất bại");
    }
} else {
    $CVH->Ex(false, "Thao tác thất bại");
}
?>