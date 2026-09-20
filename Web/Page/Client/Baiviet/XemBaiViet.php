<?php
$id = abs($_GET['id']);
if ($CVH->checkPost($id)) {
    $postData = $CVH->get_row("SELECT cvh_baiviet.*, account.username, account.point_post, player.head, player.name
                  FROM cvh_baiviet
                  LEFT JOIN account ON cvh_baiviet.poster = account.id
                  LEFT JOIN player ON cvh_baiviet.poster = player.account_id
                       WHERE cvh_baiviet.id = '" . $id . "'
                       ORDER BY cvh_baiviet.id DESC");

} else {
    echo "<script>window.location='/';</script>";

}
$table = 'cvh_baiviet';
$data = 'views';
$sodiem = 1;
$where = 'id = "' . $id . '"';
$CVH->cong($table, $data, $sodiem, $where);
?>
<div class="body-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <div class="card">
                    <div class="card-body border-bottom">
                        <div class="d-flex align-items-center justify-content-between mb-4">
                            <div class="d-flex">
                                <div class="d-flex align-items-center justify-content-center me-6">
                                    <?php if ($postData['if_admin'] != NULL) {
                                        $data = json_decode($postData['if_admin'], true); ?>
                                        <img src="<?php echo $data['avatar']; ?>" width="40" alt="" />
                                    <?php } else { ?>
                                        <img src="/Assets/images/avatar/<?php echo $postData['head']; ?>.png" width="35"
                                            alt="" />
                                    <?php } ?>
                                </div>
                                <div>
                                    <?php if ($postData['if_admin'] != NULL) { ?>
                                        <style>
                                            .centered-content {
                                                display: flex;
                                                align-items: center;
                                            }

                                            .centered-content img {
                                                margin-right: 10px;
                                            }
                                        </style>
                                        <p class="mb-1 fs-4 fw-semibold text-warning centered-content">
                                            <b>
                                                <?php echo $data['name']; ?>
                                            </b> <img class="mx-1"
                                                src="https://cdn-icons-png.flaticon.com/128/1828/1828652.png" width="16">
                                        </p>
                                    <?php } else { ?>
                                        <h6 class="mb-1 fs-4 fw-semibold">
                                            <?php echo $postData['name']; ?>
                                        </h6>
                                    <?php } ?>
                                    <p class="fs-3 mb-0">
                                        <?php echo $CVH->timeAgo($postData['time']); ?>
                                    </p>
                                </div>
                            </div>
                            <h6 class="mb-0 fw-semibold">Điểm:
                                <?php echo number_format($postData['point_post']); ?> điểm
                            </h6>

                        </div>
                        <h5 class="fw-semibold text-danger align-items-center">
                            <img src="https://cdn-icons-png.flaticon.com/128/11539/11539807.png" width="14">
                            <?php echo $postData['title']; ?>
                        </h5>
                        <p id="main-poster" class="text-dark my-3 poster">
                            <?php echo $postData['content']; ?>
                        </p>
                        <div class="d-flex align-items-center my-3">
                            <div class="d-flex align-items-center gap-2">
                                <?php if ($user) { ?>
                                    <a id="likeButton" <?php if ($CVH->checkUserLiked($id, $user["username"])) { ?>
                                            class="d-flex align-items-center justify-content-center bg-primary text-white p-2 fs-4 rounded-circle"
                                        <?php } else { ?>
                                            class="d-flex align-items-center justify-content-center bg-light-dark text-white p-2 fs-4 rounded-circle"
                                        <?php } ?> href="javascript:void(0)" data-bs-toggle="tooltip"
                                        data-bs-placement="top" data-bs-title="Like">
                                        <i class="ti ti-thumb-up"></i>
                                    </a>
                                <?php } else { ?>
                                    <a class="text-dark d-flex align-items-center justify-content-center bg-light-dark text-white p-2 fs-4 rounded-circle"
                                        href="javascript:void(0)" onclick="notice('Vui lòng đăng nhập!','error');"
                                        data-bs-toggle="tooltip" data-bs-placement="top" data-bs-title="Like">
                                        <i class="ti ti-thumb-up"></i>
                                    </a>
                                <?php } ?>
                                <span class="text-dark fw-semibold" id="totalLike">
                                    <?php echo $CVH->getTotalLikes($postData['id']); ?>
                                </span>
                            </div>
                            <div class="d-flex align-items-center gap-2 ms-4">
                                <a class="text-white d-flex align-items-center justify-content-center bg-secondary p-2 fs-4 rounded-circle"
                                    href="javascript:void(0)" data-bs-toggle="tooltip" data-bs-placement="top"
                                    data-bs-title="Comment">
                                    <i class="ti ti-message-2"></i>
                                </a>
                                <span class="text-dark fw-semibold">
                                    <?php echo $CVH->getTotalComments($postData['id']); ?>
                                </span>
                            </div>
                            <a
                                class="text-dark ms-auto d-flex align-items-center justify-content-center bg-transparent p-2 fs-4 rounded-circle">
                                <div class="d-flex align-items-center fs-2 ms-auto gap-2"><i
                                        class="ti ti-eye text-dark"></i>
                                    <?php echo number_format($postData['views']); ?>
                                </div>
                                <div class="d-flex align-items-center fs-2 ms-auto"><i
                                        class="ti ti-point text-dark"></i>
                                    <?php echo ($postData['created']); ?>
                                </div>
                            </a>
                        </div>
                        <div class="position-relative">
                            <div id="comments"></div>
                        </div>
                        <div class="text-center">
                            <button class="btn btn-outline-primary" id="load-more">Xem thêm bình luận</button>
                        </div>
                    </div>
                    <?php if ($user) { ?>
                        <form cvhvn="true" method="POST" action="/Api/Cmt/Add"
                            href="<?php echo FULL_URL('/bai-viet/' . +$id); ?>">
                            <div class="d-flex align-items-center gap-3 p-3">
                                <img src="/assets/images/avatar/<?php echo $player['head']; ?>.png" alt="" width="40"
                                    height="40">
                                <input type="hidden" name="id" value="<?php echo $id; ?>">
                                <input type="text" class="form-control py-8" name="content"
                                    placeholder="Nhập bình luận vào đây">
                                <button type="submit" href="<?php echo FULL_URL('/bai-viet/' . +$id); ?>"
                                    class="btn btn-primary">Gửi</button>
                            </div>
                        </form>
                    <?php } ?>
                </div>
            </div>
        </div>
    </div>
</div>
<script>
    $(document).ready(function () {
        var row = 1;
        var num_show = 5;
        var tatca = <?php echo $CVH->getTotalComments($postData['id']); ?>;

        function loadComments() {
            $.ajax({
                url: '/Api/Cmt/Load',
                type: 'POST',
                data: { postId: <?php echo $postData['id']; ?>, page: row, num_show: num_show },
                beforeSend: function () {
                    $("#load-more").text("Đang Tải...");
                },
                success: function (response) {
                    data = JSON.parse(response);
                    if (data.length === 0) {
                        $("#load-more").hide();
                    } else {
                        data.forEach(function (comment) {
                            var html = `
                        <div class="p-4 rounded-2 bg-light mb-3">
                            <div class="d-flex">
                                <div class="d-flex align-items-center justify-content-center me-6">
                                    <img src="/Assets/images/avatar/${comment.head}.png" width="30" alt="" />
                                </div>
                                <div>
                                    <b class="mb-1 fs-4 fw-semibold">${comment.name}</b> - <code>${formatTime(comment.time)}</code>
                                    <p class="fs-3 mb-0">${comment.noidung}</p>
                                </div>
                            </div>
                        </div>
                    `;
                            $("#comments").append(html);
                        });

                        row += 1;
                        $("#load-more").text("Xem thêm bình luận");
                    }
                }
            });
        }
        loadComments();
        $('#load-more').click(function () {
            loadComments();
        });
    });


    var likeButton = document.getElementById('likeButton');
    var total = document.getElementById('totalLike');
    likeButton.addEventListener('click', function () {
        var isLiked = likeButton.classList.contains('bg-primary');
        var postId = <?php echo $id; ?>;
        var action = isLiked ? 'unlike' : 'like';

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/Api/Post/Like', true);
        xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');

        xhr.onload = function () {
            if (xhr.status >= 200 && xhr.status < 300) {
                var data = JSON.parse(xhr.responseText);
                if (data.status === true) {
                    total.textContent = data.total;
                    if (isLiked) {
                        likeButton.classList.remove('bg-primary');
                        likeButton.classList.add('bg-light-dark');
                    } else {
                        likeButton.classList.remove('bg-light-dark');
                        likeButton.classList.add('bg-primary');
                    }
                } else {
                    notice(data.message, data.status === true ? "success" : "error");
                }
            } else {
                console.error('Yêu cầu thất bại. Mã lỗi: ' + xhr.status);
            }
        };

        xhr.send('postId=' + postId + '&action=' + action);
    });

</script>