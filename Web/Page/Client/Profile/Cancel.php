<?php
if (!$user) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
?>
<Center>
	<div class="body-wrapper">
		<div class="container-fluid">
			<div class="col-lg-4">
				<div class="card w-100">
					<div class="card-body text-center">
						<!-- Thêm icon X đỏ -->
						<div style="font-size: 80px; color: #dc3545; margin-bottom: 20px;">
							<i class="fas fa-times-circle"></i>
						</div>
						<b>
						<h5 style="color: #dc3545;">Đã hủy nạp ngọc</h5>
						</b>
						<p>Bạn đã hủy giao dịch nạp ngọc.</p>
						<a class="btn btn-primary" href="<?php echo FULL_URL('/nap-bank') ?>">Thử lại</a>
						<a class="btn btn-secondary" href="<?php echo FULL_URL('/') ?>">Về trang chủ</a>
					</div>
				</div>
			</div>
		</div>
	</div>
</Center>