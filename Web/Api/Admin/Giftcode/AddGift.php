<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

if (
    isset($_POST['code'], $_POST['count'], $_POST['expired'], $_POST['items']) &&
    $_POST['code'] !== '' &&
    $_POST['count'] !== '' &&
    $_POST['expired'] !== ''
) {
    if (isset($_SESSION['admin']) && !empty($user['is_admin'])) {
        $code = trim((string)$_POST['code']);
        $count = abs((int)$_POST['count']);
        $expiredInput = (string)$_POST['expired'];

        // Chuẩn hóa datetime-local (YYYY-MM-DDTHH:MM) về định dạng MySQL.
        $expiredTs = strtotime(str_replace('T', ' ', $expiredInput));
        if ($expiredTs === false) {
            $CVH->Ex(false, "Thời gian hết hạn không hợp lệ!");
            return;
        }
        $expired = date('Y-m-d H:i:s', $expiredTs);

        // Xử lý mảng vật phẩm
        $detailArray = array();
        foreach ($_POST['items'] as $itemData) {
            if (!isset($itemData['item_id'], $itemData['quantity'])) {
                continue;
            }

            $itemId = abs((int)$itemData['item_id']);
            $quantity = abs((int)$itemData['quantity']);
            if ($itemId <= 0 || $quantity <= 0) {
                continue;
            }

            $optionsArray = array();
            if (isset($itemData['options']) && is_array($itemData['options'])) {
                foreach ($itemData['options'] as $optionData) {
                    if (!isset($optionData['id']) || $optionData['id'] === '') {
                        continue;
                    }

                    $optionsArray[] = array(
                        "id" => abs((int)$optionData['id']),
                        "param" => isset($optionData['param']) ? (int)$optionData['param'] : 0
                    );
                }
            }

            if (empty($optionsArray)) {
                $optionsArray = array(array("id" => 30, "param" => 0));
            }

            $detailArray[] = array(
                "id" => $itemId,
                "quantity" => $quantity,
                "options" => $optionsArray
            );
        }

        if (empty($detailArray)) {
            $CVH->Ex(false, "Vui lòng thêm ít nhất một vật phẩm hợp lệ!");
            return;
        }

        if ($code === 'rd') {
            $code = rand_string(6);
        }

        $db = $CVH->connect_db();
        $giftcodeTable = mysqli_query($db, "SHOW TABLES LIKE 'giftcode'");
        $legacyTable = mysqli_query($db, "SHOW TABLES LIKE 'cvh_giftcode'");

        if ($giftcodeTable && mysqli_num_rows($giftcodeTable) > 0) {
            $data = array(
                "code" => $code,
                "count_left" => $count,
                "detail" => json_encode($detailArray),
                "expired" => $expired
            );

            if ($CVH->insert("giftcode", $data)) {
                $CVH->Ex(true, "Thêm gift code " . $code . " thành công!");
                return;
            }

            $CVH->Ex(false, "Không thể thêm giftcode vào bảng giftcode!");
            return;
        }

        if ($legacyTable && mysqli_num_rows($legacyTable) > 0) {
            $legacyItems = array();
            $legacyOptions = array();

            foreach ($detailArray as $giftItem) {
                $legacyItems[] = array(
                    "id" => (string)$giftItem['id'],
                    "soluong" => (string)$giftItem['quantity']
                );

                if (isset($giftItem['options']) && is_array($giftItem['options'])) {
                    foreach ($giftItem['options'] as $opt) {
                        $legacyOptions[] = array(
                            "id" => (int)$opt['id'],
                            "param" => (int)$opt['param']
                        );
                    }
                }
            }

            if (empty($legacyOptions)) {
                $legacyOptions[] = array("id" => 30, "param" => 0);
            }

            $legacyData = array(
                "code" => $code,
                "luot" => $count,
                "item" => json_encode($legacyItems),
                "option" => json_encode($legacyOptions),
                "status" => '1',
                "hsd" => date('Y-m-d', $expiredTs),
                "time" => time()
            );

            if ($CVH->insert("cvh_giftcode", $legacyData)) {
                $CVH->Ex(true, "Thêm gift code " . $code . " thành công!");
                return;
            }

            $CVH->Ex(false, "Không thể thêm giftcode vào bảng cvh_giftcode!");
            return;
        }

        $CVH->Ex(false, "Không tìm thấy bảng giftcode phù hợp trong database!");
    } else {
        $CVH->Ex(false, "Bạn không có quyền thực hiện thao tác này!");
    }
} else {
    $CVH->Ex(false, "Vui lòng nhập đầy đủ thông tin!");
}
?>