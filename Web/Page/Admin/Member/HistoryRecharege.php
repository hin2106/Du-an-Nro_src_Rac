<?php
$kmess = 16;
$page  = isset($_REQUEST['page']) && $_REQUEST['page'] > 0 ? intval($_REQUEST['page']) : 1;
$start = ($page - 1) * $kmess;

$db    = $CVH->connect_db();
$result = $db->query("SELECT * FROM payments ORDER BY created_at DESC LIMIT $start, $kmess");
$total_row = $db->query("SELECT COUNT(*) AS cnt FROM payments");
$tong  = $total_row ? (int)$total_row->fetch_assoc()['cnt'] : 0;

function getPaymentStatus($status) {
    switch ($status) {
        case 'pending':  return '<span class="badge bg-light-warning text-warning">Chờ thanh toán</span>';
        case 'paid':     return '<span class="badge bg-light-success text-success">Thành công</span>';
        case 'failed':   return '<span class="badge bg-light-danger text-danger">Thất bại</span>';
        case 'cancelled':return '<span class="badge bg-light-secondary text-secondary">Đã huỷ</span>';
        default:         return '<span class="badge bg-light-secondary text-secondary">' . htmlspecialchars($status) . '</span>';
    }
}
?>
<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="card card-body">
            <div class="table-responsive">
                <table class="table search-table align-middle text-nowrap">
                    <thead class="header-item">
                        <tr>
                            <th>ID</th>
                            <th>TÀI KHOẢN</th>
                            <th>SỐ TIỀN (VND)</th>
                            <th>NGỌC NHẬN</th>
                            <th>MÃ ĐƠN</th>
                            <th>TRẠNG THÁI</th>
                            <th>THỜI GIAN</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if ($result && $result->num_rows > 0): while ($row = $result->fetch_assoc()): ?>
                        <tr class="search-items">
                            <td><?php echo $row['id']; ?></td>
                            <td><b><?php echo htmlspecialchars($row['username']); ?></b></td>
                            <td><?php echo number_format($row['amount']); ?>đ</td>
                            <td><b class="text-success"><?php echo number_format(intval($row['amount'] / 1000)); ?> ngọc</b></td>
                            <td><code><?php echo htmlspecialchars($row['orderCode']); ?></code></td>
                            <td><?php echo getPaymentStatus($row['status']); ?></td>
                            <td><?php echo $row['created_at']; ?></td>
                        </tr>
                        <?php endwhile; else: ?>
                        <tr class="text-center">
                            <td colspan="7">
                                <img src="https://cdn-icons-png.flaticon.com/128/7466/7466139.png" width="50" class="img-fluid">
                                <p class="pt-3"><b>Không có dữ liệu</b></p>
                            </td>
                        </tr>
                        <?php endif; ?>
                    </tbody>
                </table>
            </div>
            <?php if ($tong > $kmess): ?>
                <center><?php echo $CVH->phantrang('/admin/history/recharge/', $start, $tong, $kmess); ?></center>
            <?php endif; ?>
        </div>
    </div>
</div>
</div>