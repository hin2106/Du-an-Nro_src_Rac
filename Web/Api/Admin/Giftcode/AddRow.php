<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
$code = md5(rand_string(10));
$item_index = $_POST['item_index'] ?? 0;
$option_index = $_POST['option_index'] ?? 0;
?>
<div class="row form_gift" data-row="<?= $code; ?>" data-item-index="<?= $item_index; ?>">
    <div class="col-sm-12 col-md-5">
        <div class="input-group mb-3">
            <select class="select2 form-control custom-select col-12" name="items[<?= $item_index; ?>][options][<?= $option_index; ?>][id]">
                <option value="">Chọn ID Option</option>
                <?php
                $query = $CVH->query("SELECT * FROM `item_option_template`");
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
    <div class="col-sm-12 col-md-5">
        <div class="input-group mb-3">
            <input type="number" class="form-control" name="items[<?= $item_index; ?>][options][<?= $option_index; ?>][param]" placeholder="Param Option" value="0">
        </div>
    </div>
    <div class="col-sm-12 col-md-2">
        <div class="input-group mb-3">
            <button class="btn btn-danger w-100" onclick="removeOption('<?= $code; ?>')" type="button">
                <i class="ti ti-minus"></i>
            </button>
        </div>
    </div>
</div>
<script>
    $(".select2").select2();
</script>