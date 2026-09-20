<div class="position-relative overflow-hidden radial-gradient min-vh-100 d-flex align-items-center justify-content-center body-wrapper">
    <div class="d-flex align-items-center justify-content-center w-100">
        <div class="row justify-content-center w-100">
            <div class="col-md-8 col-lg-6 col-xxl-3">
                <div class="card mb-0">
                    <div class="card-body">
                        <h4 class="text-center">Quên mật khẩu</h4>
                        <div class="position-relative text-center my-4">
                            <p class="mb-0 fs-4 px-3 d-inline-block bg-white text-dark z-index-5 position-relative">
                                Nhập email đã đăng ký để nhận link đặt lại mật khẩu 💌
                            </p>
                            <span class="border-top w-100 position-absolute top-50 start-50 translate-middle"></span>
                        </div>
                        <form cvhvn="true" method="POST" action="/Api/Auth/forgotPassword">
							<div class="mb-3">
								<label class="form-label">Email</label>
								<input type="email" class="form-control" name="email" placeholder="Nhập email đã đăng ký" required>
							</div>
							<button type="submit" class="btn btn-primary w-100 py-8 mb-4 rounded-2">
								Gửi link đặt lại mật khẩu
							</button>
							<div class="text-center">
								<a class="text-primary fw-medium" href="/dang-nhap">Quay lại đăng nhập</a>
							</div>
						</form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>