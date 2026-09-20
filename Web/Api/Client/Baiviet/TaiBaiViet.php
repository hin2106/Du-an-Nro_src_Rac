<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

$page = isset($_POST['page']) ? $_POST['page'] : 1;
$postsPerPage = 9;
$startFrom = ($page - 1) * $postsPerPage;
$limit = $postsPerPage;

$row = $CVH->get_list("SELECT cvh_baiviet.id, cvh_baiviet.title, cvh_baiviet.likes, cvh_baiviet.content, cvh_baiviet.time, 
                      account.username, player.head, player.name
                      FROM cvh_baiviet
                      LEFT JOIN account ON cvh_baiviet.poster = account.id
                      LEFT JOIN player ON cvh_baiviet.poster = player.account_id
                      WHERE cvh_baiviet.status = 1
                      AND cvh_baiviet.role = 1
                      ORDER BY cvh_baiviet.time DESC
                      LIMIT $limit OFFSET $startFrom");

foreach ($row as &$post) {
    $post['likes'] = $CVH->getTotalLikes($post['id']);
}


$totalPosts = $CVH->get_value("SELECT COUNT(*) FROM cvh_baiviet");
$totalPages = ceil($totalPosts / $postsPerPage);
$data = array(
    'current_page' => $page,
    'total_pages' => $totalPages,
    'data' => $row
);
header('Content-Type: application/json');
echo json_encode($data);
?>