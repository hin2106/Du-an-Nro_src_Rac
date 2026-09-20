<?php
// BẬT HIỂN THỊ LỖI - RẤT QUAN TRỌNG
error_reporting(E_ALL);
ini_set('display_errors', 1);

// KIỂM TRA BIẾN VÀ KẾT NỐI CƠ BẢN
try {
    if (!isset($CVH)) {
        throw new Exception("Biến CVH không tồn tại");
    }
    
    if (!function_exists('getCurrentURL')) {
        function getCurrentURL() {
            return $_SERVER['REQUEST_URI'];
        }
    }
    
    $kmess = 16;
    $page = isset($_REQUEST['page']) && $_REQUEST['page'] > 0 ? intval($_REQUEST['page']) : 1;
    $start = isset($_REQUEST['page']) ? $page * $kmess - $kmess : (isset($_GET['start']) ? abs(intval($_GET['start'])) : 0);
    
    // THỬ KẾT NỐI DATABASE
    $db = $CVH->connect_db();
    if (!$db) {
        throw new Exception("Không thể kết nối database");
    }
    
    // KIỂM TRA SCHEMA GIFTCODE (mới hoặc legacy)
    $check_table = mysqli_query($db, "SHOW TABLES LIKE 'giftcode'");
    $check_legacy_table = mysqli_query($db, "SHOW TABLES LIKE 'cvh_giftcode'");
    $giftcode_schema = 'new';

    if ($check_table && mysqli_num_rows($check_table) > 0) {
        $giftcode_schema = 'new';
    } elseif ($check_legacy_table && mysqli_num_rows($check_legacy_table) > 0) {
        $giftcode_schema = 'legacy';
    } else {
        throw new Exception("Không tìm thấy bảng 'giftcode' hoặc 'cvh_giftcode' trong database");
    }

    // THỰC HIỆN TRUY VẤN
    if ($giftcode_schema === 'new') {
        $query = "SELECT `id`, `code`, `count_left`, `detail`, `datecreate`, `expired` FROM `giftcode` ORDER BY `datecreate` DESC LIMIT $start, $kmess";
        $count_query = "SELECT COUNT(*) as total FROM `giftcode`";
    } else {
        $query = "SELECT `id`, `code`, `luot` AS `count_left`, `item` AS `detail`, FROM_UNIXTIME(`time`) AS `datecreate`, CONCAT(`hsd`, ' 23:59:59') AS `expired` FROM `cvh_giftcode` ORDER BY `time` DESC LIMIT $start, $kmess";
        $count_query = "SELECT COUNT(*) as total FROM `cvh_giftcode`";
    }

    $result = mysqli_query($db, $query);
    if (!$result) {
        throw new Exception("Lỗi truy vấn: " . mysqli_error($db));
    }

    $count_result = mysqli_query($db, $count_query);
    if ($count_result) {
        $count_row = mysqli_fetch_assoc($count_result);
        $tong = $count_row['total'];
    } else {
        $tong = 0;
    }
    
} catch (Exception $e) {
    $fatal_error = $e->getMessage();
}

// NẾU CÓ LỖI NGHIÊM TRỌNG, HIỂN THỊ VÀ DỪNG LẠI
if (isset($fatal_error)) {
    echo "<div class='alert alert-danger m-3'><strong>Lỗi nghiêm trọng:</strong> " . htmlspecialchars($fatal_error) . "</div>";
    exit;
}
?>

<div class="body-wrapper">
    <div class="container-fluid note-has-grid">
        <div class="row">
            <div class="col-sm-12">
                <!-- FORM TẠO GIFTCODE -->
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">Tạo Giftcode Mới</h4>
                        <form class="row" method="POST" action="/Api/Admin/Giftcode/AddGift" id="giftcodeForm">
                            <!-- Thông tin giftcode -->
                            <div class="col-sm-12 col-md-4">
                                <div class="input-group mb-3">
                                    <input type="text" class="form-control" name="code" placeholder="Mã code (rd để tạo ngẫu nhiên)" required>
                                </div>
                            </div>
                            <div class="col-sm-12 col-md-4">
                                <div class="input-group mb-3">
                                    <input type="number" class="form-control" name="count" placeholder="Số lượt nhập" min="1" required>
                                </div>
                            </div>
                            <div class="col-sm-12 col-md-4">
                                <div class="input-group mb-3">
                                    <input type="datetime-local" class="form-control" name="expired" required>
                                </div>
                            </div>

                            <!-- Danh sách vật phẩm -->
                            <div class="col-12">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <h5>Danh sách vật phẩm</h5>
                                    <button type="button" class="btn btn-primary" onclick="addNewItem()">
                                        <i class="ti ti-plus"></i> Thêm vật phẩm
                                    </button>
                                </div>
                                
                                <div id="items_container">
                                    <!-- Vật phẩm sẽ được thêm vào đây -->
                                </div>
                            </div>

                            <div class="col-12 mt-4">
                                <button type="submit" class="btn btn-success">Tạo Giftcode</button>
                                <button type="button" class="btn btn-secondary" onclick="window.location.reload()">Làm mới</button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- DANH SÁCH GIFTCODE -->
                <div class="card mt-4">
                    <div class="card-body">
                        <h4 class="card-title">Danh Sách Giftcode</h4>
                        
                        <?php if (mysqli_num_rows($result) > 0): ?>
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>MÃ CODE</th>
                                        <th>SỐ LƯỢT CÒN LẠI</th>
                                        <th>PHẦN THƯỞNG</th>
                                        <th>NGÀY TẠO</th>
                                        <th>HẾT HẠN</th>
                                        <th>THAO TÁC</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <?php
                                    while ($row = mysqli_fetch_assoc($result)) {
                                        // Xử lý hiển thị phần thưởng an toàn
                                        $reward_display = "Lỗi đọc dữ liệu";
                                        $reward_tooltip = "";
                                        try {
                                            $detail = json_decode($row["detail"], true);
                                            if (is_array($detail) && count($detail) > 0) {
                                                $first_item = $detail[0];
                                                $firstQty = isset($first_item['quantity']) ? $first_item['quantity'] : (isset($first_item['soluong']) ? $first_item['soluong'] : 0);
                                                $reward_display = "ID " . $first_item['id'] . " x" . $firstQty;
                                                if (count($detail) > 1) {
                                                    $reward_display .= " +" . (count($detail) - 1) . " món khác";
                                                }
                                                
                                                // Tạo tooltip chi tiết
                                                $reward_tooltip = "Chi tiết phần thưởng:\n";
                                                foreach ($detail as $index => $item) {
                                                    $itemQty = isset($item['quantity']) ? $item['quantity'] : (isset($item['soluong']) ? $item['soluong'] : 0);
                                                    $reward_tooltip .= ($index + 1) . ". ID " . $item['id'] . " x" . $itemQty;
                                                    if (isset($item['options']) && is_array($item['options'])) {
                                                        $reward_tooltip .= " (Options: ";
                                                        foreach ($item['options'] as $opt) {
                                                            $reward_tooltip .= "ID:" . $opt['id'] . "=" . $opt['param'] . " ";
                                                        }
                                                        $reward_tooltip .= ")";
                                                    }
                                                    $reward_tooltip .= "\n";
                                                }
                                            } else {
                                                $reward_display = "Không có dữ liệu";
                                            }
                                        } catch (Exception $e) {
                                            $reward_display = "Lỗi parse JSON";
                                        }
                                        ?>
                                        <tr>
                                            <td><?php echo $row['id']; ?></td>
                                            <td><code><?php echo htmlspecialchars($row['code']); ?></code></td>
                                            <td>
                                                <span class="editable-count badge bg-info cursor-pointer" 
                                                      data-id="<?php echo $row['id']; ?>" 
                                                      data-value="<?php echo $row['count_left']; ?>"
                                                      title="Click để sửa số lượt">
                                                    <?php echo $row['count_left']; ?>
                                                </span>
                                            </td>
                                            <td>
                                                <span class="reward-tooltip" title="<?php echo htmlspecialchars($reward_tooltip); ?>">
                                                    <?php echo $reward_display; ?>
                                                </span>
                                            </td>
                                            <td><?php echo date('d/m/Y H:i', strtotime($row['datecreate'])); ?></td>
                                            <td><?php echo date('d/m/Y H:i', strtotime($row['expired'])); ?></td>
                                            <td>
                                                <button class="btn btn-sm btn-danger" onclick="deleteGiftcode(<?php echo $row['id']; ?>)">
                                                    <i class="ti ti-trash"></i> Xóa
                                                </button>
                                            </td>
                                        </tr>
                                        <?php
                                    }
                                    ?>
                                </tbody>
                            </table>
                        </div>
                        
                        <!-- PHÂN TRANG -->
                        <?php if ($tong > $kmess): ?>
                        <div class="mt-3">
                            <?php echo $CVH->phantrang('/admin/giftcode/', $start, $tong, $kmess); ?>
                        </div>
                        <?php endif; ?>
                        
                        <?php else: ?>
                        <div class="text-center py-4">
                            <img src="https://cdn-icons-png.flaticon.com/128/7466/7466139.png" width="80" class="img-fluid mb-3">
                            <p class="text-muted">Chưa có giftcode nào được tạo</p>
                        </div>
                        <?php endif; ?>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
let itemCounter = 0;
let optionCounters = {}; // Lưu số lượng option cho mỗi vật phẩm

// Khởi tạo trang - thêm vật phẩm đầu tiên
$(document).ready(function() {
    addNewItem();
    
    // Khởi tạo tooltip
    $('[title]').tooltip();
});

// Thêm vật phẩm mới
function addNewItem() {
    $.ajax({
        url: '/Api/Admin/Giftcode/AddItem',
        method: 'POST',
        data: { item_index: itemCounter },
        beforeSend: function() {
            $('#items_container').append('<div class="text-center py-3"><div class="spinner-border" role="status"></div></div>');
        },
        success: function(response) {
            $('#items_container .text-center').remove();
            $('#items_container').append(response);
            optionCounters[itemCounter] = 0; // Khởi tạo counter cho vật phẩm mới
            itemCounter++;
            
            // Re-init select2 cho select mới
            $('.select2').select2();
        },
        error: function() {
            $('#items_container .text-center').remove();
            alert('Lỗi khi thêm vật phẩm');
        }
    });
}

// Xóa vật phẩm
function removeItem(itemIndex) {
    if ($('.item-block').length <= 1) {
        alert('Cần ít nhất một vật phẩm');
        return;
    }
    
    $(`.item-block[data-item-index="${itemIndex}"]`).remove();
    delete optionCounters[itemIndex];
}

// Thêm option cho vật phẩm
function addOptionToItem(itemIndex) {
    if (!optionCounters[itemIndex]) {
        optionCounters[itemIndex] = 0;
    }
    
    const optionIndex = optionCounters[itemIndex];
    
    $.ajax({
        url: '/Api/Admin/Giftcode/AddRow',
        method: 'POST',
        data: { 
            item_index: itemIndex,
            option_index: optionIndex
        },
        success: function(response) {
            $(`.options-container-${itemIndex}`).append(response);
            optionCounters[itemIndex]++;
            
            // Re-init select2 cho select mới
            $('.select2').select2();
        },
        error: function() {
            alert('Lỗi khi thêm option');
        }
    });
}

// Xóa option
function removeOption(code) {
    $(`.form_gift[data-row="${code}"]`).remove();
}

// Xóa giftcode
function deleteGiftcode(id) {
    if (confirm('Bạn có chắc muốn xóa giftcode này?')) {
        $.ajax({
            url: '/Api/Admin/Giftcode/Delete',
            method: 'POST',
            data: {
                type: 'Del_Gift',
                id: id
            },
            success: function(response) {
                try {
                    const data = JSON.parse(response);
                    if (data.status) {
                        alert('Xóa thành công!');
                        window.location.reload();
                    } else {
                        alert('Lỗi: ' + data.message);
                    }
                } catch (e) {
                    alert('Lỗi xử lý dữ liệu');
                }
            },
            error: function() {
                alert('Lỗi kết nối');
            }
        });
    }
}

// Chức năng sửa số lượt trực tiếp
$(document).on('click', '.editable-count', function() {
    const element = $(this);
    const currentValue = element.data('value');
    const id = element.data('id');
    
    const newValue = prompt('Nhập số lượt mới:', currentValue);
    if (newValue !== null && newValue !== '' && !isNaN(newValue) && newValue >= 0) {
        $.ajax({
            url: '/Api/Admin/Giftcode/Edit',
            method: 'POST',
            data: { 
                type: 'EditCount', 
                id: id, 
                number: newValue 
            },
            success: function(response) {
                try {
                    const data = JSON.parse(response);
                    if (data.status) {
                        element.text(newValue);
                        element.data('value', newValue);
                        alert('Cập nhật thành công!');
                    } else {
                        alert('Lỗi: ' + data.message);
                    }
                } catch (e) {
                    alert('Lỗi xử lý dữ liệu');
                }
            },
            error: function() {
                alert('Lỗi kết nối');
            }
        });
    }
});

// Xử lý submit form
$('#giftcodeForm').on('submit', function(e) {
    e.preventDefault();
    
    // Validate ít nhất một vật phẩm
    if ($('.item-block').length === 0) {
        alert('Vui lòng thêm ít nhất một vật phẩm');
        return;
    }
    
    // Validate từng vật phẩm
    let valid = true;
    $('.item-block').each(function() {
        const itemId = $(this).find('[name*="[item_id]"]').val();
        const quantity = $(this).find('[name*="[quantity]"]').val();
        
        if (!itemId || !quantity) {
            valid = false;
            $(this).addClass('border border-danger');
        } else {
            $(this).removeClass('border border-danger');
        }
    });
    
    if (!valid) {
        alert('Vui lòng điền đầy đủ thông tin cho tất cả vật phẩm');
        return;
    }
    
    // Hiển thị loading
    const submitBtn = $(this).find('button[type="submit"]');
    const originalText = submitBtn.html();
    submitBtn.html('<i class="ti ti-loader"></i> Đang xử lý...');
    submitBtn.prop('disabled', true);
    
    // Gửi form
    $.ajax({
        url: $(this).attr('action'),
        method: 'POST',
        data: $(this).serialize(),
        success: function(response) {
            try {
                const data = JSON.parse(response);
                if (data.status) {
                    alert('Tạo giftcode thành công!');
                    window.location.reload();
                } else {
                    alert('Lỗi: ' + data.message);
                }
            } catch (e) {
                alert('Lỗi xử lý dữ liệu');
            }
        },
        error: function() {
            alert('Lỗi kết nối');
        },
        complete: function() {
            submitBtn.html(originalText);
            submitBtn.prop('disabled', false);
        }
    });
});
</script>