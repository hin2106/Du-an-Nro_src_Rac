<?php
if (!$user) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}

?>
<Center>
    <div class="body-wrapper">
        <div class="container-fluid">
            <div class="col-lg-4">
                <div class="card w-100">
                    <div class="card-body">
                        <div class="mb-2">
                            <h5 class="mb-0 card-title text-center">NẠP NGỌC QUA BANK</h5>
                            <p class="text-center text-muted fs-2 mb-0">1.000đ = 1 ngọc &bull; Ngọc được cộng tự động vào game</p>
                            <hr />
                        </div>
                        <form id="bank-recharge-form" method="POST" action="/Api/User/NapBank" class="pt-3">
    						<div class="mb-3">
                            <select name="amount" class="form-control form-select" required>
    								<option value="">--Chọn gói nạp--</option>
    								<option value="10000">10 ngọc &mdash; 10.000đ</option>
    								<option value="20000">20 ngọc &mdash; 20.000đ</option>
    								<option value="50000">50 ngọc &mdash; 50.000đ</option>
    								<option value="100000">100 ngọc &mdash; 100.000đ</option>
    								<option value="200000">200 ngọc &mdash; 200.000đ</option>
    								<option value="500000">500 ngọc &mdash; 500.000đ</option>
    								<option value="1000000">1.000 ngọc &mdash; 1.000.000đ</option>
    							</select>
    						</div>
    						<div class="mb-3">
    							<button type="submit" class="btn btn-danger w-100">
    								Nạp Ngọc Ngay
    							</button>
    						</div>
    					</form>

    					<div class="alert alert-light border mt-3 mb-0">
    						<div class="fw-semibold mb-1">Lưu ý</div>
                            <div class="small text-muted">Sau khi bấm nạp, mã QR sẽ hiện ngay. Khi thanh toán thành công, popup tự cập nhật trạng thái mà không cần tải lại trang.</div>
                            <div class="small text-warning mt-1">Nếu game server chưa chạy, giao dịch vẫn ghi nhận thành công nhưng ngọc sẽ được cộng khi game server xử lý hàng đợi.</div>
    					</div>

                    </div>
                </div>
            </div>
        </div>
    </div>
</Center>

    <div class="modal fade" id="qrPaymentModal" tabindex="-1" aria-labelledby="qrPaymentModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="qrPaymentModalLabel">Quét QR để thanh toán</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <div class="text-center mb-3">
                        <img id="qrPaymentImage" src="" alt="QR thanh toán" class="img-fluid" style="max-width:280px;">
                    </div>

                    <div class="small text-muted mb-2" id="qrCheckoutLinkWrap" style="display:none;">
                        Nếu app ngân hàng không quét được, mở liên kết thanh toán:
                        <a id="qrCheckoutLink" href="#" target="_blank" rel="noopener">Mở cổng thanh toán</a>
                    </div>

                    <div class="border rounded p-2 mb-2">
                        <div class="d-flex justify-content-between"><span>Mã đơn</span><strong id="qrOrderCode">-</strong></div>
                        <div class="d-flex justify-content-between"><span>Số tiền</span><strong id="qrAmount">-</strong></div>
                        <div class="d-flex justify-content-between"><span>Trạng thái</span><strong id="qrStatusText" class="text-warning">Đang chờ thanh toán</strong></div>
                        <div class="d-flex justify-content-between"><span>Thời gian còn lại</span><strong id="qrCountdown">--:--</strong></div>
                    </div>

                    <div id="qrSuccessAlert" class="alert alert-success mb-0" style="display:none;">
                        Thanh toán thành công. Hệ thống đã ghi nhận giao dịch của bạn.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" id="btnIHavePaid" class="btn btn-primary">Tôi đã chuyển khoản</button>
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                </div>
            </div>
        </div>
    </div>

<script>
$(document).ready(function() {
        var activeOrderCode = null;
        var pollingTimer = null;
        var qrModalEl = document.getElementById('qrPaymentModal');
        var qrModal = qrModalEl ? new bootstrap.Modal(qrModalEl) : null;

        function formatMoney(vnd) {
            var value = parseInt(vnd || 0, 10);
            return value.toLocaleString('vi-VN') + 'đ';
        }

        function formatCountdown(totalSeconds) {
            var sec = Math.max(0, parseInt(totalSeconds || 0, 10));
            var min = Math.floor(sec / 60);
            var rem = sec % 60;
            return String(min).padStart(2, '0') + ':' + String(rem).padStart(2, '0');
        }

        function setStatusUI(status, remainingSeconds) {
            var $status = $('#qrStatusText');
            var $countdown = $('#qrCountdown');
            var st = String(status || 'pending').toLowerCase();
            $status.removeClass('text-warning text-success text-danger text-secondary');

            if (st === 'paid') {
                $status.addClass('text-success').text('Đã thanh toán');
                $('#qrSuccessAlert').show();
                $('#btnIHavePaid').prop('disabled', true).text('Đã xác nhận');
                $countdown.text('00:00');
                return;
            }

            $('#qrSuccessAlert').hide();
            $('#btnIHavePaid').prop('disabled', false).text('Tôi đã chuyển khoản');

            if (st === 'expired') {
                $status.addClass('text-secondary').text('Hết hạn thanh toán');
                $countdown.text('00:00');
                return;
            }

            if (st === 'cancelled' || st === 'failed') {
                $status.addClass('text-danger').text('Thanh toán thất bại/đã hủy');
                $countdown.text(formatCountdown(remainingSeconds));
                return;
            }

            $status.addClass('text-warning').text('Đang chờ thanh toán');
            $countdown.text(formatCountdown(remainingSeconds));
        }

        function stopPolling() {
            if (pollingTimer) {
                clearInterval(pollingTimer);
                pollingTimer = null;
            }
        }

        function checkPaymentStatus(silent) {
            if (!activeOrderCode) return;

            $.ajax({
                type: 'POST',
                url: '/Api/User/PaymentStatus',
                data: { orderCode: activeOrderCode },
                dataType: 'json',
                success: function(res) {
                    if (!res || !res.success) {
                        if (!silent) {
                            toastr.warning((res && res.message) || 'Không thể lấy trạng thái đơn hàng');
                        }
                        return;
                    }

                    setStatusUI(res.status, res.remainingSeconds);
                    if (res.status === 'paid' || res.status === 'expired' || res.status === 'cancelled' || res.status === 'failed') {
                        stopPolling();
                        if (res.status === 'paid') {
                            toastr.success('Thanh toán thành công. Ngọc sẽ cộng ngay khi game server xử lý.', 'Thông báo', { closeButton: true });
                        }
                    }
                },
                error: function() {
                    if (!silent) {
                        toastr.warning('Tạm thời không kiểm tra được trạng thái thanh toán.');
                    }
                }
            });
        }

        function startPolling() {
            stopPolling();
            checkPaymentStatus(true);
            pollingTimer = setInterval(function() {
                checkPaymentStatus(true);
            }, 3000);
        }

        if (qrModalEl) {
            qrModalEl.addEventListener('hidden.bs.modal', function() {
                stopPolling();
            });
        }

        $('#btnIHavePaid').on('click', function() {
            checkPaymentStatus(false);
        });

        $('#bank-recharge-form').off('submit').on('submit', function(e) {
        e.preventDefault();
        
        var form = $(this);
        var url = form.attr('action');
        var method = form.attr('method');
        var data = form.serialize();
        var button = form.find('button[type="submit"]');
        
        // Lưu text button gốc
        var originalText = button.text();
        
        // Vô hiệu hóa button và đổi text
        button.prop('disabled', true).text('Đang xử lý...');
        
        $.ajax({
            type: method,
            url: url,
            data: data,
            dataType: 'json',
            success: function(response) {
                if (response.success && response.orderCode) {
                    activeOrderCode = response.orderCode;

                    $('#qrOrderCode').text(String(response.orderCode));
                    $('#qrAmount').text(formatMoney(response.amount || 0));
                    $('#qrPaymentImage').attr('src', response.qrImageUrl || '');

                    if (response.paymentUrl) {
                        $('#qrCheckoutLink').attr('href', response.paymentUrl);
                        $('#qrCheckoutLinkWrap').show();
                    } else {
                        $('#qrCheckoutLinkWrap').hide();
                    }

                    setStatusUI(response.status || 'pending', 15 * 60);
                    if (qrModal) {
                        qrModal.show();
                    }
                    startPolling();
                } else {
                    toastr.error(response.message || 'Có lỗi xảy ra!', 'Thông báo', {closeButton: true});
                }
            },
            error: function(xhr, status, error) {
                toastr.error('Có lỗi xảy ra khi kết nối đến server!', 'Thông báo', {closeButton: true});
            },
            complete: function() {
                button.prop('disabled', false).text(originalText);
            }
        });
    });
});
</script>