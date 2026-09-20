<?php
if (!$user) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}
?>
<div class="position-relative overflow-hidden radial-gradient min-vh-100 d-flex align-items-center justify-content-center body-wrapper">
    <div class="d-flex align-items-center justify-content-center w-100">
        <div class="row justify-content-center w-100">
            <div class="col-md-8 col-lg-6 col-xxl-3">
                <div class="card mb-0">
                    <div class="card-body">
                        <h4 class="text-center">Đổi Mật Khẩu</h4>
                        <div class="position-relative text-center my-4">
                            <p class="mb-0 fs-4 px-3 d-inline-block bg-white text-dark z-index-5 position-relative">Đổi mật khẩu mới</p>
                            <span class="border-top w-100 position-absolute top-50 start-50 translate-middle"></span>
                        </div>
                        <form cvhvn="true" autocomplete="off" method="POST" action="/Api/Auth/changePassword" href="<?php echo FULL_URL('/'); ?>">
                            <div class="mb-3">
                                <label class="form-label">Nhập mật khẩu cũ</label>
                                <input autocomplete="off" type="text" class="form-control" name="old_password" placeholder="Nhập mật khẩu cũ">
                            </div>
                            <div class="mb-4">
                                <label class="form-label">Nhập mật khẩu mới</label>
                                <input autocomplete="off" type="password" class="form-control" name="new_password" placeholder="Nhập mật khẩu mới">
                            </div>
                            <div class="mb-4">
                                <label class="form-label">Nhập lại mật khẩu mới</label>
                                <input autocomplete="off" type="password" class="form-control" name="renew_password" placeholder="Nhập lại mật khẩu mới">
                            </div>
                            <button type="submit" href="<?php echo FULL_URL('/'); ?>" class="btn btn-primary w-100 py-8 mb-4 rounded-2">Đổi Mật Khẩu</button>
                            <div class="d-flex align-items-center justify-content-center">
                                <p class="fs-4 mb-0 fw-medium">Quay lại trang chủ?</p>
                                <a class="text-primary fw-medium ms-2" href="<?php echo FULL_URL('/'); ?>">Home</a>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>