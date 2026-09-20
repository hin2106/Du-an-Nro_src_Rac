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
						<!-- Thêm icon tích xanh -->
						<div style="font-size: 80px; color: #28a745; margin-bottom: 20px;">
							<i class="fas fa-check-circle"></i>
						</div>
						<b>
						<h5 style="color: #28a745;">Nạp ngọc thành công</h5>
						</b>
						<p>Giao dịch nạp ngọc đã được xử lý. Ngọc sẽ được cộng vào game trong vài giây.</p>
						<a class="btn btn-primary" href="<?php echo FULL_URL('/nap-bank') ?>">Nạp thêm</a>
						<a class="btn btn-secondary" href="<?php echo FULL_URL('/') ?>">Về trang chủ</a>
					</div>
				</div>
			</div>
		</div>
	</div>
</Center>