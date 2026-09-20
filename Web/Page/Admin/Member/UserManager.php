<?php
$kmess = 10; // Số phim hiện trong mỗi page
$page = isset($_REQUEST['page']) && $_REQUEST['page'] > 0 ? intval($_REQUEST['page']) : 1;
$start = isset($_REQUEST['page']) ? $page * $kmess - $kmess : (isset($_GET['start']) ? abs(intval($_GET['start'])) : 0);
$result = mysqli_query($CVH->connect_db(), "SELECT player.*, account.*  FROM player INNER JOIN account ON player.account_id = account.id ORDER BY account.id DESC LIMIT $start, $kmess");
$tong = mysqli_num_rows(mysqli_query($CVH->connect_db(), "SELECT player.*, account.*  FROM player INNER JOIN account ON player.account_id = account.id"));
?>
<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="card card-body">
            <div class="row">
                <div class="col-md-12 col-xl-12">
                    <div class="position-relative">
                        <input type="text" class="form-control product-search ps-5" id="search"
                            placeholder="Tìm kiếm thành viên...">
                        <i
                            class="ti ti-search position-absolute top-50 start-0 translate-middle-y fs-6 text-dark ms-3"></i>
                    </div>
                    <script type="text/javascript">
                        $(document).ready(function () {
                            $("#search").keyup(function () {
                                var query = $(this).val();
                                if (query != '') {
                                    $.ajax({
                                        url: '/Api/Admin/Search',
                                        method: 'POST',
                                        data: { query: query },
                                        success: function (data) {
                                            $('#result').html(data);
                                        }
                                    });
                                }
                            });
                        });
                    </script>
                </div>
            </div>
        </div>
        <div class="card card-body">
            <div class="table-responsive">
                <table class="table search-table align-middle text-nowrap">
                    <thead class="header-item">
                        <tr>
                            <th>Information</th>
                            <th>Ngọc (Tổng nạp)</th>
                            <th>Banned</th>
                            <th>Active</th>
                            <th>IP</th>
                            <th>Time</th>
                            <th>Control</th>
                        </tr>
                    </thead>
                    <tbody id="result">
                        <?php
                        $i = 1;
                        if (mysqli_num_rows($result) > 0) {
                            while ($row = mysqli_fetch_assoc($result)) {
                                ?>
                                <tr class="search-items">
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <img src="/Assets/images/avatar/<?php echo $row["head"]; ?>.png" width="30"
                                                alt="" />
                                            <div class="ms-3">
                                                <div class="user-meta-info">
                                                    <h6 class="user-name mb-0">Tên:
                                                        <?php echo $row["name"]; ?>
                                                    </h6>
                                                    <span class="user-work fs-3">TK:
                                                        <?php echo $row["username"]; ?>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="usr-email-addr">
                                            <?php echo number_format($row["napngoc"]); ?> ngọc
                                        </span>
                                    </td>
                                    <td>
                                        <span class="usr-location">
                                            <?php echo getBand($row["ban"]); ?>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="usr-ph-no">
                                            <?php echo getActive($row["active"]); ?>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="usr-ph-no">
                                            <?php echo ($row["ip_address"]); ?>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="usr-ph-no">
                                            <?php echo ($row["create_time"]); ?>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="action-btn">
                                            <a href="javascript:void(0)" onclick="del_(<?php echo $row['account_id']; ?>);"
                                                class="text-danger delete ms-2">
                                                <i class="ti ti-trash fs-5"></i>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            <?php }
                        } else { ?>
                            <tr class="text-center">
                                <td colspan='6'>
                                    <img src="https://cdn-icons-png.flaticon.com/128/7466/7466139.png" width="50"
                                        class="img-fluid">
                                    <p class="pt-3"><b>Không có dữ liệu</b></p>
                                </td>
                            </tr>
                        <?php } ?>
                    </tbody>
                </table>
            </div>
            <?php
            if ($tong > $kmess) {
                echo '<center>' . $CVH->phantrang('/admin/users/', $start, $tong, $kmess) . '</center>';
            }
            ?>
        </div>
    </div>
    <script>
        function del_(id) {
            Swal.fire({
                title: 'Thông Báo',
                text: 'Bạn có muốn xóa tài khoản và nhân vật của #' + id + ' không!',
                icon: 'warning',
                showDenyButton: true,
                confirmButtonText: 'Đồng Ý',
                denyButtonText: `Đóng`,
            }).then((result) => {
                if (result.isConfirmed) {
                    $.ajax({
                        type: 'POST',
                        url: '/Api/Admin/DelMem',
                        data: {
                            type: 'Del_Mem',
                            id: id
                        },
                        success: function (response) {
                            var data = JSON.parse(response);
                            if (data.status == true) {
                                Swal.fire('Thông báo', data.message, 'success').then(() => {
                                    window.location.reload();
                                });
                            } else {
                                Swal.fire('Thông báo', 'Có lỗi xảy ra vui lòng thử lại!', 'error').then(() => {
                                });
                            }
                        },

                        error: function () {
                            Swal.fire('Thông Báo', 'Có lỗi xảy ra vui lòng thử lại sau!', 'error');
                        }
                    });
                }
            });
        }
    </script>
</div>
</div>