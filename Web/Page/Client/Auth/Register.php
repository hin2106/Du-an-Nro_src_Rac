<?php
if ($user) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}
?>
<div
    class="position-relative overflow-hidden radial-gradient min-vh-100 d-flex align-items-center justify-content-center body-wrapper">
    <div class="d-flex align-items-center justify-content-center w-100">
        <div class="row justify-content-center w-100">
            <div class="col-md-8 col-lg-6 col-xxl-3">
                <div class="card mb-0">
                    <div class="card-body">
                        <h4 class="text-center">Đăng Ký</h4>
                        <div class="position-relative text-center my-4">
                            <p class="mb-0 fs-4 px-3 d-inline-block bg-white text-dark z-index-5 position-relative">
                                đăng ký tài khoản mới</p>
                            <span class="border-top w-100 position-absolute top-50 start-50 translate-middle"></span>
                        </div>
                        <form cvhvn="true" method="POST" action="/Api/Auth/Register" href="<?php echo FULL_URL('/'); ?>">
                            <div class="mb-3">
                                <label class="form-label">Tài khoản</label>
                                <input type="text" class="form-control" name="username" placeholder="Nhập tài khoản">
                            </div>
                            <div class="mb-4">
                                <label class="form-label">Mật khẩu</label>
                                <input type="password" class="form-control" name="password" placeholder="Nhập mật khẩu">
                            </div>
                            <div class="mb-4">
                                <label class="form-label">Nhập lại mật khẩu</label>
                                <input type="password" class="form-control" name="repassword" placeholder="Nhập lại mật khẩu">
                            </div>
							<div class="mb-4">
                                <label class="form-label">Email</label>
                                <input type="email" class="form-control" name="email" placeholder="Email này dùng để lấy lại mật khẩu!">
                            </div>
                            <button type="submit" href="<?php echo FULL_URL('/'); ?>" class="btn btn-primary w-100 py-8 mb-4 rounded-2">Đăng
                                Ký</button>
                            <div class="d-flex align-items-center justify-content-center">
                                <p class="fs-4 mb-0 fw-medium">Bạn đã có tài khoản?</p>
                                <a class="text-primary fw-medium ms-2" href="/dang-nhap">đăng nhập</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>