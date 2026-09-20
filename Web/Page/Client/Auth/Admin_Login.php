<?php
if ($user['is_admin'] != 1) {
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
                        <form cvhvn="true" method="POST" action="/Api/Admin/Login" href="<?php echo FULL_URL('/admin'); ?>">
                            <div class="mb-3">
                                <input type="password" class="form-control" name="password" placeholder="Nhập mật khẩu cấp 2">
                            </div>
                            <button type="submit" href="<?php echo FULL_URL('/admin'); ?>"
                                class="btn btn-sm btn-primary w-100 py-8 mb-4 rounded-2">Đăng
                                Nhập</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>