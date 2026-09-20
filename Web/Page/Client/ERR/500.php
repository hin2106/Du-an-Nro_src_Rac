<?php
http_response_code(500);
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
?>
<Center>
	<div class="body-wrapper">
		<div class="container-fluid">
			<div class="col-lg-4">
				<div class="card w-100">
					<div class="card-body">
						<!-- Icon lỗi máy chủ -->
						<div style="text-align: center; margin-bottom: 20px;">
							<svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
								<circle cx="12" cy="12" r="10" fill="#dc3545"/>
								<path d="M12 8V12M12 16H12.01" stroke="white" stroke-width="2" stroke-linecap="round"/>
							</svg>
						</div>
						<b>
						<h5 style="color: #dc3545;">Lỗi máy chủ</h5>
						</b>
						<p>Đã xảy ra lỗi trong quá trình xử lý. Vui lòng thử lại sau.</p>
						<button class="btn btn-danger w-100" onclick="window.location.href='<?php echo FULL_URL('/') ?>'">Về trang chủ</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</Center>