<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="col-md-12">
            <div class="row">
                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h5 class="fs-4">Tổng tài khoản</h5>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo fNumber($CVH->count('account')); ?> tài khoản
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-user-share"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h5 class="fs-4">Tổng nhân vật</h5>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo fNumber($CVH->count('player')); ?> nhân vật
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-user-pause"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h5 class="fs-4">Đã kích hoạt</h5>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo fNumber($CVH->count('account', 'active = 1')); ?> tài
                                        khoản
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-user-check"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h5 class="fs-4">Doanh thu</h5>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo number_format($CVH->tongdoanhthu()); ?>đ
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-coin"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h6 class="fs-2">Tài khoản hôm nay</h6>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo fNumber($CVH->TKhomnay()); ?> tài khoản
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-calendar-plus"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h6 class="fs-2">Doanh thu hôm nay</h6>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo number_format($CVH->DThomnay()); ?>đ
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-24-hours"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h5 class="fs-2">Tổng bài viết</h5>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo fNumber($CVH->count('cvh_baiviet')); ?> bài viết
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-ballpen"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-3 col-md-6">
                    <div class="card">
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-2">
                                <div>
                                    <h5 class="fs-4">Thẻ đang chờ</h5>
                                    <h6 class="card-subtitle mb-1 text-danger">
                                        <?php echo fNumber($CVH->count('cvh_recharge', 'status = 0')); ?>
                                        thẻ
                                    </h6>
                                </div>
                                <div class="ms-auto">
                                    <span class="text-success display-6"><i class="ti ti-credit-card"></i></span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <div class="row">
                <div class="col-lg-4">
                    <div class="card w-100">
                        <div class="card-body">
                            <div class="mb-2">
                                <h5 class="mb-0 card-title text-center">Kích Hoạt</h5>
                            </div>
                            <form cvhvn="true" method="POST" action="/Api/Admin/ChucNang/1"
                                href="<?php echo getCurrentURL(); ?>" class="pt-3">
                                <div class="mb-3">
                                    <select name="type" class="form-control form-select">
                                        <option>--Chọn chức năng--</option>
                                        <option value="active">Kích hoạt tài khoản</option>
                                        <option value="unactive">Hủy kích hoạt tài khoản</option>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <input type="text" class="form-control" name="username"
                                        placeholder="Nhập tên tài khoản">
                                </div>
                                <div class="mb-3">
                                    <button type="submit" href="<?php echo getCurrentURL(); ?>"
                                        class="btn btn-primary w-100">
                                        Cập Nhật Ngay
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="card w-100">
                        <div class="card-body">
                            <div class="mb-2">
                                <h5 class="mb-0 card-title text-center">Cộng Ngọc</h5>
                            </div>
                            <form cvhvn="true" method="POST" action="/Api/Admin/ChucNang/2"
                                href="<?php echo getCurrentURL(); ?>" class="pt-3">
                                <div class="mb-3">
                                    <input type="text" class="form-control" name="username"
                                        placeholder="Nhập tên tài khoản">
                                </div>
                                <div class="mb-3">
                                    <input type="number" class="form-control" name="money" placeholder="Nhập số ngọc">
                                </div>
                                </div>
                                <div class="mb-3">
                                    <button type="submit" href="<?php echo getCurrentURL(); ?>"
                                        class="btn btn-primary w-100">
                                        Cập Nhật Ngay
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="card w-100">
                        <div class="card-body">
                            <div class="mb-2">
                                <h5 class="mb-0 card-title text-center">Khóa Tài Khoản</h5>
                            </div>
                            <div class="mb-2">
                                <h5 class="mb-0 card-title text-center"></h5>
                            </div>
                            <form cvhvn="true" method="POST" action="/Api/Admin/ChucNang/3"
                                href="<?php echo getCurrentURL(); ?>" class="pt-3">
                                <div class="mb-3">
                                    <select name="type" class="form-control form-select">
                                        <option>--Chọn chức năng--</option>
                                        <option value="band">Khóa tài khoản</option>
                                        <option value="unband">Mở khóa tài khoản</option>
                                    </select>
                                </div>
                                <div class="mb-3">
                                    <input type="text" class="form-control" name="username"
                                        placeholder="Nhập tên tài khoản">
                                </div>
                                <div class="mb-3">
                                    <button type="submit" href="<?php echo getCurrentURL(); ?>"
                                        class="btn btn-primary w-100">
                                        Cập Nhật Ngay
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>