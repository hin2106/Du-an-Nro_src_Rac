<?php
if (!$user) {
    $BLOCK_JOIN = false;
    if ($BLOCK_JOIN === false) {
        echo '<script type="text/javascript">window.location.href = "/";</script>';
        exit();
    }
}
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
?>
<div class="center">
<div class="body-wrapper">
    <div class="container-fluid">
                <div class="card w-100">
                    <div class="card-body">
                        <div class="mb-2">
                            <h5 class="mb-0 card-title text-center">Donate cho game</h5>
                            <hr />
							<div class="the-p"
								<p>Chuyển khoản vui lòng ghi nội dung tên nhân vật trong game</p>
							</div>
                        </div>
                        <img src="<?php echo $homeurl; ?>/Assets/images/Bank/QR_Bank.png" alt="QR_Bank" width="300" height="300">
                    </div>
                </div>
    </div>
</div>
</div>