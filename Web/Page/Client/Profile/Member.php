<?php
if (!$user) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}
?>
<div class="body-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <div class="card shadow-none border">
                    <div class="card-body">
                        <div class="mt-n5">
                            <div class="d-flex align-items-center justify-content-center mb-3">
                                <div class="linear-gradient d-flex align-items-center justify-content-center rounded-circle"
                                    style="width: 110px; height: 110px;" ;="">
                                    <div class="border border-4 border-white d-flex align-items-center justify-content-center rounded-circle overflow-hidden"
                                        style="width: 100px; height: 100px;" ;="">
                                        <img src="/assets/images/avatar/<?php echo $player['head']; ?>.png" alt=""
                                            class="w-75">
                                    </div>
                                </div>
                            </div>
                            <div class="text-center">
                                <h5 class="fs-5 mb-0 fw-semibold">
                                    <?php echo $user['username']; ?> [
                                    <?php echo $player['name']; ?>]
                                </h5>
                                <p class="mb-0 fs-4 text-danger">Thành Viên</p>
                            </div>
                        </div>
                        <div class="pt-2 col-12 mb-3 d-flex align-items-center justify-content-center">
                            <table class="table">
                                <tbody>
                                    <tr>
                                        <th scope="row">ID của bạn:</th>
                                        <th><span class="c-font-uppercase">
                                                <?php echo $user['id']; ?>
                                            </span></th>
                                    </tr>
                                    <tr>
                                        <th scope="row">Tên tài khoản:</th>
                                        <th>
                                            <?php echo $user['username']; ?>
                                        </th>
                                    </tr>
                                    <tr>
                                        <th scope="row">Ngọc hiện có trong game:</th>
                                        <td><b class="text-danger">
                                                <?php echo number_format($player['ingame_gem'] ?? 0); ?> ngọc
                                            </b></td>
                                    </tr>
                                    <tr>
                                        <th scope="row">Tổng ngọc đã nạp:</th>
                                        <td><b class="text-danger">
                                                <?php echo number_format($user['napngoc']); ?> ngọc
                                            </b></td>
                                    </tr>
                                    <tr>
                                        <th scope="row">Nhóm tài khoản:</th>
                                        <td>Thành viên</td>
                                    </tr>
                                    <tr>
                                        <th scope="row">Ngày tham gia:</th>
                                        <td>
                                            <?php echo $user['create_time']; ?>
                                        </td>
                                    </tr>
                                    <tr>
                                        <th scope="row">Mật khẩu:</th>
                                        <td><a class="thea" href="/doi-mat-khau"><b><i class="text-danger">****** (Đổi
                                                        mật khẩu)</i></b></a>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>