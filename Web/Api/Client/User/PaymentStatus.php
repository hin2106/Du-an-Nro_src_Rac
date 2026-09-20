<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

if (!isset($user) || !isset($user['username'])) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Bạn chưa đăng nhập']);
    exit;
}

$orderCode = isset($_POST['orderCode']) ? intval($_POST['orderCode']) : 0;
if ($orderCode <= 0) {
    echo json_encode(['success' => false, 'message' => 'orderCode không hợp lệ']);
    exit;
}

$conn = new mysqli($DB['SERVER'], $DB['USERNAME'], $DB['PASSWORD'], $DB['TABLE']);
if ($conn->connect_error) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Không thể kết nối database']);
    exit;
}

$username = $user['username'];
$stmt = $conn->prepare("SELECT orderCode, amount, status, created_at, paid_at FROM payments WHERE orderCode = ? AND username = ? LIMIT 1");
if (!$stmt) {
    $conn->close();
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Không thể truy vấn trạng thái']);
    exit;
}

$stmt->bind_param("is", $orderCode, $username);
$stmt->execute();
$result = $stmt->get_result();
$row = $result ? $result->fetch_assoc() : null;
$stmt->close();
$conn->close();

if (!$row) {
    echo json_encode(['success' => false, 'message' => 'Không tìm thấy giao dịch']);
    exit;
}

$status = strtolower((string)$row['status']);
$createdAt = strtotime((string)$row['created_at']);
$expiresAt = ($createdAt ?: time()) + (15 * 60);
$remainingSeconds = max(0, $expiresAt - time());

if ($status === 'pending' && $remainingSeconds <= 0) {
    $status = 'expired';
}

echo json_encode([
    'success' => true,
    'orderCode' => intval($row['orderCode']),
    'amount' => intval($row['amount']),
    'status' => $status,
    'paid' => ($status === 'paid'),
    'createdAt' => $row['created_at'],
    'paidAt' => $row['paid_at'],
    'expiresAt' => date('Y-m-d H:i:s', $expiresAt),
    'remainingSeconds' => $remainingSeconds
]);
