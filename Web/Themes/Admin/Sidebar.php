<style>
    .fixed-left-menu {
        position: fixed;
        top: 50%;
        left: -65px;
        transform: translateY(-50%);
        padding: 10px;
        cursor: pointer;
        transition: left 0.3s ease;
        opacity: 0.7;
    }

    .fixed-left-menu:hover {
        left: -20px;
        opacity: 1;
    }

    .offcanvas {
        transform: translateX(-100%);
        transition: transform 0.3s ease-in-out;
    }

    .offcanvas.show {
        transform: translateX(0);
    }
</style>
<div class="fixed-left-menu">
    <button class="btn btn-danger" type="button" data-bs-toggle="offcanvas" data-bs-target="#menuAdmin"
        aria-controls="menuAdmin">Menu <i class="ti ti-layout-sidebar-left-expand"></i></button>
</div>
<div class="offcanvas offcanvas-start" data-bs-scroll="true" tabindex="-1" id="menuAdmin"
    aria-labelledby="menuAdminLabel" aria-modal="true" role="dialog">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="menuAdminLabel">
            <b>CVHVN</b>
        </h5>
        <button type="button" class="btn-close text-reset" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body" data-simplebar="init">
        <div class="simplebar-wrapper" style="margin: -16px;">
            <div class="simplebar-height-auto-observer-wrapper">
                <div class="simplebar-height-auto-observer"></div>
            </div>
            <div class="simplebar-mask">
                <div class="simplebar-offset" style="right: 0px; bottom: 0px;">
                    <div class="simplebar-content-wrapper" tabindex="0" role="region" aria-label="scrollable content"
                        style="height: 100%; overflow: hidden;">
                        <div class="simplebar-content" style="padding: 16px;">
                            <div>
                                <div class="position-relative">
                                    <a href="/admin"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/747/747846.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Trang Chủ</h6>
                                            <span class="fs-2 d-block text-dark">Thống kê và chức năng</span>
                                        </div>
                                    </a>
                                    <a href="/admin/users"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/11648/11648725.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Thành viên</h6>
                                            <span class="fs-2 d-block text-dark">Quản lý thành viên</span>
                                        </div>
                                    </a>
                                    <a href="/admin/history/recharge"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/7290/7290212.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Nạp thẻ</h6>
                                            <span class="fs-2 d-block text-dark">Lịch sử nạp thẻ</span>
                                        </div>
                                    </a>
                                    <a href="/admin/setting"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/975/975660.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Cài Đặt</h6>
                                            <span class="fs-2 d-block text-dark">Thông tin website</span>
                                        </div>
                                    </a>
                                    <a href="/admin/poster-user"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/7995/7995429.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Bài viết</h6>
                                            <span class="fs-2 d-block text-dark">Quản lý bài viết</span>
                                        </div>
                                    </a>
                                    <a href="/admin/poster"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/12343/12343174.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Thông báo</h6>
                                            <span class="fs-2 d-block text-dark">Thêm thông báo nổi bật</span>
                                        </div>
                                    </a>
                                    <a href="/admin/giftcode"
                                        class="d-flex align-items-center pb-9 position-relative text-decoration-none text-decoration-none text-decoration-none text-decoration-none">
                                        <div
                                            class="bg-light rounded-1 me-3 p-6 d-flex align-items-center justify-content-center">
                                            <img src="https://cdn-icons-png.flaticon.com/128/1651/1651898.png" alt=""
                                                class="img-fluid" width="24" height="24">
                                        </div>
                                        <div class="d-inline-block">
                                            <h6 class="mb-1 fw-semibold">Giftcode</h6>
                                            <span class="fs-2 d-block text-dark">Thêm & xóa giftcode</span>
                                        </div>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="simplebar-placeholder" style="width: auto; height: 234px;"></div>
        </div>
        <div class="simplebar-track simplebar-horizontal" style="visibility: hidden;">
            <div class="simplebar-scrollbar" style="width: 0px; transform: translate3d(0px, 0px, 0px); display: none;">
            </div>
        </div>
        <div class="simplebar-track simplebar-vertical" style="visibility: hidden;">
            <div class="simplebar-scrollbar" style="height: 0px; display: none;"></div>
        </div>
    </div>
</div>