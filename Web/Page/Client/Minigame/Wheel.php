<?php
if ($setting["vongquay"] == false) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}
?>
<style>
    .line-center {
        width: 90px;
        height: 3px;
        background-color: #32c5d2;
        margin: 0 auto 30px auto;
    }
</style>
<div class="body-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <div class="card w-100">
                    <div class="card-body">
                        <h2 class="fw-semibold text-center">Vòng Quay Thỏi Vàng</h2>
                        <div class="line-center"></div>
                        <div class="container">
                            <div class="row justify-content-center">
                                <div class="col-md-6">
                                    <div class="v-luckywheel w-100 position-relative overflow-x-hidden">
                                        <div class="d-flex justify-content-center">
                                            <div class="wheel-wrapper">
                                                <a href="javascript:;" id="play" class="start-played wheel-pointer"></a>
                                                <div class="wheel-bg">
                                                    <img src="https://cdn.upanh.info/storage/upload/minigame-config-VnZ0dkYwUkJ5SWY4cDE0cE55UDA1UT09/images/vqvang1.png"
                                                        id="rotate-play" class="img-fluid">
                                                </div>
                                            </div>
                                        </div>
                                        <div class="d-flex justify-content-center">
                                            <select id="numrolllop"
                                                class="form-select border-1 w-70 bg-white border-warning rounded focus:outline-none">
                                                <option value="1">Quay 1 lần -
                                                    <?php echo number_format(60000); ?>đ
                                                </option>
                                                <option value="1">Quay 3 lần -
                                                    <?php echo number_format(60000 * 3); ?>đ
                                                </option>
                                                <option value="1">Quay 5 lần -
                                                    <?php echo number_format(60000 * 5); ?>đ
                                                </option>
                                                <option value="1">Quay 10 lần -
                                                    <?php echo number_format(60000 * 10); ?>đ
                                                </option>
                                            </select>
                                        </div>
                                        <div class="my-2 d-flex justify-content-center align-items-center gap-2">
                                            <button type="button" id="play" class="start-played btn btn-danger">
                                                Quay Ngay
                                            </button>
                                            <button type="button" id="try" class="start-played btn btn-info">
                                                Chơi Thử
                                            </button>
                                        </div>
                                        <div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
<div class="modal fade" id="modalMinigame" tabindex="-1" aria-modal="true" role="dialog">
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
<script type="text/javascript">

    $(document).ready(function (e) {
        var roll_check = true;
        var num_loop = 4;
        var angle_gift = '';
        var num_gift = 8;
        var gift_detail = '';
        var num_roll_remain = 0;
        var angles = 0;
        $('body').delegate('.start-played', 'click', function () {
            if (roll_check) {
                typeRoll = this.id;
                numrolllop = $("#numrolllop").val();
                type = 'vong-quay-robux';
                roll_check = false;
                $.ajax({
                    url: '/Minigame/Whell',
                    datatype: 'json',
                    data: {
                        numrolllop,
                        typeRoll,
                        type
                    },
                    type: 'POST',
                    success: function (data) {
                        if (data.status == 'error') {
                            roll_check = true;
                            $('#rotate-play').css({ "transform": "rotate(0deg)" });
                            $('#content').text(data.msg);
                            $('#modalMinigame').modal('show');
                            return;
                        }
                        if (data.status == 'login') {
                            $('#content').text(data.msg);
                            $('#modalMinigame').modal('show');
                            return;
                        }
                        gift_detail = data.msg;
                        gift_revice = data.arr_gift;
                        gift_total = data.total;
                        gift_price = data.price;
                        num_roll_remain = gift_detail.num_roll_remain;
                        $('#rotate-play').css({ "transform": "rotate(0deg)" });
                        angles = 0;
                        angle_gift = gift_detail.pos * (360 / num_gift);
                        loop();
                    },
                    error: function () {
                        $('#content').text('Có lỗi xảy ra. Vui lòng thử lại!');
                        $('#modalMinigame').modal('show');
                    }
                })
            }
        });


        function loop() {
            $('#rotate-play').css({ "transform": "rotate(" + angles + "deg)" });

            if ((parseInt(angles) - 25) <= -(((num_loop * 360) + angle_gift))) {
                angles = parseInt(angles) - 2;
            } else {
                angles = parseInt(angles) - 25;
            }

            if (angles >= -((num_loop * 360) + angle_gift)) {
                requestAnimationFrame(loop);
            }
            else {
                roll_check = true;
                if (gift_revice.length > 0) {
                    $html = "";
                    if (typeRoll == "play") {
                        $html += "<p>Kết quả <span class='text-red-600' style='font-weight:bold'>( Quay Thật ):</span> Quay " + gift_revice.length + " lần - giá " + gift_price + "đ</p>";
                        $html += "<div class='h-2'></div>";
                        for ($i = 0; $i < gift_revice.length; $i++) {
                            $html += "<p class='text-md'>- Quay lần " + ($i + 1) + ": " + gift_revice[$i]["title"];
                        }
                    } else {
                        $html += "<p>Kết quả <span class='text-red-600' style='font-weight:bold'>( Quay Thử ):</span> Quay " + gift_revice.length + " lần - giá " + gift_price + "đ</p>";
                        $html += "<div class='h-2'></div>";
                        for ($i = 0; $i < gift_revice.length; $i++) {
                            $html += "<p class='text-md'>- Quay lần " + ($i + 1) + ": " + gift_revice[$i]["title"];
                        }
                    }
                }
                $('#content').html($html);
                $('#modalMinigame').modal('show');
            }
        }
    });
</script>