<?php
http_response_code(403);
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
?>
<Center>
	<div class="body-wrapper">
		<div class="container-fluid">
			<div class="col-lg-4">
				<div class="card w-100">
					<div class="card-body">
						<!-- Icon cấm -->
						<div style="text-align: center; margin-bottom: 20px;">
							<svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
								<circle cx="12" cy="12" r="10" fill="#6c757d"/>
								<path d="M18 6L6 18M6 6L18 18" stroke="white" stroke-width="2" stroke-linecap="round"/>
							</svg>
						</div>
						<b>
						<h5 style="color: #6c757d;">Truy cập bị từ chối</h5>
						</b>
						<p>Bạn không có quyền truy cập trang này.</p>
						<button class="btn btn-secondary w-100" onclick="window.location.href='<?php echo FULL_URL('/') ?>'">Về trang chủ</button>
					</div>
				</div>
			</div>
		</div>
	</div>
</Center>