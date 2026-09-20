<?php
$kmess = 16; // Số phim hiện trong mỗi page
$page = isset($_REQUEST['page']) && $_REQUEST['page'] > 0 ? intval($_REQUEST['page']) : 1;
$start = isset($_REQUEST['page']) ? $page * $kmess - $kmess : (isset($_GET['start']) ? abs(intval($_GET['start'])) : 0);
$result = mysqli_query($CVH->connect_db(), "SELECT * FROM `cvh_baiviet` WHERE `role` = 1 ORDER BY `time` DESC LIMIT $start, $kmess");
$tong = mysqli_num_rows(mysqli_query($CVH->connect_db(), "SELECT * FROM `cvh_baiviet` WHERE `role` = 1 "));
?>
<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="row">
            <div class="col-sm-12">
                <div class="card card-body">
                    <div class="table-responsive">
                        <table class="table search-table align-middle text-nowrap">
                            <thead class="header-item">
                                <tr>
                                    <th>ID</th>
                                    <th>NAME</th>
                                    <th>TITLE</th>
                                    <th>VIEWS</th>
                                    <th>LIKE</th>
                                    <th>CMT</th>
                                    <th>TIME</th>
                                    <th>CHÚC NĂNG</th>
                                </tr>
                            </thead>
                            <tbody id="result">
                                <?php
                                $i = 0;
                                if (mysqli_num_rows($result) > 0) {
                                    while ($row = mysqli_fetch_assoc($result)) {
                                        $i++;
                                        $pl = $CVH->player($row["poster"]);
                                        ?>
                                        <tr class="search-items">
                                            <td>
                                                <span>
                                                    <?php echo $i; ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span>
                                                    <?php echo $pl["name"]; ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span>
                                                    <?php echo $row["title"]; ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span>
                                                    <?php echo number_format($row["views"]); ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span>
                                                    <?php echo $CVH->getTotalLikes($row['id']); ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span>
                                                    <?php echo $CVH->getTotalComments($row['id']); ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span>
                                                    <?php echo $CVH->time_ago($row['time']); ?>
                                                </span>
                                            </td>
                                            <td>
                                                <div class="action-btn">
                                                    <a href="javascript:void(0)" onclick="del_(<?php echo $row['id']; ?>);"
                                                        class="text-danger delete ms-2">
                                                        <i class="ti ti-trash fs-5"></i>
                                                    </a>
                                                </div>
                                            </td>
                                        </tr>
                                    <?php }
                                } else { ?>
                                    <tr class="text-center">
                                        <td colspan='8'>
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
                        echo '<center>' . $CVH->phantrang('/admin/poster/', $start, $tong, $kmess) . '</center>';
                    }
                    ?>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
<script>
    function del_(id) {
        Swal.fire({
            title: 'Thông Báo',
            text: 'Bạn có muốn xóa thông báo có ID #' + id + ' không!',
            icon: 'warning',
            showDenyButton: true,
            confirmButtonText: 'Đồng Ý',
            denyButtonText: `Đóng`,
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    type: 'POST',
                    url: '/Api/Admin/Post/Delete',
                    data: {
                        type: 'Del_Post',
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