<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

// ──────────────────────────────────────────────────────────────────────────────
// PayOS Webhook handler
// Luồng: PayOS POST → xác thực chữ ký HMAC-SHA256 → SELECT FOR UPDATE
//        → nếu game server đang chạy thì luôn insert pending_rewards (Java tự quyết online/offline)
//        → nếu game server không chạy thì cộng trực tiếp vào player.data_inventory + account.napngoc
// ──────────────────────────────────────────────────────────────────────────────

function writePayOSLog($data, $type = 'INFO') {
    $logDir = $_SERVER['DOCUMENT_ROOT'] . '/logs/payos/';
    if (!is_dir($logDir)) {
        mkdir($logDir, 0777, true);
    }
    $logFile   = $logDir . 'webhook_' . date('Y-m-d') . '.log';
    $timestamp = date('Y-m-d H:i:s');
    $ip        = $_SERVER['REMOTE_ADDR'] ?? 'UNKNOWN';
    file_put_contents($logFile, "[$timestamp] [$type] [IP: $ip] $data" . PHP_EOL, FILE_APPEND | LOCK_EX);
}

function isGameServerRunning($conn, $ttlSeconds = 25) {
    $serviceName = 'WebRewardService';
    $stmt = $conn->prepare("SELECT UNIX_TIMESTAMP(last_heartbeat) AS hb FROM game_server_status WHERE service_name = ? LIMIT 1");
    if (!$stmt) {
        return false;
    }
    $stmt->bind_param("s", $serviceName);
    $stmt->execute();
    $result = $stmt->get_result();
    $stmt->close();
    if (!$result || $result->num_rows === 0) {
        return false;
    }
    $row = $result->fetch_assoc();
    $hb = isset($row['hb']) ? (int) $row['hb'] : 0;
    if ($hb <= 0) {
        return false;
    }
    return (time() - $hb) <= $ttlSeconds;
}

$input       = file_get_contents('php://input');
$webhookData = json_decode($input, true);

writePayOSLog("=== WEBHOOK RECEIVED ===");
writePayOSLog($input);

if (!$webhookData) {
    writePayOSLog("Invalid JSON", 'ERROR');
    http_response_code(400);
    echo json_encode(['error' => 1, 'message' => 'Invalid data']);
    exit;
}

try {
    // ── 1. Xác thực chữ ký PayOS (HMAC-SHA256) ──────────────────────────────
    // PayOS ký toàn bộ fields trong data[], sort theo alphabet, nối key=value&...
    if (!isset($webhookData['signature']) || !isset($webhookData['data'])) {
        writePayOSLog("Missing signature or data field", 'ERROR');
        http_response_code(400);
        echo json_encode(['error' => 1, 'message' => 'Missing signature']);
        exit;
    }

    $dataObj = $webhookData['data'];
    ksort($dataObj);
    $parts = [];
    foreach ($dataObj as $key => $value) {
        $parts[] = $key . '=' . $value;
    }
    $expectedSig = hash_hmac('sha256', implode('&', $parts), $PAYOS_SECRET_KEY);

    if (!hash_equals($expectedSig, $webhookData['signature'])) {
        writePayOSLog("INVALID SIGNATURE - expected: $expectedSig, got: " . $webhookData['signature'], 'ERROR');
        http_response_code(400);
        echo json_encode(['error' => 1, 'message' => 'Invalid signature']);
        exit;
    }
    writePayOSLog("Signature OK");

    // ── 2. Kiểm tra cấu trúc dữ liệu ────────────────────────────────────────
    if (!isset($dataObj['orderCode'])) {
        throw new Exception("Missing orderCode in webhook data");
    }

    $data      = $dataObj;
    $orderCode = $data['orderCode'];

    writePayOSLog("orderCode=$orderCode | code=" . ($data['code'] ?? 'N/A') .
                  " | desc=" . ($data['desc'] ?? 'N/A') .
                  " | amount=" . ($data['amount'] ?? 'N/A'));

    // ── 3. Xác định giao dịch thành công ─────────────────────────────────────
    $isSuccess = (
        (isset($data['code']) && $data['code'] === '00' &&
         isset($data['desc']) && $data['desc'] === 'success')
        ||
        (isset($webhookData['success']) && $webhookData['success'] === true)
    );
    writePayOSLog("isSuccess=" . ($isSuccess ? 'true' : 'false'));

    // ── 4. Kết nối database ──────────────────────────────────────────────────
    $conn = new mysqli($DB['SERVER'], $DB['USERNAME'], $DB['PASSWORD'], $DB['TABLE']);
    if ($conn->connect_error) {
        throw new Exception("DB connect failed: " . $conn->connect_error);
    }

    if (!$isSuccess) {
        writePayOSLog("Payment not successful, skipping orderCode: $orderCode");
        $conn->close();
        echo json_encode(['error' => 0, 'message' => 'OK']);
        exit;
    }

    // ── 5. Xử lý thanh toán thành công ──────────────────────────────────────
    // Dùng transaction + SELECT FOR UPDATE để ngăn race condition:
    // nếu PayOS gửi lại webhook, request thứ 2 chờ lock và thấy status='paid' => bỏ qua
    $conn->begin_transaction();
    try {
        $stmt = $conn->prepare(
            "SELECT username, amount FROM payments WHERE orderCode = ? AND status = 'pending' FOR UPDATE"
        );
        if (!$stmt) throw new Exception("Prepare SELECT failed: " . $conn->error);
        $stmt->bind_param("i", $orderCode);
        $stmt->execute();
        $result = $stmt->get_result();
        $stmt->close();

        writePayOSLog("Pending rows: " . $result->num_rows);

        if ($result->num_rows === 0) {
            $conn->commit(); // giải phóng lock
            writePayOSLog("orderCode $orderCode already processed or not found", 'WARNING');

        } else {
            $payment  = $result->fetch_assoc();
            $username = $payment['username'];
            $amount   = (int) $payment['amount'];

            // 1000 VND = 1 ngọc (player.inventory.gem / data_inventory[1])
            $gems = max(1, intval($amount / 1000));
            writePayOSLog("Username=$username | Amount={$amount}đ | Gems=$gems ngọc");

            // Lấy account_id.
            $su = $conn->prepare("SELECT id FROM account WHERE username = ? LIMIT 1");
            if (!$su) throw new Exception("Prepare account lookup failed: " . $conn->error);
            $su->bind_param("s", $username);
            $su->execute();
            $ur = $su->get_result();
            $su->close();
            if ($ur->num_rows === 0) throw new Exception("User not found: $username");
            $account_id = (int) $ur->fetch_assoc()['id'];
            writePayOSLog("account_id=$account_id");

            $gameServerRunning = isGameServerRunning($conn, 25);
            writePayOSLog("gameServerRunning=" . ($gameServerRunning ? 'true' : 'false'));

            // Ghi lịch sử — Java set status='successful' sau khi xử lý xong
            $ih = $conn->prepare(
                "INSERT INTO lichsu_napngoc (username, cash_spent, ngoc_received, created_at, status)
                 VALUES (?, ?, ?, NOW(), 'processing')"
            );
            if (!$ih) throw new Exception("Prepare lichsu insert failed: " . $conn->error);
            $ih->bind_param("sii", $username, $amount, $gems);
            $ih->execute();
            $history_id = (int) $conn->insert_id;
            $ih->close();

            if ($gameServerRunning) {
                // Khi game server chạy: luôn để Java xử lý để xác định online/offline tuyệt đối.
                $ir = $conn->prepare(
                    "INSERT INTO pending_rewards (account_id, item_id, quantity, processed, history_id)
                     VALUES (?, 0, ?, 0, ?)"
                );
                if (!$ir) throw new Exception("Prepare pending_rewards insert failed: " . $conn->error);
                $ir->bind_param("iii", $account_id, $gems, $history_id);
                $ir->execute();
                $ir->close();

                // Cập nhật tongnap VND cho thống kê doanh thu.
                // account.napngoc sẽ do WebRewardService cập nhật khi xử lý pending_rewards.
                $ut = $conn->prepare("UPDATE account SET tongnap = tongnap + ? WHERE id = ?");
                if (!$ut) throw new Exception("Prepare tongnap update failed: " . $conn->error);
                $ut->bind_param("ii", $amount, $account_id);
                $ut->execute();
                $ut->close();

                writePayOSLog("queued pending_rewards for game-server flow: account_id=$account_id");
            } else {
                // Game server không chạy: cộng trực tiếp vào DB nhân vật.
                $upPlayer = $conn->prepare(
                    "UPDATE player
                     SET data_inventory = JSON_SET(
                        data_inventory,
                        '$[1]',
                        CAST(JSON_UNQUOTE(JSON_EXTRACT(data_inventory, '$[1]')) AS UNSIGNED) + ?
                     )
                     WHERE account_id = ?"
                );
                if (!$upPlayer) throw new Exception("Prepare player gem update failed: " . $conn->error);
                $upPlayer->bind_param("ii", $gems, $account_id);
                $upPlayer->execute();
                $upPlayer->close();

                $ua = $conn->prepare("UPDATE account SET napngoc = napngoc + ?, tongnap = tongnap + ? WHERE id = ?");
                if (!$ua) throw new Exception("Prepare account update failed: " . $conn->error);
                $ua->bind_param("iii", $gems, $amount, $account_id);
                $ua->execute();
                $ua->close();

                $uh = $conn->prepare("UPDATE lichsu_napngoc SET status = 'successful' WHERE id = ?");
                if (!$uh) throw new Exception("Prepare history update failed: " . $conn->error);
                $uh->bind_param("i", $history_id);
                $uh->execute();
                $uh->close();

                writePayOSLog("direct grant applied for offline user: account_id=$account_id, gems=$gems");
            }

            // Đánh dấu paid để tránh webhook trùng lặp
            $up = $conn->prepare("UPDATE payments SET status = 'paid', paid_at = NOW() WHERE orderCode = ?");
            if (!$up) throw new Exception("Prepare payment update failed: " . $conn->error);
            $up->bind_param("i", $orderCode);
            $up->execute();
            $up->close();

            $conn->commit();
            writePayOSLog("SUCCESS: queued $gems ngọc for $username (order=$orderCode, history=$history_id)");
        }

    } catch (Exception $e) {
        $conn->rollback();
        writePayOSLog("Transaction rollback: " . $e->getMessage(), 'ERROR');
        throw $e;
    }

} catch (Exception $e) {
    writePayOSLog("Exception: " . $e->getMessage(), 'ERROR');
    http_response_code(500);
    echo json_encode(['error' => 1, 'message' => 'Internal Server Error']);
} finally {
    if (isset($conn) && $conn instanceof mysqli && $conn->ping()) {
        $conn->close();
    }
}
?>
