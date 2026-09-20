<div class="body-wrapper">
    <div class="container-fluid">
        <div class="card bg-light-info shadow-none position-relative overflow-hidden">
            <div class="card-body px-4 py-3">
                <div class="row align-items-center">
                    <div class="col-9">
                        <h5 class="fw-semibold mb-8">Trang Tải Game</h5>
                    </div>
                    <p>Tất cả phiên bản dưới đều sử dụng được!</p>
                    <p>Hãy chọn phiên bản phù hợp với thiết bị mà bạn đang sử dụng</p>
                    <p>Chúc các bạn chơi game vui vẻ!</p>
                </div>
            </div>
        </div>
        <div class="row">
            <?php
            $folderPath = 'Upload';
            $files = scandir($folderPath);
            $files = array_diff($files, array('.', '..'));

            // ánh xạ tên file -> tên hiển thị
            $nameMap = [
                "NroVN.apk" => "Android",
                "NroVN.ipa" => "IOS",
                "NroVN.jar" => "Java",
                "NroVN.zip" => "Window"
            ];
            ?>
            <?php foreach ($files as $file): ?>
                <?php
                $filePath = $folderPath . '/' . $file;
                $displayName = isset($nameMap[$file]) ? $nameMap[$file] : pathinfo($file, PATHINFO_FILENAME);
                $fileExt = pathinfo($file, PATHINFO_EXTENSION);
                ?>
                <div class="col-md-6 col-xl-4">
                    <div class="card">
                        <div class="card-body p-4 d-flex align-items-center gap-3">
                            <div>
                                <h5 class="fw-semibold mb-0">
                                    <?= $displayName ?>
                                </h5>
                                <span class="fs-2 d-flex align-items-center">
                                    <i class="ti ti-download text-danger fs-3 me-1"></i>
                                    <?= strtoupper($fileExt) ?>
                                </span>
                            </div>
                            <a href="<?= $filePath ?>" 
                               class="btn btn-outline-<?php echo RandomString(array("primary", "secondary", "success", "danger", "warning", "info", "dark")); ?> py-1 px-2 ms-auto"
                               download>
                               Tải Ngay
                            </a>
                        </div>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>
    </div>
</div>