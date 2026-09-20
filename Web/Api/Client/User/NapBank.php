<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    $CVH->Ex(false, "method not allow!");
    exit;
}

// Kiểm tra biến môi trường PayOS
if (!isset($PAYOS_CLIENT_ID) || !isset($PAYOS_API_KEY) || !isset($PAYOS_SECRET_KEY)) {
    echo json_encode(['success' => false, 'message' => 'Thiếu cấu hình PayOS']);
    exit;
}

// Kiểm tra tham số
if (!isset($_POST['amount']) || !isset($user) || !isset($user['username'])) {
    $CVH->Ex(false, "Thiếu tham số!");
    exit;
}

$amount = intval($_POST['amount']);
$username = $user['username'];

// Validate amount (đơn vị VND, 1000 VND = 1 ngọc)
$allowed_amounts = [10000, 20000, 50000, 100000, 200000, 500000, 1000000];
if (!in_array($amount, $allowed_amounts)) {
    $CVH->Ex(false, "Gói nạp không hợp lệ!");
    exit;
}

try {
    // Tạo order code unique
    $orderCode = intval(microtime(true)) % 100000000;
    
    // PayOS yêu cầu expiredAt theo Unix time (seconds) và phải <= 2147483647.
    $now = time();
    $expiredAt = $now + (30 * 60); // +30 phút để tránh lệch giờ nhỏ giữa client/server
    if ($expiredAt > 2147483647) {
        $expiredAt = 2147483647;
    }
    
    // Tạo payment data
    $paymentData = [
        'orderCode' => $orderCode,
        'amount' => $amount,
        'description' => 'Nap User ' . $username,
        'returnUrl' => FULL_URL('/Success'),
        'cancelUrl' => FULL_URL('/Cancel'),
        'buyerName' => $username,
        'expiredAt' => $expiredAt
    ];
    
    // Tạo chữ ký đúng chuẩn PayOS (không bao gồm signature trong data)
    $dataForSignature = "amount={$amount}&cancelUrl={$paymentData['cancelUrl']}&description={$paymentData['description']}&orderCode={$orderCode}&returnUrl={$paymentData['returnUrl']}";
    $signature = hash_hmac('sha256', $dataForSignature, $PAYOS_SECRET_KEY);
    
    // Thêm signature vào paymentData
    $paymentData['signature'] = $signature;
    
    // Log để debug
    error_log("PayOS Request Data: " . json_encode($paymentData, JSON_UNESCAPED_UNICODE));
    error_log("Signature Data: " . $dataForSignature);
    error_log("Signature: " . $signature);
    error_log("Expired At (s): " . $expiredAt);
    error_log("Expired At (date): " . date('Y-m-d H:i:s', $expiredAt));
    
    // Gọi API payOS
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://api-merchant.payos.vn/v2/payment-requests');
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($paymentData));
    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'Content-Type: application/json',
        'x-client-id: ' . $PAYOS_CLIENT_ID,
        'x-api-key: ' . $PAYOS_API_KEY
    ]);
    curl_setopt($ch, CURLOPT_TIMEOUT, 30);
    
    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $curlError = curl_error($ch);
    curl_close($ch);
    
    // Debug: Ghi log response
    error_log("PayOS Response HTTP Code: " . $httpCode);
    error_log("PayOS Response: " . $response);
    
    if ($curlError) {
        throw new Exception('Lỗi curl: ' . $curlError);
    }
    
    if ($httpCode === 200) {
        $responseData = json_decode($response, true);
        
        // Kiểm tra cấu trúc response
        if (!isset($responseData['data']) || !isset($responseData['data']['checkoutUrl'])) {
            throw new Exception('PayOS trả về response không hợp lệ: ' . $response);
        }
        
        // CHỈ KHI NÀO PayOS trả về thành công thì mới lưu vào database
        $conn = new mysqli($DB['SERVER'], $DB['USERNAME'], $DB['PASSWORD'], $DB['TABLE']);
        
        if ($conn->connect_error) {
            throw new Exception("Database connection failed: " . $conn->connect_error);
        }
        
        $stmt = $conn->prepare("INSERT INTO payments (orderCode, username, amount, status, created_at) VALUES (?, ?, ?, 'pending', NOW())");
        
        if (!$stmt) {
            throw new Exception("Prepare failed: " . $conn->error);
        }
        
        $stmt->bind_param("isi", $orderCode, $username, $amount);
        
        if (!$stmt->execute()) {
            throw new Exception("Execute failed: " . $stmt->error);
        }
        
        $stmt->close();
        $conn->close();
        
        $payData = $responseData['data'];
        $checkoutUrl = isset($payData['checkoutUrl']) ? $payData['checkoutUrl'] : '';
        $qrPayload = isset($payData['qrCode']) ? $payData['qrCode'] : '';
        $qrImageUrl = '';
        if (!empty($qrPayload)) {
            $qrImageUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=' . rawurlencode($qrPayload);
        } elseif (!empty($checkoutUrl)) {
            $qrImageUrl = 'https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=' . rawurlencode($checkoutUrl);
        }

        // Trả về payment URL + metadata để frontend mở popup QR và theo dõi trạng thái realtime.
        echo json_encode([
            'status' => true,
            'success' => true, 
            'message' => 'Tạo giao dịch thành công',
            'paymentUrl' => $checkoutUrl,
            'orderCode' => $orderCode,
            'amount' => $amount,
            'description' => $paymentData['description'],
            'expiredAt' => $expiredAt,
            'status' => 'pending',
            'qrCode' => $qrPayload,
            'qrImageUrl' => $qrImageUrl,
            'accountName' => isset($payData['accountName']) ? $payData['accountName'] : '',
            'accountNumber' => isset($payData['accountNumber']) ? $payData['accountNumber'] : ''
        ]);
    } else {
        // Phân tích lỗi chi tiết từ PayOS
        $errorResponse = json_decode($response, true);
        $errorMsg = 'Lỗi không xác định từ PayOS';
        
        if (isset($errorResponse['desc'])) {
            $errorMsg = $errorResponse['desc'];
        } elseif (isset($errorResponse['message'])) {
            $errorMsg = $errorResponse['message'];
        }
        
        throw new Exception('Lỗi PayOS (' . $httpCode . '): ' . $errorMsg);
    }
    
} catch (Exception $e) {
    error_log("Payment error: " . $e->getMessage());
    echo json_encode(['success' => false, 'message' => 'Có lỗi xảy ra khi tạo giao dịch: ' . $e->getMessage()]);
}
?>