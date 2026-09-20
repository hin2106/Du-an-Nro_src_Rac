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
                        <h4 class="text-center">Đăng Nhập</h4>
                        <div class="position-relative text-center my-4">
                            <p class="mb-0 fs-4 px-3 d-inline-block bg-white text-dark z-index-5 position-relative">
                                đăng nhập vào tài khoản</p>
                            <span class="border-top w-100 position-absolute top-50 start-50 translate-middle"></span>
                        </div>
                        <form cvhvn="true" method="POST" action="/Api/Auth/Login" href="<?php echo FULL_URL('/'); ?>">
                            <div class="mb-3">
                                <lbael class="form-label">Tài khoản</lbael>
                                <input type="text" class="form-control" name="username" placeholder="Nhập tài khoản">
                            </div>
                            <div class="mb-4">
                                <label class="form-label">Mật khẩu</label>
                                <input type="password" class="form-control" name="password" placeholder="Nhập mật khẩu">
                            </div>
                            <div class="d-flex align-items-center justify-content-between mb-4">
                                <div class="form-check">
                                    <input class="form-check-input primary" type="checkbox" value=""
                                        id="flexCheckChecked" checked="">
                                    <label class="form-check-label text-dark" for="flexCheckChecked">
                                        Ghi nhớ thiết bị
                                    </label>
                                </div>
                                <a class="text-primary fw-medium" href="/quen-mat-khau">Quên mật
                                    khẩu ?</a>
                            </div>
                            <button type="submit" href="<?php echo FULL_URL('/'); ?>"
                                class="btn btn-primary w-100 py-8 mb-4 rounded-2">Đăng
                                Nhập</button>
                            <div class="d-flex align-items-center justify-content-center">
                                <p class="fs-4 mb-0 fw-medium">Bạn chưa có tài khoản?</p>
                                <a class="text-primary fw-medium ms-2" href="/dang-ky">đăng ký</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>