<?php
http_response_code(401);
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
?>

<Center>
	<div class="body-wrapper">
		<div class="container-fluid">
			<div class="col-lg-4">
				<div class="card w-100">
					<div class="card-body">
						<!-- Icon khóa -->
						<div style="text-align: center; margin-bottom: 20px;">
							<svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
								<circle cx="12" cy="12" r="10" fill="#6c757d"/>
								<rect x="8" y="10" width="8" height="8" rx="1" fill="white"/>
								<path d="M12 14V16" stroke="#6c757d" stroke-width="2" stroke-linecap="round"/>
								<path d="M8 10V8C8 5.79086 9.79086 4 12 4C14.2091 4 16 5.79086 16 8V10" stroke="white" stroke-width="2"/>
							</svg>
						</div>
						<b>
						<h5 style="color: #6c757d;">Chưa xác thực</h5>
						</b>
						<p>Bạn cần đăng nhập để truy cập trang này.</p>
						<button class="btn btn-secondary w-100" onclick="window.location.href='<?php echo FULL_URL('/dang-nhap') ?>'">Đăng nhập</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</Center>