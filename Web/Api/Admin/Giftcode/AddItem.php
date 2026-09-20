<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
$item_index = $_POST['item_index'] ?? 0;
?>
<div class="card item-block mb-3" data-item-index="<?= $item_index; ?>">
    <div class="card-header bg-light">
        <div class="d-flex justify-content-between align-items-center">
            <h6 class="mb-0">Vật phẩm #<?= $item_index + 1; ?></h6>
            <button type="button" class="btn btn-sm btn-danger" onclick="removeItem(<?= $item_index; ?>)">
                <i class="ti ti-trash"></i> Xóa vật phẩm
            </button>
        </div>
    </div>
    <div class="card-body">
        <div class="row">
            <div class="col-sm-12 col-md-6">
                <div class="input-group mb-3">
                    <select class="select2 form-control custom-select col-12" name="items[<?= $item_index; ?>][item_id]" required>
                        <option value="">Chọn ID Vật Phẩm</option>
                        <?php
                        $query = $CVH->query("SELECT * FROM `item_template`");
                        if (mysqli_num_rows($query) > 0) {
                            while ($row = mysqli_fetch_assoc($query)) {
                                ?>
                                <option value="<?php echo $row['id']; ?>">
                                    <?php echo $row['id']; ?> - <?php echo $row['NAME']; ?>
                                </option>
                            <?php }
                        } else { ?>
                            <option value="">Không còn dữ liệu nào</option>
                        <?php } ?>
                    </select>
                </div>
            </div>
            <div class="col-sm-12 col-md-4">
                <div class="input-group mb-3">
                    <input type="number" class="form-control" name="items[<?= $item_index; ?>][quantity]" placeholder="Số lượng" min="1" required>
                </div>
            </div>
            <div class="col-sm-12 col-md-2">
                <div class="input-group mb-3">
                    <button class="btn btn-success w-100" type="button" onclick="addOptionToItem(<?= $item_index; ?>)">
                        <i class="ti ti-plus"></i> Option
                    </button>
                </div>
            </div>
        </div>
        
        <!-- Container cho các option của vật phẩm này -->
        <div class="options-container-<?= $item_index; ?> mt-3">
            <label class="form-label">Options:</label>
            <!-- Option mặc định sẽ được thêm ở đây bằng JavaScript -->
        </div>
    </div>
</div>
<script>
    $(".select2").select2();
    
    // Thêm option mặc định khi vật phẩm mới được tạo
    $(document).ready(function() {
        addOptionToItem(<?= $item_index; ?>);
    });
</script>