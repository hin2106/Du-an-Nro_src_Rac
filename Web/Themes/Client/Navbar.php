<div class="page-wrapper" id="main-wrapper" data-layout="horizontal" data-navbarbg="skin6" data-sidebartype="full"
    data-boxed-layout="boxed" data-sidebar-position="fixed" data-header-position="fixed">
    <header class="app-header">
        <nav class="navbar navbar-expand-lg py-0">
            <div class="container-sm">
                <a class="navbar-brand" href="/">
                    <img src="<?php echo $setting['logo']; ?>" height="<?php echo $setting['size_logo']; ?>px" alt="img-fluid">
                </a>
                <button class="navbar-toggler d-none" type="button" data-bs-toggle="collapse"
                    data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                    aria-expanded="false" aria-label="Toggle navigation">
                    <i class="ti ti-menu-2 fs-9"></i>
                </button>
                <button class="navbar-toggler border-0 p-0 shadow-none" type="button" data-bs-toggle="offcanvas"
                    data-bs-target="#offcanvasNavbar" aria-controls="offcanvasNavbar">
                    <i class="ti ti-menu-2 fs-9"></i>
                </button>
                <div class="collapse navbar-collapse" id="navbarSupportedContent">
                    <ul class="navbar-nav align-items-center mb-2 mb-lg-0 ms-auto quick-links">
                        <li class="nav-item hover-dd">
                            <a class="nav-link" href="/"> TRANG CHỦ </a>
                        </li>
                        <li class="nav-item hover-dd">
                            <a class="nav-link" href="/tai-game"> TẢI GAME </a>
                        </li>
                        <li class="nav-item hover-dd">
                            <a class="nav-link" href="/top-nap"> TOP NẠP </a>
                        </li>
                        <li class="nav-item hover-dd">
                            <a class="nav-link" href="/top-suc-manh"> TOP SỨC MẠNH </a>
                        </li>
                        <?php if ($user && $user['is_admin'] == 1) { ?>
                            <li class="nav-item hover-dd">
                                <a class="nav-link text-danger" href="/admin"> ADMIN </a>
                            </li>
                        <?php } ?>
                        <li class="nav-item ms-2">
                            <?php if ($user) { ?>
                                <a href="/thong-tin" href="javascript:void(0)" id="infoUser" data-bs-toggle="dropdown"
                                    aria-expanded="true">
                                    <div class="d-flex align-items-center fs-3 col-8">
                                        <img src="/assets/images/avatar/<?php echo $player['head']; ?>.png"
                                            alt="Ảnh đại diện" width="35">
                                        <div class="ms-2">
                                            <div class="user-meta-info">
                                                <h6 class="user-name mb-0">
                                                    <?php echo $player['name']; ?>
                                                </h6>
                                                <span class="user-work fs-3 text-dark"><b class="text-danger">
                                                        <?php echo number_format($player['ingame_gem'] ?? 0); ?> ngọc
                                                    </b></span>
                                            </div>
                                        </div>
                                    </div>
                                </a>
                                <div class="dropdown-menu content-dd dropdown-menu-end dropdown-menu-animate-up"
                                    aria-labelledby="infoUser" data-bs-popper="static">
                                    <div class="profile-dropdown position-relative" data-simplebar="init">
                                        <div class="simplebar-wrapper" style="margin: 0px;">
                                            <div class="simplebar-height-auto-observer-wrapper">
                                                <div class="simplebar-height-auto-observer"></div>
                                            </div>
                                            <div class="simplebar-mask">
                                                <div class="simplebar-offset" style="right: 0px; bottom: 0px;">
                                                    <div class="simplebar-content-wrapper" tabindex="0" role="region"
                                                        aria-label="scrollable content"
                                                        style="height: auto; overflow: hidden;">
                                                        <div class="simplebar-content" style="padding: 0px;">
                                                            <div class="message-body">
                                                                <a href="/thong-tin-tai-khoan"
                                                                    class="py-8 px-7 mt-8 d-flex align-items-center">
                                                                    <span
                                                                        class="d-flex align-items-center justify-content-center bg-light rounded-1 p-6">
                                                                        <img src="https://cdn-icons-png.flaticon.com/128/3917/3917688.png"
                                                                            alt="" width="18" height="18 ">
                                                                    </span>
                                                                    <div class="w-75 d-inline-block v-middle ps-3">
                                                                        <h6 class="mb-1 bg-hover-primary fw-semibold"> Tài
                                                                            khoản của tôi </h6>
                                                                    </div>
                                                                </a>

                                                                <a href="/nap-bank"
                                                                    class="py-8 px-7 mt-8 d-flex align-items-center">
                                                                    <span
                                                                        class="d-flex align-items-center justify-content-center bg-light rounded-1 p-6">
                                                                        <img src="https://cdn-icons-png.flaticon.com/128/3914/3914398.png"
                                                                            alt="" width="18" height="18 ">
                                                                    </span>
                                                                    <div class="w-75 d-inline-block v-middle ps-3">
                                                                        <h6 class="mb-1 bg-hover-primary fw-semibold"> Nạp
                                                                            tiền vào tài khoản </h6>
                                                                    </div>
                                                                </a>
                                                            </div>
                                                            <div class="d-grid py-4 px-7 pt-8">
                                                                <?php if ($user['active'] == 0) { ?>
                                                                    <div
                                                                        class="upgrade-plan bg-light-primary position-relative overflow-hidden rounded-4 p-4 mb-9">
                                                                        <div class="row">
                                                                            <div class="col-6">
                                                                                <h5
                                                                                    class="fs-4 mb-3 w-100 fw-semibold text-dark">
                                                                                    Tài khoản chưa được kích hoạt.</h5>
                                                                                <form cvhvn="true" method="POST"
                                                                                    action="/Api/User/Active"
                                                                                    href="<?php echo getCurrentURL(); ?>">
                                                                                    <input name="username" type="hidden"
                                                                                        value="<?php echo $user['username']; ?>" />
                                                                                    <button type="submit"
                                                                                        href="<?php echo getCurrentURL(); ?>"
                                                                                        class="btn btn-primary text-white">Kích
                                                                                        Hoạt</button>
                                                                                </form>
                                                                            </div>
                                                                            <div class="col-6">
                                                                                <div class="m-n1">
                                                                                    <img src="https://cdn-icons-png.flaticon.com/128/2916/2916908.png"
                                                                                        alt="" class="w-100">
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                <?php } else { ?>
                                                                    <div
                                                                        class="upgrade-plan bg-light-success position-relative overflow-hidden rounded-4 p-4 mb-9">
                                                                        <div class="row">
                                                                            <div class="col-6">
                                                                                <h5
                                                                                    class="fs-4 mb-3 w-100 fw-semibold text-dark">
                                                                                    Tài của bạn đã được kích hoạt.</h5>
                                                                            </div>
                                                                            <div class="col-6">
                                                                                <div class="m-n1">
                                                                                    <img src="https://cdn-icons-png.flaticon.com/128/12225/12225792.png"
                                                                                        alt="" class="w-60">
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                <?php } ?>
                                                                <a href="/dang-xuat" class="btn btn-outline-primary">Đăng
                                                                    xuất khỏi tài
                                                                    khoản</a>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="simplebar-placeholder" style="width: auto; height: 385px;"></div>
                                        </div>
                                        <div class="simplebar-track simplebar-horizontal" style="visibility: hidden;">
                                            <div class="simplebar-scrollbar" style="width: 0px; display: none;"></div>
                                        </div>
                                        <div class="simplebar-track simplebar-vertical" style="visibility: hidden;">
                                            <div class="simplebar-scrollbar" style="height: 0px; display: none;"></div>
                                        </div>
                                    </div>
                                </div>
                            <?php } else { ?>
                                <a class="btn btn-primary fs-3 rounded btn-hover-shadow px-3 py-2" href="/dang-nhap">ĐĂNG
                                    NHẬP</a>
                            <?php } ?>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>
    </header>