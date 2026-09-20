<?php
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";
$output = '';
if (isset($_POST['query'])) {
    $search = mysqli_real_escape_string($CVH->connect_db(), $_POST['query']);

    if (empty($search)) {
        $query = "SELECT player.*, account.* 
                  FROM player 
                  INNER JOIN account ON player.account_id = account.id
                  ORDER BY account.id DESC";
        $result = mysqli_query($CVH->connect_db(), $query);
    } else {

        $columns = array('player.name', 'account.napngoc', 'account.username', 'account.id', 'account.ip_address');
        $search_query = "SELECT player.*, account.* 
                     FROM player 
                     INNER JOIN account ON player.account_id = account.id
                     WHERE ";
        foreach ($columns as $column) {
            $search_query .= $column . " LIKE '%$search%' OR ";
        }
        $search_query = rtrim($search_query, "OR ");
        $search_query .= " ORDER BY account.id DESC";

        $result = mysqli_query($CVH->connect_db(), $search_query);
    }
    if (mysqli_num_rows($result) > 0) {
        while ($row = mysqli_fetch_array($result)) {
            $output .= '<tr class="search-items">
                            <td>
                                <div class="d-flex align-items-center">
                                    <img src="/Assets/images/avatar/' . $row["head"] . '.png"
                                        width="30" alt="" />
                                    <div class="ms-3">
                                        <div class="user-meta-info">
                                            <h6 class="user-name mb-0">Tên:
                                                ' . $row["name"] . '
                                            </h6>
                                            <span class="user-work fs-3">TK:
                                                ' . $row["username"] . '
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="usr-email-addr">
                                    ' . number_format($row["napngoc"]) . ' ngọc
                                </span>
                            </td>
                            <td>
                                <span class="usr-location">
                                    ' . getBand($row["ban"]) . '
                                </span>
                            </td>
                            <td>
                                <span class="usr-ph-no">
                                    ' . getActive($row["active"]) . '
                                </span>
                            </td>
                            <td>
                                <span class="usr-ph-no">
                                    ' . $row["ip_address"] . '
                                </span>
                            </td>
                            <td>
                                <span class="usr-ph-no">
                                    ' . $row["create_time"] . '
                                </span>
                            </td>
                            <td>
                                <div class="action-btn">
                                    <a href="javascript:void(0)" class="text-info edit">
                                        <i class="ti ti-eye fs-5"></i>
                                    </a>
                                    <a href="javascript:void(0)" onclick="del_(' . $row['account_id'] . ');" class="text-dark delete ms-2">
                                        <i class="ti ti-trash fs-5"></i>
                                    </a>
                                </div>
                            </td>
                        </tr>';
        }
    } else {
        $output .= '<tr class="text-center">
        <td colspan="7">
            <img src="https://cdn-icons-png.flaticon.com/128/7466/7466139.png"
                width="50" class="img-fluid">
            <p class="pt-3"><b>Không có dữ liệu</b></p>
        </td>
    </tr>';
    }
    echo $output;
}
?>