<div class="position-relative overflow-hidden radial-gradient min-vh-100 d-flex align-items-center justify-content-center body-wrapper">
    <div class="d-flex align-items-center justify-content-center w-100">
        <div class="row justify-content-center w-100">
            <div class="col-md-8 col-lg-6 col-xxl-3">
                <div class="card mb-0">
                    <div class="card-body">
                        <h4 class="text-center">Đặt Lại Mật Khẩu</h4>
                        <div class="position-relative text-center my-4">
                            <p class="mb-0 fs-4 px-3 d-inline-block bg-white text-dark z-index-5 position-relative">
                                Đặt lại mật khẩu mới
                            </p>
                            <span class="border-top w-100 position-absolute top-50 start-50 translate-middle"></span>
                        </div>
                        <form cvhvn="true" method="POST" action="/Api/Auth/resetPassword">
							<!-- Thêm các trường ẩn để truyền token và email -->
							<input type="hidden" name="email" value="<?php echo $_GET['email'] ?? ''; ?>">
							<input type="hidden" name="token" value="<?php echo $_GET['token'] ?? ''; ?>">
							<div class="mb-4">
								<label>Mật khẩu mới</label>
								<input type="password" class="form-control" name="new_password" required>
							</div>

							<button type="submit" class="btn btn-primary w-100 py-8 mb-4 rounded-2">
								Đặt lại mật khẩu
							</button>
						</form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>