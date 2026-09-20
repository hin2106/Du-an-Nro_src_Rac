<?php if($setting["nhanqua"] == 'true'){ 
if(isset($user["username"]) && !$CVH->checkNhanqua($user["username"])){
?>
<style>
    @media only screen and (max-width: 640px) {
        #bonus {
            width: 35% !important;
        }
    }

    #bonus {
        position: fixed;
        bottom: 15px;
        left: 15px;
        width: 13%;
        z-index: 1000;
        cursor: pointer;
    }
</style>
<div class="modal fade" id="noticeModal1" data-bs-backdrop="static" tabindex="-1" aria-modal="true" role="dialog">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header d-flex align-items-center">
                <h4 class="modal-title" id="myLargeModalLabel">
                    Thông Báo
                </h4>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body" id="content"></div>
            <div class="modal-footer">
                <button type="button" class="btn btn-light-danger text-danger font-medium waves-effect text-start"
                    data-bs-dismiss="modal">
                    Đóng
                </button>
            </div>
        </div>
    </div>
</div>
<div id="bonus" title="Click để nhận thưởng!">
    <img class=gift_box src="/Assets/images/icon/giftbox.gif">
</div>
<script type="text/javascript">
    $(document).ready(function (e) {
        $('body').delegate('#bonus', 'click', function () {
            $.ajax({
                url: '/Api/User/Bonus',
                datatype: 'json',
                data: {},
                type: 'POST',
                success: function (data) {

                    if (data.status == 'LOGIN') {
                        location.href = '/dang-nhap';
                        return;
                    }
                    $('#content').html(data.msg);
                    $('#noticeModal1').modal('show');
                    $('#bonus').css('opacity', '0');
                },
                error: function () {
                    $('#content').text('Có lỗi xảy ra. Vui lòng thử lại!');
                    $('#noticeModal1').modal('show');
                }
            })
        });
    });
    $('#indexModal').modal('show');
</script>
<?php } } ?>
<div class="offcanvas offcanvas-start modernize-lp-offcanvas" tabindex="-1" id="offcanvasNavbar"
    aria-labelledby="offcanvasNavbarLabel" aria-modal="true" role="dialog">
    <div class="">
        <div>
            <div class="brand-logo d-flex align-items-center justify-content-between">
                <a href="/" class="text-nowrap logo-img">
                    <img src="<?php echo $setting['logo']; ?>" class="dark-logo" width="<?php echo $setting['size_logo'] * 3; ?>px" alt="">
                    <img src="<?php echo $setting['logo']; ?>" class="light-logo" width="<?php echo $setting['size_logo'] * 3; ?>px" alt=""
                        style="display: none;">
                </a>
                <div class="close-btn d-lg-none d-block sidebartoggler cursor-pointer" id="sidebarCollapse">
                    <i class="ti ti-x fs-8 text-muted text-primary"></i>
                </div>
            </div>
            <nav class="sidebar-nav scroll-sidebar" data-simplebar="init">
                <div class="simplebar-wrapper selected" style="margin: 0px -24px;">
                    <div class="simplebar-height-auto-observer-wrapper">
                        <div class="simplebar-height-auto-observer"></div>
                    </div>
                    <div class="simplebar-mask selected">
                        <div class="simplebar-offset selected" style="right: 0px; bottom: 0px;">
                            <div class="simplebar-content-wrapper selected" tabindex="0" role="region"
                                aria-label="scrollable content" style="height: 100%; overflow: hidden scroll;">
                                <div class="simplebar-content selected" style="padding: 0px 24px;">
                                    <ul id="sidebarnav" class="in">
                                        <li class="nav-small-cap">
                                            <i class="ti ti-dots nav-small-cap-icon fs-4"></i>
                                            <span class="hide-menu">Menu</span>
                                        </li>

                                        <li class="sidebar-item">
                                            <a class="sidebar-link" href="/" aria-expanded="false">
                                                <span class="d-flex">
                                                    <i class="ti ti-home"></i>
                                                </span>
                                                <span class="hide-menu">TRANG CHỦ</span>
                                            </a>
                                        </li>
                                        <li class="sidebar-item">
                                            <a class="sidebar-link" href="/tai-game" aria-expanded="false">
                                                <span class="d-flex">
                                                    <i class="ti ti-device-gamepad-2"></i>
                                                </span>
                                                <span class="hide-menu">TẢI GAME</span>
                                            </a>
                                        </li>
                                        <li class="sidebar-item">
                                            <a class="sidebar-link" href="/top-nap" aria-expanded="false">
                                                <span class="d-flex">
                                                    <i class="ti ti-cash"></i>
                                                </span>
                                                <span class="hide-menu">TOP NẠP</span>
                                            </a>
                                        </li>
                                        <li class="sidebar-item">
                                            <a class="sidebar-link" href="/top-suc-manh" aria-expanded="false">
                                                <span class="d-flex">
                                                    <i class="ti ti-align-box-top-center"></i>
                                                </span>
                                                <span class="hide-menu">TOP SỨC MẠNH</span>
                                            </a>
                                        </li>
                                        <?php if ($user) { ?>
                                            <li class="sidebar-item">
                                                <a class="sidebar-link" href="/nap-bank" aria-expanded="false">
                                                    <span class="d-flex">
                                                        <i class="ti ti-coin"></i>
                                                    </span>
                                                    <span class="hide-menu">NẠP TIỀN</span>
                                                </a>
                                            </li>
                                            <li class="sidebar-item">
                                                <a class="sidebar-link" href="/dang-xuat" aria-expanded="false">
                                                    <span class="d-flex">
                                                        <i class="ti ti-logout"></i>
                                                    </span>
                                                    <span class="hide-menu">ĐĂNG XUẤT</span>
                                                </a>
                                            </li>
                                        <?php } ?>
                                        <?php if ($user && $user['is_admin'] == 1) { ?>
                                            <li class="sidebar-item">
                                                <a class="sidebar-link text-danger" href="/admin" aria-expanded="false">
                                                    <span class="d-flex">
                                                        <i class="ti ti-user"></i>
                                                    </span>
                                                    <span class="hide-menu">ADMIN</span>
                                                </a>
                                            </li>
                                        <?php } ?>
                                    </ul>
                                    <?php if (!$user) { ?>
                                        <a class="btn btn-primary fs-3 w-100 rounded btn-hover-shadow px-3 py-2"
                                            href="/dang-nhap"><i class="ti ti-login"></i> Đăng nhập vào tài khoản</a>
                                    <?php } else { ?>
                                        <hr>
                                        <a href="/thong-tin">
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
                                    <?php if (isset($user) && $user['active'] == 0) { ?>
                                        <div
                                            class="unlimited-access hide-menu bg-light-primary position-relative my-7 rounded">
                                            <div class="d-flex">
                                                <div class="unlimited-access-title">
                                                    <h6 class="fw-semibold fs-4 mb-6 text-dark w-85">Tài khoản chưa được
                                                        kích hoạt.</h6>
                                                    <form cvhvn="true" method="POST" action="/Api/User/Active"
                                                        href="<?php echo getCurrentURL(); ?>">
                                                        <input name="username" type="hidden"
                                                            value="<?php echo $user['username']; ?>" />
                                                        <button type="submit" href="<?php echo getCurrentURL(); ?>"
                                                            class="btn btn-primary fs-2 fw-semibold lh-sm">Kích
                                                            Hoạt</button>
                                                    </form>
                                                </div>
                                                <div class="unlimited-access-img">
                                                    <img src="/Assets/images/backgrounds/rocket.png" alt=""
                                                        class="img-fluid">
                                                </div>
                                            </div>
                                        </div>
                                    <?php } else { ?>
                                        <div
                                            class="unlimited-access hide-menu bg-light-success position-relative my-7 rounded">
                                            <div class="d-flex">
                                                <div class="unlimited-access-title">
                                                    <h6 class="fw-semibold fs-4 mb-6 text-dark w-100">Tài khoản của bạn đã
                                                        được
                                                        kích hoạt, chúc
                                                        <?php echo $player['name']; ?> chơi game vui vẻ
                                                    </h6>
                                                </div>
                                            </div>
                                        </div>
                                    <?php }  }?>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="simplebar-placeholder" style="width: auto; height: 3756px;"></div>
                </div>
                <div class="simplebar-track simplebar-horizontal" style="visibility: hidden;">
                    <div class="simplebar-scrollbar" style="width: 0px; display: none;"></div>
                </div>
                <div class="simplebar-track simplebar-vertical" style="visibility: visible;">
                    <div class="simplebar-scrollbar"
                        style="height: 90px; transform: translate3d(0px, 0px, 0px); display: block;"></div>
                </div>
            </nav>
        </div>
    </div>
</div>