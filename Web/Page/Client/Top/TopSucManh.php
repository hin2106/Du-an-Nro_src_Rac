<div class="body-wrapper">
    <div class="container-fluid">
        <div class="row">
            <div class="col-lg-12">
                <div class="card bg-light-danger shadow-none position-relative overflow-hidden">
                    <div class="card-body px-4 py-3">
                        <div class="row align-items-center">
                            <div class="col-9">
                                <h5 class="fw-semibold mb-8">ĐUA TOP SỨC MẠNH SERVER
                                </h5>
                            </div>
                            
                            <p>Chúc các bạn chơi game vui vẻ!</p>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Top Sức Mạnh Sư Phụ -->
            <div class="col-lg-12 d-flex align-items-strech">
                <div class="card w-100">
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table align-middle text-nowrap mb-0">
                                <thead>
                                    <tr class="text-muted fw-semibold">
                                        <th scope="col" class="ps-0">TOP</th>
                                        <th scope="col">NHÂN VẬT</th>
                                        <th scope="col">SỨC MẠNH</th>
                                        <th scope="col">ĐỆ TỬ</th>
                                    </tr>
                                </thead>
                                <tbody class="border-top">
                                    <?php
                                    $query = $CVH->query("SELECT player.*, account.tongnap,
                                    CAST(JSON_UNQUOTE(JSON_EXTRACT(data_point, '$[1]')) AS SIGNED) AS sucmanh, 
                                    SUBSTRING_INDEX(SUBSTRING_INDEX(JSON_UNQUOTE(JSON_EXTRACT(pet, '$[1]')), ',', 2), ',', -1) AS detu
                                    FROM player 
                                    INNER JOIN account ON player.account_id = account.id                       
                                    ORDER BY sucmanh DESC 
                                    LIMIT 20;
                                    ");
                                    $i = 1;
                                    if (mysqli_num_rows($query) > 0) {
                                        while ($row = mysqli_fetch_assoc($query)) {
                                            ?>
                                            <tr>
                                                <td class="ps-0">
                                                    <span
                                                        class="badge rounded-pill ms-auto bg-<?php echo RandomString(array("primary", "secondary", "success", "danger", "warning", "info", "dark")); ?>">
                                                        <?php echo $i++; ?>
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="d-flex align-items-center">
                                                        <div class="me-2 pe-1">
                                                            <img src="/Assets/images/avatar/<?php echo $row["head"]; ?>.png"
                                                                width="30" alt="" />
                                                        </div>
                                                        <div>
                                                            <h6 class="fw-semibold mb-1">
                                                                <?php echo $row["name"]; ?>
                                                            </h6>
                                                            <p class="fs-2 mb-0 text-danger ">Hành tinh:
                                                                <?php echo checkGender($row["gender"]); ?>
                                                            </p>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <h6 class="fw-semibold mb-1">
                                                        <?php
                                                        $data = json_decode($CVH->player($row['account_id'], true)["data_point"]);
                                                        echo fNumber($data[1]);
                                                        ?> sức mạnh
                                                    </h6>
                                                    <p class="fs-2 text-danger mb-0">HPG:
                                                        <?php echo fNumber($data[5]); ?> - MPG:
                                                        <?php echo fNumber($data[6]); ?> - DMG:
                                                        <?php echo fNumber($data[7]); ?>
                                                    </p>
                                                </td>
                                                <td>
                                                    <?php if ($row['detu']) { ?>
                                                        <h6 class="fw-semibold mb-1">
                                                            <?php echo fNumber($row['detu']); ?> sức mạnh
                                                        </h6>
                                                        <p class="fs-2 text-danger mb-0">HPG:
                                                            <?php $dataz = json_decode($row["pet"]); ?>
                                                            <?php echo fNumber(json_decode($dataz[1], true)[5]); ?> - MPG:
                                                            <?php echo fNumber(json_decode($dataz[1], true)[6]); ?> - DMG:
                                                            <?php echo fNumber(json_decode($dataz[1], true)[7]); ?>
                                                        </p>
                                                    <?php }else{ ?>
                                                        <p><b>Không có đệ tử</b></p>
                                                    <?php } ?>
                                                </td>
                                            </tr>
                                        <?php }
                                    } else { ?>
                                        <tr class="text-center">
                                            <td colspan='4'>
                                                <img src="https://cdn-icons-png.flaticon.com/128/7466/7466139.png"
                                                    width="50" class="img-fluid">
                                                <p class="pt-3"><b>Không có dữ liệu</b></p>
                                            </td>
                                        </tr>
                                    <?php } ?>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</div>
</div>