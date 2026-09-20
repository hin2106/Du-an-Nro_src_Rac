<?php

$root = $_SERVER['DOCUMENT_ROOT'];
date_default_timezone_set('Asia/Ho_Chi_Minh');

class System
{
    private $conn = null;
    private $schemaChecked = false;

    /***  Hàm gọi tự động các hàm khác  ***/
    public function __construct()
    {
        $this->connect_db();
    }

    /***   Kết Nối Database   ***/
    public function connect_db()
    {
        global $DB;
        // nếu đã kết nối thì trả lại
        if ($this->conn && $this->conn->ping()) {
            return $this->conn;
        }

        $conn = mysqli_connect(
            $DB['SERVER'],
            $DB['USERNAME'],
            $DB['PASSWORD'],
            $DB['TABLE']
        );

        if (!$conn) {
            die("Không Thể Kết Nối Tới Cơ Sở Dữ Liệu! " . mysqli_connect_error());
        }

        $conn->set_charset("utf8");
        $this->conn = $conn;
        return $this->conn;
    }

    private function table_exists($table)
    {
        $tableEsc = $this->conn->real_escape_string($table);
        $result = $this->query("SHOW TABLES LIKE '" . $tableEsc . "'");
        if ($result === false) {
            return false;
        }
        $exists = $result->num_rows > 0;
        $result->free();
        return $exists;
    }

    private function column_exists($table, $column)
    {
        $tableEsc = $this->conn->real_escape_string($table);
        $columnEsc = $this->conn->real_escape_string($column);
        $sql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" . $tableEsc . "' AND COLUMN_NAME = '" . $columnEsc . "'";
        $result = $this->query($sql);
        if ($result === false) {
            return false;
        }
        $row = $result->fetch_row();
        $result->free();
        return isset($row[0]) && (int)$row[0] > 0;
    }

    public function ensure_schema_compatibility()
    {
        if ($this->schemaChecked) {
            return;
        }

        $this->connect_db();

        // Bảng phục vụ luồng nạp PayOS.
        $this->query("CREATE TABLE IF NOT EXISTS `payments` (
            `id` int(11) NOT NULL AUTO_INCREMENT,
            `orderCode` bigint(20) NOT NULL,
            `username` varchar(50) NOT NULL,
            `amount` int(11) NOT NULL DEFAULT 0,
            `status` enum('pending','paid','failed','cancelled') NOT NULL DEFAULT 'pending',
            `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
            `paid_at` timestamp NULL DEFAULT NULL,
            PRIMARY KEY (`id`),
            UNIQUE KEY `uniq_orderCode` (`orderCode`),
            KEY `idx_username` (`username`),
            KEY `idx_status` (`status`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        // Lịch sử nạp ngọc (mội giao dịch PayOS thành công tạo 1 bản ghi).
        $this->query("CREATE TABLE IF NOT EXISTS `lichsu_napngoc` (
            `id` int NOT NULL AUTO_INCREMENT,
            `username` varchar(255) NOT NULL,
            `cash_spent` int DEFAULT 0,
            `ngoc_received` int DEFAULT 0,
            `created_at` datetime DEFAULT NULL,
            `status` varchar(20) NOT NULL DEFAULT 'processing',
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        // Hàng đợi cấp phát ngọc cho game server xử lý.
        $this->query("CREATE TABLE IF NOT EXISTS `pending_rewards` (
            `id` int NOT NULL AUTO_INCREMENT,
            `account_id` int NOT NULL,
            `item_id` int NOT NULL DEFAULT 0,
            `quantity` int NOT NULL,
            `processed` tinyint(1) DEFAULT 0,
            `history_id` int DEFAULT NULL,
            `created_at` timestamp DEFAULT current_timestamp(),
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        // Heartbeat game server để webhook xác định server có đang chạy hay không.
        $this->query("CREATE TABLE IF NOT EXISTS `game_server_status` (
            `service_name` varchar(64) NOT NULL,
            `last_heartbeat` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
            PRIMARY KEY (`service_name`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        // Bảng phục vụ quên mật khẩu.
        $this->query("CREATE TABLE IF NOT EXISTS `password_reset` (
            `id` int(11) NOT NULL AUTO_INCREMENT,
            `email` varchar(255) NOT NULL,
            `token` varchar(64) NOT NULL,
            `used` tinyint(1) NOT NULL DEFAULT 0,
            `created_at` datetime NOT NULL DEFAULT current_timestamp(),
            PRIMARY KEY (`id`),
            KEY `idx_email` (`email`),
            KEY `idx_token` (`token`),
            KEY `idx_created_at` (`created_at`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        // Nhiều source cũ dùng account.tongnap thay vì account.danap.
        if ($this->table_exists('account') && !$this->column_exists('account', 'tongnap')) {
            $this->query("ALTER TABLE `account` ADD COLUMN `tongnap` int(11) NOT NULL DEFAULT 0");
        }

        if ($this->table_exists('account') && !$this->column_exists('account', 'point_post')) {
            $this->query("ALTER TABLE `account` ADD COLUMN `point_post` int(11) NOT NULL DEFAULT 0");
        }

        // Cột pass2 cho admin login (nếu không có, lấy từ password)
        if ($this->table_exists('account') && !$this->column_exists('account', 'pass2')) {
            $this->query("ALTER TABLE `account` ADD COLUMN `pass2` varchar(100) DEFAULT NULL");
            // Copy password sang pass2 cho các admin đã có
            $this->query("UPDATE `account` SET `pass2` = `password` WHERE `is_admin` = 1 AND `pass2` IS NULL");
        }

        if ($this->table_exists('account') && $this->column_exists('account', 'tongnap') && $this->column_exists('account', 'danap')) {
            $this->query("UPDATE `account` SET `tongnap` = `danap` WHERE `tongnap` = 0 AND `danap` > 0");
        }

        $this->schemaChecked = true;
    }

    /*** TRUY VẤN SQL - wrapper an toàn ***/
    public function query($sql)
    {
        $this->connect_db();
        $result = $this->conn->query($sql);
        if ($result === false) {
            // Ghi log để debug (local dev có thể hiển thị)
            error_log("MySQL Error: " . $this->conn->error . " -- SQL: " . $sql);
            return false;
        }
        return $result;
    }

    public function cong($table, $data, $sotien, $where)
    {
        $sql = "UPDATE `$table` SET `$data` = `$data` + '$sotien' WHERE $where ";
        return $this->query($sql);
    }
    public function tru($table, $data, $sotien, $where)
    {
        $sql = "UPDATE `$table` SET `$data` = `$data` - '$sotien' WHERE $where ";
        return $this->query($sql);
    }
    public function insert($table, $data)
    {
        $this->connect_db();
        $field_list = '';
        $value_list = '';
        foreach ($data as $key => $value) {
            $field_list .= ",$key";
            $value_list .= ",'" . $this->conn->real_escape_string($value) . "'";
        }
        $sql = 'INSERT INTO ' . $table . '(' . trim($field_list, ',') . ') VALUES (' . trim($value_list, ',') . ')';

        return $this->query($sql);
    }
    public function update($table, $data, $where)
    {
        $this->connect_db();
        $sql = '';
        foreach ($data as $key => $value) {
            $sql .= "$key = '" . $this->conn->real_escape_string($value) . "',";
        }
        $sql = 'UPDATE ' . $table . ' SET ' . trim($sql, ',') . ' WHERE ' . $where;
        return $this->query($sql);
		
    }
    public function update_value($table, $data, $where, $value1)
    {
        $this->connect_db();
        $sql = '';
        foreach ($data as $key => $value) {
            $sql .= "$key = '" . $this->conn->real_escape_string($value) . "',";
        }
        $sql = 'UPDATE ' . $table . ' SET ' . trim($sql, ',') . ' WHERE ' . $where . ' LIMIT ' . $value1;
        return $this->query($sql);
    }
    public function remove($table, $where)
    {
        $sql = "DELETE FROM $table WHERE $where";
        return $this->query($sql);
    }
    public function get_list($sql)
    {
        $result = $this->query($sql);
        if ($result === false) {
            // trả về mảng rỗng khi query lỗi (an toàn)
            return array();
        }
        $return = array();
        if ($result->num_rows > 0) {
            while ($row = $result->fetch_assoc()) {
                $return[] = $row;
            }
        }
        $result->free();
        return $return;
    }
    public function get_row($sql)
    {
        $result = $this->query($sql);
        if ($result === false) {
            return false;
        }
        $row = $result->fetch_assoc();
        $result->free();
        if ($row) {
            return $row;
        }
        return false;
    }
    public function get_value($sql)
    {
        $result = $this->query($sql);
        if ($result === false) return null;
        if ($result && $result->num_rows > 0) {
            $row = $result->fetch_row();
            $result->free();
            return $row[0];
        }
        if ($result) $result->free();
        return null;
    }

    public function num_rows($sql)
    {
        $result = $this->query($sql);
        if ($result === false) {
            return false;
        }
        $row = $result->num_rows;
        $result->free();
        if ($row) {
            return $row;
        }
        return false;
    }

    // Check tài khoản đã tồn tại hay chưa
    public function check_account_exist($username)
    {
        $this->connect_db();
        $usernameEsc = $this->conn->real_escape_string($username);
        $sql = "SELECT * FROM account WHERE username = '$usernameEsc' LIMIT 1";
        $result = $this->query($sql);
        if ($result === false) return false;
        $exists = ($result->num_rows > 0);
        $result->free();
        return $exists;
    }


    /***   Anti SQL Injection - Chỉ nhận dạng Số   ***/
    public function anti_sql($number)
    {
        $id = isset($number) ? preg_replace("/[^0-9]/", "", (string)$number) : false;
        return $id;
    }

    public function count($table, $where = null) {
        $sql = "SELECT COUNT(*) as total FROM `$table`";
        if ($where) {
            $sql .= " WHERE $where";
        }
        $result = $this->query($sql);
        if ($result === false) return 0;
        $row = $result->fetch_assoc();
        $result->free();
        if ($row) {
            return $row["total"];
        } else {
            return 0;
        }
    }

    public function tongdoanhthu() {
        // Tổng VND từ giao dịch bank (PayOS) đã thanh toán
        $result = $this->query("SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE status = 'paid'");
        $bank = 0;
        if ($result) {
            $row = $result->fetch_assoc();
            $result->free();
            $bank = (int)$row['total'];
        }
        return $bank;
    }

    public function TKhomnay() {
        $todayDate = date("Y-m-d");
        $startTime = $todayDate . " 00:00:00";
        $endTime = $todayDate . " 23:59:59";
    
        $sql = "SELECT COUNT(*) AS total FROM account WHERE create_time BETWEEN '$startTime' AND '$endTime'";
        $result = $this->query($sql);
        if ($result === false) return 0;
        $row = $result->fetch_assoc();
        $result->free();
        if ($row) return $row['total'];
        return 0;
    }


    public function DThomnay() {
        $todayStart = date('Y-m-d') . ' 00:00:00';
        $todayEnd   = date('Y-m-d') . ' 23:59:59';
        $result = $this->query("SELECT COALESCE(SUM(amount),0) AS total FROM payments WHERE status = 'paid' AND paid_at BETWEEN '$todayStart' AND '$todayEnd'");
        if ($result) {
            $row = $result->fetch_assoc();
            $result->free();
            return (int)$row['total'];
        }
        return 0;
    }
    
    
    public function setting($id)
    {
        $result = $this->query("SELECT * FROM `cvh_setting` WHERE `id`='" . $this->conn->real_escape_string($id) . "'");
        if ($result === false) return null;
        $row = $result->fetch_array(MYSQLI_ASSOC);
        $result->free();
        return $row;
    }


    public function player($account_id)
    {
        $result = $this->query("SELECT * FROM `player` WHERE `account_id`='" . $this->conn->real_escape_string($account_id) . "'");
        if ($result === false) return null;
        $row = $result->fetch_array(MYSQLI_ASSOC);
        $result->free();
        if ($row && isset($row['data_inventory'])) {
            $inventory = json_decode($row['data_inventory'], true);
            $row['ingame_gem'] = (is_array($inventory) && isset($inventory[1])) ? (int)$inventory[1] : 0;
        } elseif ($row) {
            $row['ingame_gem'] = 0;
        }
        return $row;
    }

    public function canUserPost($userId)
    {
        $currentDate = date('Y-m-d');

        $conn = $this->connect_db();

        $sql = "SELECT COUNT(*) as post_count FROM cvh_baiviet WHERE poster = ? AND DATE(created) = ?";
        $result = $conn->prepare($sql);
        if (!$result) return false;
        $result->bind_param("is", $userId, $currentDate);
        $result->execute();
        $result->bind_result($postCount);
        $result->fetch();
        $result->close();

        return $postCount < 3;
    }

    public function get_account_by_username($username)
    {
        $usernameEsc = $this->conn->real_escape_string($username);
        $result = $this->query("SELECT * FROM account WHERE username = '$usernameEsc'");
        if ($result === false) return array();
        if ($result->num_rows > 0) {
            $account = $result->fetch_assoc();
        } else {
            $account = array();
        }
        $result->free();
        return $account;
    }


    /***   kiểm tra đăng nhập   ***/
    public function check_user($user, $pass)
    {
        $this->connect_db();
        $user = str_replace('"', "\"", $user);
        $user = str_replace("'", "\'", $user);
        $pass = str_replace('"', "\"", $pass);
        $pass = str_replace("'", "\'", $pass);

        $userEsc = $this->conn->real_escape_string($user);
        $passEsc = $this->conn->real_escape_string($pass);

        $result = $this->query("SELECT * FROM `account` WHERE `username`='" . $userEsc . "' AND `password`='" . $passEsc . "' ");
        if ($result === false) return false;
        $rowcount = $result->num_rows;
        $result->free();
        if ($rowcount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public function LoginAD($user, $pass)
    {
        $this->connect_db();

        // chuẩn hoá input
        $user = trim($user);
        $pass = (string)$pass;

        // Lấy pass2 hoặc password từ DB (hỗ trợ cả schema mới và cũ)
        $stmt = $this->conn->prepare("SELECT password, pass2 FROM account WHERE username = ? AND is_admin = 1 LIMIT 1");
        if (!$stmt) {
            error_log("Prepare failed: " . $this->conn->error);
            return false;
        }
        $stmt->bind_param("s", $user);
        $stmt->execute();
        $res = $stmt->get_result();
        $stmt->close();

        if (!$res || $res->num_rows === 0) {
            return false; // không có user hoặc không phải admin
        }

        $row = $res->fetch_assoc();
        
        // Ưu tiên pass2 nếu có, fallback về password
        $dbPassPlain = !empty($row['pass2']) ? (string)$row['pass2'] : (string)$row['password'];

        // So sánh plain text (password game thường lưu plain)
        return ($pass === $dbPassPlain);
    }
    /***   kiểm tra người dùng đã có trên hệ thống chưa    ***/
    public function check_user_register($user)
    {
        $this->connect_db();
        $user = str_replace('"', "\"", $user);
        $user = str_replace("'", "\'", $user);
        $userEsc = $this->conn->real_escape_string($user);

        $result = $this->query("SELECT * FROM `account` WHERE `username`='" . $userEsc . "'");
        if ($result === false) return false;
        $rowcount = $result->num_rows;
        $result->free();
        if ($rowcount > 0) {
            return true;
        } else {
            return false;
        }
    }
	
	public function check_user_password($user, $pass)
		{
			$this->connect_db();

			// escape username
			$user = str_replace('"', "\"", $user);
			$user = str_replace("'", "\'", $user);
			$userEsc = $this->conn->real_escape_string($user);

			// escape password
			$pass = str_replace('"', "\"", $pass);
			$pass = str_replace("'", "\'", $pass);
			$passEsc = $this->conn->real_escape_string($pass);

			// truy vấn check username + password
			$result = $this->query("SELECT * FROM `account` WHERE `username`='" . $userEsc . "' AND `password`='" . $passEsc . "'");
			if ($result === false) return false;

			$rowcount = $result->num_rows;
			$result->free();

			if ($rowcount > 0) {
				return true; // đúng mật khẩu
			} else {
				return false; // sai mật khẩu
			}
		}

    public function checkPost($id)
    {

        $result = $this->query("SELECT * FROM `cvh_baiviet` WHERE `id`='" . $this->conn->real_escape_string($id) . "'");
        if ($result === false) return false;
        $rowcount = $result->num_rows;
        $result->free();
        if ($rowcount > 0) {
            return true;
        } else {
            return false;
        }
    }

    public function getToken()
    {
        if (isset($_COOKIE['token'])) {
            return $_COOKIE['token'];
        } else {
            return null;
        }
    }

    public function Check($token)
    {

        $result = $this->query("SELECT * FROM `account`");
        if ($result === false) return null;
        while ($row = $result->fetch_assoc()) {
            $tokenget = $token;
            $tokendata = $this->Token($row['username'], $row['password']);

            if ($tokenget == $tokendata) {
                $result->free();
                return $row;
            }
        }
        $result->free();
        return null;
    }

    public function post_card($request_id, $telco, $pin, $serial, $amount, $partner_id, $partner_key)
    {
        $data = array(
            'telco' => $telco,
            'code' => $pin,
            'serial' => $serial,
            'amount' => $amount,
            'request_id' => $request_id,
            'partner_id' => $partner_id,
            'sign' => md5($partner_key . $pin . $serial),
            'command' => 'charging'
        );

        $curl = curl_init();

        curl_setopt_array($curl, [
            CURLOPT_URL => 'https://doithe.vn/chargingws/v2?' . http_build_query($data),
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => '',
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 0,
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => 'GET',
            CURLOPT_HTTPHEADER => [
                'Content-Type: application/json',
            ],
        ]);

        $response = curl_exec($curl);

        curl_close($curl);
        return json_decode($response, true);
    }


    /***  chuyển đổi 0h:00 phút ngày hôm nay sang dạng timestamp    ***/
    public function time_today()
    {
        $date = date("d-m-Y 00:00");
        $timestamp = strtotime($date);
        return $timestamp;
    }

    /***   xác định mốc thời gian cho trước    ***/
    public function time_ago($time)
    {
        $time_difference = time() - $time;

        if ($time_difference < 1) {
            return "vừa xong";
        }
        $condition = [12 * 30 * 24 * 60 * 60 => "năm", 30 * 24 * 60 * 60 => "tháng", 24 * 60 * 60 => "ngày", 60 * 60 => "giờ", 60 => "phút", 1 => "giây",];

        foreach ($condition as $secs => $str) {
            $d = $time_difference / $secs;

            if ($d >= 1) {
                $t = round($d);
                return $t . " " . $str . ($t > 1 ? "" : "") . " trước";
            }
        }
    }

    /***   lấy url request hiện tại    ***/
    public function PageURL()
    {
        $pageURL = "http";
        if (isset($_SERVER["HTTPS"]) && $_SERVER["HTTPS"] == "on") {
            $pageURL .= "s";
        }

        $pageURL .= "://";
        if ($_SERVER["SERVER_PORT"] != "80") {
            $pageURL .= $_SERVER["SERVER_NAME"] . ":" . $_SERVER["SERVER_PORT"] . $_SERVER["REQUEST_URI"];
        } else {
            $pageURL .= $_SERVER["SERVER_NAME"] . $_SERVER["REQUEST_URI"];
        }

        return $pageURL;
    }

    /***   thu gọn chuỗi    ***/
    public function cat_chuoi($string = "", $size = 100, $link = "...")
    {
        $string = strip_tags(trim($string));
        $strlen = strlen($string);
        $str = substr($string, $size, 20);
        $exp = explode(" ", $str);
        $sum = count($exp);
        $yes = "";
        for ($i = 0; $i < $sum; $i++) {
            if ($yes == "") {
                $a = strlen($exp[$i]);
                if ($a == 0) {
                    $yes = "no";
                    $a = 0;
                }
                if ($a >= 1 && $a <= 12) {
                    $yes = "no";
                    $a;
                }
                if ($a > 12) {
                    $yes = "no";
                    $a = 12;
                }
            }
        }
        $sub = substr($string, 0, $size + $a);
        if ($strlen - $size > 0) {
            $sub .= $link;
        }
        return $sub;
    }

    /***   rewrite text sang dạng url    ***/
    public function rewrite($text)
    {
        $text = html_entity_decode(trim($text), ENT_QUOTES, "UTF-8");
        $text = str_replace(" ", "-", $text);
        $text = str_replace("--", "-", $text);
        $text = str_replace("@", "-", $text);
        $text = str_replace("/", "-", $text);
        $text = str_replace("\\", "-", $text);
        $text = str_replace(":", "", $text);
        $text = str_replace("\"", "", $text);
        $text = str_replace("'", "", $text);
        $text = str_replace("<", "", $text);
        $text = str_replace(">", "", $text);
        $text = str_replace(",", "", $text);
        $text = str_replace("?", "", $text);
        $text = str_replace(";", "", $text);
        $text = str_replace(".", "", $text);
        $text = str_replace("[", "", $text);
        $text = str_replace("]", "", $text);
        $text = str_replace("(", "", $text);
        $text = str_replace(")", "", $text);
        $text = str_replace("́", "", $text);
        $text = str_replace("̀", "", $text);
        $text = str_replace("̃", "", $text);
        $text = str_replace("̣", "", $text);
        $text = str_replace("̉", "", $text);
        $text = str_replace("*", "", $text);
        $text = str_replace("!", "", $text);
        $text = str_replace("$", "-", $text);
        $text = str_replace("&", "-and-", $text);
        $text = str_replace("%", "", $text);
        $text = str_replace("#", "", $text);
        $text = str_replace("^", "", $text);
        $text = str_replace("=", "", $text);
        $text = str_replace("+", "", $text);
        $text = str_replace("~", "", $text);
        $text = str_replace("`", "", $text);
        $text = str_replace("--", "-", $text);
        $text = preg_replace("/(à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ)/", "a", $text);
        $text = preg_replace("/(à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ)/", "a", $text);
        $text = preg_replace("/(è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ)/", "e", $text);
        $text = preg_replace("/(è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ)/", "e", $text);
        $text = preg_replace("/(ì|í|ị|ỉ|ĩ)/", "i", $text);
        $text = preg_replace("/(ì|í|ị|ỉ|ĩ)/", "i", $text);
        $text = preg_replace("/(ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ)/", "o", $text);
        $text = preg_replace("/(ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ)/", "o", $text);
        $text = preg_replace("/(ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ)/", "u", $text);
        $text = preg_replace("/(ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ)/", "u", $text);
        $text = preg_replace("/(ỳ|ý|ỵ|ỷ|ỹ)/", "y", $text);
        $text = preg_replace("/(đ)/", "d", $text);
        $text = preg_replace("/(ỳ|ý|ỵ|ỷ|ỹ)/", "y", $text);
        $text = preg_replace("/(đ)/", "d", $text);
        $text = preg_replace("/(À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ)/", "A", $text);
        $text = preg_replace("/(À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ)/", "A", $text);
        $text = preg_replace("/(È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ)/", "E", $text);
        $text = preg_replace("/(È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ)/", "E", $text);
        $text = preg_replace("/(Ì|Í|Ị|Ỉ|Ĩ)/", "I", $text);
        $text = preg_replace("/(Ì|Í|Ị|Ỉ|Ĩ)/", "I", $text);
        $text = preg_replace("/(Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ)/", "O", $text);
        $text = preg_replace("/(Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ)/", "O", $text);
        $text = preg_replace("/(Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ)/", "U", $text);
        $text = preg_replace("/(Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ)/", "U", $text);
        $text = preg_replace("/(Ỳ|Ý|Ỵ|Ỷ|Ỹ)/", "Y", $text);
        $text = preg_replace("/(Đ)/", "D", $text);
        $text = preg_replace("/(Ỳ|Ý|Ỵ|Ỷ|Ỹ)/", "Y", $text);
        $text = preg_replace("/(Đ)/", "D", $text);
        $text = strtolower($text);
        return $text;
    }

    /***   Chuyển chuỗi sang văn bản không có các kí tự    ***/
    public function antil_text($text)
    {
        $text = html_entity_decode(trim($text), ENT_QUOTES, "UTF-8");
        $text = str_replace(":", "", $text);
        $text = str_replace("\"", "", $text);
        $text = str_replace("'", "", $text);
        $text = str_replace("<", "", $text);
        $text = str_replace(">", "", $text);
        $text = str_replace(",", "", $text);
        $text = str_replace("?", "", $text);
        $text = str_replace(";", "", $text);
        $text = str_replace(".", "", $text);
        $text = str_replace("[", "", $text);
        $text = str_replace("]", "", $text);
        $text = str_replace("(", "", $text);
        $text = str_replace(")", "", $text);
        $text = str_replace("́", "", $text);
        $text = str_replace("̀", "", $text);
        $text = str_replace("̃", "", $text);
        $text = str_replace("̣", "", $text);
        $text = str_replace("̉", "", $text);
        $text = str_replace("*", "", $text);
        $text = str_replace("!", "", $text);
        $text = str_replace("%", "", $text);
        $text = str_replace("#", "", $text);
        $text = str_replace("^", "", $text);
        $text = str_replace("=", "", $text);
        $text = str_replace("+", "", $text);
        $text = str_replace("~", "", $text);
        $text = str_replace("`", "", $text);
        $text = strtolower($text);
        return $text;
    }
    /***   kiểm ra chuỗi con có trong chuỗi mẹ hay không    ***/
    public function tim_chuoi($str, $chuoi)
    {
        if (strpos($str, $chuoi) !== false) {
            return true;
        } else {
            return false;
        }
    }

    public function dectect_tiengviet($string)
    {
        $tiengviet = ["à", "á", "ạ", "ả", "ã", "â", "ầ", "ấ", "ậ", "ẩ", "ẫ", "ă", "ằ", "ắ", "ặ", "ẳ", "ẵ", "À", "Á", "Ạ", "Ả", "Ã", "Â", "Ầ", "Ấ", "Ậ", "Ẩ", "Ẫ", "Ă", "Ằ", "Ắ", "Ặ", "Ẳ", "Ẵ", "è", "é", "ẹ", "ẻ", "ẽ", "ê", "ề", "ế", "ệ", "ể", "ễ", "È", "É", "Ẹ", "Ẻ", "Ẽ", "Ê", "Ề", "Ế", "Ệ", "Ể", "Ễ", "đ", "Đ", "ò", "ó", "ọ", "ỏ", "õ", "ô", "ồ", "ố", "ộ", "ổ", "ỗ", "ơ", "ờ", "ớ", "ợ", "ở", "ỡ", "Ò", "Ó", "Ọ", "Ỏ", "Õ", "Ô", "Ồ", "Ố", "Ộ", "Ổ", "Ỗ", "Ơ", "Ờ", "Ớ", "Ợ", "Ở", "Ỡ", "ì", "í", "ị", "ỉ", "ĩ", "Ì", "Í", "Ị", "Ỉ", "Ĩ", "ù", "ú", "ụ", "ủ", "ũ", "ư", "ừ", "ứ", "ự", "ử", "ữ", "Ù", "Ú", "Ụ", "Ủ", "Ũ", "Ư", "Ừ", "Ứ", "Ự", "Ử", "Ữ", "ỳ", "ý", "ỵ", "ỷ", "ỹ", "Ỳ", "Ý", "Ỵ", "Ỷ", "Ỹ",];

        foreach ($tiengviet as $key) {
            if ($this->tim_chuoi($string, $key) == true) {
                return true;
            } else {
                return false;
            }
        }
    }

    public function compact_string($string, $length = 5, $replace)
    {
        $compact = substr($string, 0, $length);
        $compact = $compact . $replace;
        return $compact;
    }

    /***  kiểm tra chuỗi có phải dạng số hay không    ***/
    public function check_int($data)
    {
        if (is_int($data) === true) {
            return true;
        }
        if (is_string($data) === true && is_numeric($data) === true) {
            return strpos($data, ".") === false;
        }
    }

    /***   xác định mốc thời gian từ dạng ngày tháng    ***/
    function timeAgo($timestamp)
    {
        $currentTime = time();
        $timeDifference = $currentTime - $timestamp;

        $seconds = $timeDifference;
        $minutes = round($seconds / 60);
        $hours = round($seconds / 3600);
        $days = round($seconds / 86400);

        if ($seconds <= 60) {
            return "Vừa xong";
        } else if ($minutes <= 60) {
            return "$minutes phút trước";
        } else if ($hours <= 24) {
            return "$hours giờ trước";
        } else if ($days <= 7) {
            return "$days ngày trước";
        } else {
            return date("d-m-Y", $timestamp);
        }
    }


    /***   hàm Craw hoặc Post dữ liệu sử dụng CUrl   ***/
    public function curl($url, $data)
    {
        $ch = @curl_init();
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_USERAGENT, "");
        curl_setopt($ch, CURLOPT_ENCODING, "");
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_TIMEOUT, 60);
        curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 60);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
        if ($data) {
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        }
        $page = curl_exec($ch);
        curl_close($ch);
        return $page;
    }

    /***   kiểm tra thiết bị đang request có phải điện thoại hay không - copy từ mã nguồn wordpress   ***/
    public function is_mobile()
    {
        if (empty($_SERVER["HTTP_USER_AGENT"])) {
            $is_mobile = false;
        } elseif (
            strpos($_SERVER["HTTP_USER_AGENT"], "Mobile") !== false || // many mobile devices (all iPhone, iPad, etc.)
            strpos($_SERVER["HTTP_AGENT"], "Android") !== false || strpos($_SERVER["HTTP_USER_AGENT"], "Silk/") !== false || strpos($_SERVER["HTTP_USER_AGENT"], "Kindle") !== false || strpos($_SERVER["HTTP_USER_AGENT"], "BlackBerry") !== false || strpos($_SERVER["HTTP_USER_AGENT"], "Opera Mini") !== false || strpos($_SERVER["HTTP_USER_AGENT"], "Opera Mobi") !== false
        ) {
            $is_mobile = true;
        } else {
            $is_mobile = false;
        }

        return $is_mobile;
    }

    /***   rút gọn chuỗi    ***/
    public function cut_str($str, $max)
    {
        $str = trim($str);
        if (strlen($str) > $max) {
            $s_pos = strpos($str, " ");
            $cut = $s_pos === false || $s_pos > $max;
            $str = wordwrap($str, $max, ";;", $cut);
            $str = explode(";;", $str);
            $str = $str[0] . "...";
        }
        return $str;
    }

    /***   tạo ra chuỗi ngẫu nhiên gồm cả số và chữ (tạo token)    ***/
    public function generateToken()
    {
        $token = bin2hex(openssl_random_pseudo_bytes(64));
        return $token;
    }


    public function Upanh($image_path, $file_type)
    {
        $client_id = 'c957f559086c4ea';
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, 'https://api.imgur.com/3/image.json');
        curl_setopt($ch, CURLOPT_POST, TRUE);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        curl_setopt($ch, CURLOPT_HTTPHEADER, array('Authorization: Client-ID ' . $client_id));
        curl_setopt(
            $ch,
            CURLOPT_POSTFIELDS,
            array(
                'image' => base64_encode(file_get_contents($image_path)),
                'type' => $file_type
            )
        );
        $response = curl_exec($ch);
        curl_close($ch);
        $response = json_decode($response, true);
        return $response['data']['link'];
    }

    public function FormatString($data)
    {
        // Fix &entity\n;
        $data = str_replace(array('&amp;', '&lt;', '&gt;'), array('&amp;amp;', '&amp;lt;', '&amp;gt;'), $data);
        $data = preg_replace('/(&#*\w+)[\x00-\x20]+;/u', '$1;', $data);
        $data = preg_replace('/(&#x*[0-9A-F]+);*/iu', '$1;', $data);
        $data = html_entity_decode($data, ENT_COMPAT, 'UTF-8');

        // Remove any attribute starting with "on" or xmlns
        $data = preg_replace('#(<[^>]+?[\x00-\x20"\'])(?:on|xmlns)[^>]*+>#iu', '$1>', $data);

        // Remove javascript: and vbscript: protocols
        $data = preg_replace('#([a-z]*)[\x00-\x20]*=[\x00-\x20]*([`\'"]*)[\x00-\x20]*j[\x00-\x20]*a[\x00-\x20]*v[\x00-\x20]*a[\x00-\x20]*s[\x00-\x20]*c[\x00-\x20]*r[\x00-\x20]*i[\x00-\x20]*p[\x00-\x20]*t[\x00-\x20]*:#iu', '$1=$2nojavascript...', $data);
        $data = preg_replace('#([a-z]*)[\x00-\x20]*=([\'"]*)[\x00-\x20]*v[\x00-\x20]*b[\x00-\x20]*s[\x00-\x20]*c[\x00-\x20]*r[\x00-\x20]*i[\x00-\x20]*p[\x00-\x20]*t[\x00-\x20]*:#iu', '$1=$2novbscript...', $data);
        $data = preg_replace('#([a-z]*)[\x00-\x20]*=([\'"]*)[\x00-\x20]*-moz-binding[\x00-\x20]*:#u', '$1=$2nomozbinding...', $data);

        // Only works in IE: <span style="width: expression(alert('Ping!'));"></span>
        $data = preg_replace('#(<[^>]+?)style[\x00-\x20]*=[\x00-\x20]*[`\'"]*.*?expression[\x00-\x20]*\([^>]*+>#i', '$1>', $data);
        $data = preg_replace('#(<[^>]+?)style[\x00-\x20]*=[\x00-\x20]*[`\'"]*.*?behaviour[\x00-\x20]*\([^>]*+>#i', '$1>', $data);
        $data = preg_replace('#(<[^>]+?)style[\x00-\x20]*=[\x00-\x20]*[`\'"]*.*?s[\x00-\x20]*c[\x00-\x20]*r[\x00-\x20]*i[\x00-\x20]*p[\x00-\x20]*t[\x00-\x20]*:*[^>]*+>#iu', '$1>', $data);

        // Remove namespaced elements (we do not need them)
        $data = preg_replace('#</*\w+:\w[^>]*+>#i', '', $data);

        do {
            // Remove really unwanted tags
            $old_data = $data;
            $data = preg_replace('#</*(?:applet|b(?:ase|gsound|link)|embed|frame(?:set)?|i(?:frame|layer)|l(?:ayer|ink)|meta|object|s(?:cript|tyle)|title|xml)[^>]*+>#i', '', $data);
        }
        while ($old_data !== $data);

        // we are done...
        return $data;
    }

    public function Ex($status = false, $message = '', $data = [])
    {
        echo json_encode([
            'status' => $status,
            'message' => $message,
            'data' => $data,
        ]);
    }

    public function LimitString($string, $min, $max)
    {
        if (strlen($string) > $max || strlen($string) < $min) {
            return false;
        } else {
            return true;
        }
    }
    public function Token($username, $password)
    {
        $salt = 'CVH36VN';
        $hash = md5($username . $salt . $password . $salt . 'caovanhuydeptraivaicalonmadeoaibietditconmethangnaokhongbietlanobingunobimunhungmacongnhancaovanhuydeptraivclramoinguoia');
        return $hash;
    }

    public function addCMT($post_id, $noidung, $player)
    {
        $sql = "SELECT comments FROM cvh_baiviet WHERE id = " . intval($post_id);
        $result = $this->query($sql);
    
        if ($result && $result !== false && $result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $comments = json_decode($row["comments"], true);
            if (json_last_error() === JSON_ERROR_NONE && is_array($comments)) {
                $newComment = ["account_id" => $player, "noidung" => $noidung, "time" => time()];
                array_unshift($comments, $newComment);
                $jsonComments = json_encode($comments, JSON_UNESCAPED_UNICODE);
                $updateSql = "UPDATE cvh_baiviet SET comments = '" . $this->conn->real_escape_string($jsonComments) . "' WHERE id = " . intval($post_id);
                $cvhvn = $this->query($updateSql);
                if ($result) $result->free();
                return $cvhvn === true;
            }
        }
        if ($result) $result->free();
        return false;
    }

    public function addLike($id_post, $username)
    {
        $sql = "SELECT likes FROM cvh_baiviet WHERE id = " . intval($id_post);
        $result = $this->query($sql);
        if ($result === false) return false;
        $row = $result->fetch_assoc();
        $likes = json_decode($row["likes"], true);
        if (!is_array($likes)) $likes = [];

        if (!in_array($username, $likes)) {
            $likes[] = $username;
        }

        $jsonLikes = json_encode($likes);
        $updateSql = "UPDATE cvh_baiviet SET likes = '" . $this->conn->real_escape_string($jsonLikes) . "' WHERE id = " . intval($id_post);

        $res = $this->query($updateSql);
        $result->free();
        return $res === TRUE || $res === true;
    }


    public function addNhanqua($username)
    {
        $sql = "SELECT user_nhanqua FROM cvh_setting";
        $result = $this->query($sql);
        if ($result === false) return false;
        $row = $result->fetch_assoc();
        $likes = json_decode($row["user_nhanqua"], true);
        if (!is_array($likes)) $likes = [];

        if (!in_array($username, $likes)) {
            $likes[] = $username;
        }
        $jsonLikes = json_encode($likes);
        $updateSql = "UPDATE cvh_setting SET user_nhanqua = '" . $this->conn->real_escape_string($jsonLikes) . "'";

        $res = $this->query($updateSql);
        $result->free();
        return $res === TRUE || $res === true;
    }

    public function checkNhanqua($username)
    {

        $sql = "SELECT user_nhanqua FROM cvh_setting";
        $result = $this->query($sql);
        if ($result === false) return false;
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $likes = json_decode($row['user_nhanqua'], true);
            $result->free();
            if ($likes && in_array($username, $likes)) {
                return true;
            } else {
                return false;
            }
        } else {
            $result->free();
            return false;
        }
    }

    public function unLike($postId, $username)
    {
        $sql = "SELECT likes FROM cvh_baiviet WHERE id = " . intval($postId);
        $result = $this->query($sql);
        if ($result === false) return false;

        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $likes = json_decode($row['likes'], true);
            if (!is_array($likes)) $likes = [];

            if (($key = array_search($username, $likes)) !== false) {
                unset($likes[$key]);
                $likesJson = json_encode(array_values($likes));
                $updateSql = "UPDATE cvh_baiviet SET likes = '" . $this->conn->real_escape_string($likesJson) . "' WHERE id = " . intval($postId);
                $res = $this->query($updateSql);
                $result->free();
                return $res === TRUE || $res === true;
            } else {
                $result->free();
                return false;
            }
        } else {
            $result->free();
            return false;
        }
    }

    public function checkUserLiked($postId, $username)
    {

        $sql = "SELECT likes FROM cvh_baiviet WHERE id = " . intval($postId);
        $result = $this->query($sql);
        if ($result === false) return false;
        if ($result->num_rows > 0) {
            $row = $result->fetch_assoc();
            $likes = json_decode($row['likes'], true);
            $result->free();
            if ($likes && in_array($username, $likes)) {
                return true;
            } else {
                return false;
            }
        } else {
            $result->free();
            return false;
        }
    }

    function getTotalComments($postId)
    {

        $sql = "SELECT comments FROM cvh_baiviet WHERE id = " . intval($postId);
        $result = $this->query($sql);
        if ($result === false) return 0;
        $row = $result->fetch_assoc();
        $comments = json_decode($row["comments"], true);
        $result->free();
        $totalComments = is_array($comments) ? count($comments) : 0;

        return $totalComments;
    }

    public function getTotalLikes($id_post)
    {
        $sql = "SELECT likes FROM cvh_baiviet WHERE id = " . intval($id_post);
        $result = $this->query($sql);
        if ($result === false) return 0;
        $row = $result->fetch_assoc();
        $result->free();

        $likes = json_decode($row["likes"], true);
        $total = is_array($likes) ? count($likes) : 0;
        return $total;
    }

    public function displayComments($postId)
    {

        $sql = "SELECT comments FROM cvh_baiviet WHERE id = " . intval($postId);
        $result = $this->query($sql);
        if ($result === false) {
            echo "Không có bình luận.";
            return;
        }
        $row = $result->fetch_assoc();
        $result->free();
        $comments = json_decode($row["comments"], true);

        if (!empty($comments) && is_array($comments)) {
            foreach ($comments as $comment) {
                $accountId = $comment["account_id"];
                $player = $this->player($accountId);
                $content = $comment["noidung"];
                $time = $comment["time"];
                echo '<div class="p-4 rounded-2 bg-light mb-3">';
                echo '    <div class="d-flex">';
                echo '        <div class="d-flex align-items-center justify-content-center me-6">';
                echo '            <img src="/Assets/images/avatar/' . htmlspecialchars($player['head'] ?? 'default') . '.png" width="30" alt="" />';
                echo '        </div>';
                echo '        <div>';
                echo '            <h6 class="mb-1 fs-4 fw-semibold">' . htmlspecialchars($player['name'] ?? 'Người chơi') . '</h6>';
                echo '            <p class="fs-3 mb-0">' . $this->timeAgo($time) . '</p>';
                echo '        </div>';
                echo '    </div>';
                echo '    <p class="my-3">' . htmlspecialchars($content) . '</p>';
                echo '</div>';
            }
        } else {
            echo "Không có bình luận.";
        }
    }

    public function xss($data)
    {
        // Fix &entity\n;
        $data = str_replace(array('&amp;', '&lt;', '&gt;'), array('&amp;amp;', '&amp;lt;', '&amp;gt;'), $data);
        $data = preg_replace('/(&#*\w+)[\x00-\x20]+;/u', '$1;', $data);
        $data = preg_replace('/(&#x*[0-9A-F]+);*/iu', '$1;', $data);
        $data = html_entity_decode($data, ENT_COMPAT, 'UTF-8');

        // Remove any attribute starting with "on" or xmlns
        $data = preg_replace('#(<[^>]+?[\x00-\x20"\'])(?:on|xmlns)[^>]*+>#iu', '$1>', $data);

        // Remove javascript: and vbscript: protocols
        $data = preg_replace('#([a-z]*)[\x00-\x20]*=[\x00-\x20]*([`\'"]*)[\x00-\x20]*j[\x00-\x20]*a[\x00-\x20]*v[\x00-\x20]*a[\x00-\x20]*s[\x00-\x20]*c[\x00-\x20]*r[\x00-\x20]*i[\x00-\x20]*p[\x00-\x20]*t[\x00-\x20]*:#iu', '$1=$2nojavascript...', $data);
        $data = preg_replace('#([a-z]*)[\x00-\x20]*=([\'"]*)[\x00-\x20]*v[\x00-\x20]*b[\x00-\x20]*s[\x00-\x20]*c[\x00-\x20]*r[\x00-\x20]*i[\x00-\x20]*p[\x00-\x20]*t[\x00-\x20]*:#iu', '$1=$2novbscript...', $data);
        $data = preg_replace('#([a-z]*)[\x00-\x20]*=([\'"]*)[\x00-\x20]*-moz-binding[\x00-\x20]*:#u', '$1=$2nomozbinding...', $data);

        // Only works in IE: <span style="width: expression(alert('Ping!'));"></span>
        $data = preg_replace('#(<[^>]+?)style[\x00-\x20]*=[\x00-\x20]*[`\'"]*.*?expression[\x00-\x20]*\([^>]*+>#i', '$1>', $data);
        $data = preg_replace('#(<[^>]+?)style[\x00-\x20]*=[\x00-\x20]*[`\'"]*.*?behaviour[\x00-\x20]*\([^>]*+>#i', '$1>', $data);
        $data = preg_replace('#(<[^>]+?)style[\x00-\x20]*=[\x00-\x20]*[`\'"]*.*?s[\x00-\x20]*c[\x00-\x20]*r[\x00-\x20]*i[\x00-\x20]*p[\x00-\x20]*t[\x00-\x20]*:*[^>]*+>#iu', '$1>', $data);

        // Remove namespaced elements (we do not need them)
        $data = preg_replace('#</*\w+:\w[^>]*+>#i', '', $data);

        do {
            // Remove really unwanted tags
            $old_data = $data;
            $data = preg_replace('#</*(?:applet|b(?:ase|gsound|link)|embed|frame(?:set)?|i(?:frame|layer)|l(?:ayer|ink)|meta|object|s(?:cript|tyle)|title|xml)[^>]*+>#i', '', $data);
        }
        while ($old_data !== $data);

        // we are done...
        $ducthanhit = htmlspecialchars(addslashes(trim($data)));

        return $ducthanhit;
    }

    /***   hàm phân trang khi gọi dữ kiệu config tại index  - Copy từ Hoàng Minh Thuận ***/
    function phantrang($url, $start, $total, $kmess) {
        $out[] = '<nav><ul class="pagination justify-content-center mb-0 mt-4">';
        $neighbors = 2;
        if ($start >= $total) $start = max(0, $total - (($total % $kmess) == 0 ? $kmess : ($total % $kmess)));
        else $start = max(0, (int)$start - ((int)$start % (int)$kmess));
        $base_link = '<li class="page-item"><a class="page-link border-0 rounded-circle text-dark round-32 d-flex align-items-center justify-content-center" href="' . strtr($url, array('%' => '%%')) . 'page=%d' . '"> %s </a></li>';
        $out[] = $start == 0 ? '' : sprintf($base_link, $start / $kmess, '<i class="ti ti-chevron-left"></i>');
        if ($start > $kmess * $neighbors) $out[] = sprintf($base_link, 1, '1');
        if ($start > $kmess * ($neighbors + 1)) $out[] = '<li><a class="page-link border-0 rounded-circle text-dark round-32 mx-1 d-flex align-items-center justify-content-center" href="#">...</a></li>';
        for ($nCont = $neighbors;$nCont >= 1;$nCont--) if ($start >= $kmess * $nCont) {
            $tmpStart = $start - $kmess * $nCont;
            $out[] = sprintf($base_link, $tmpStart / $kmess + 1, $tmpStart / $kmess + 1);
        }
        $out[] = '<li class="page-item active"><a class="page-link border-0 rounded-circle round-32 mx-1 d-flex align-items-center justify-content-center">' . ($start / $kmess + 1) . '</a></li>';
        $tmpMaxPages = (int)(($total - 1) / $kmess) * $kmess;
        for ($nCont = 1;$nCont <= $neighbors;$nCont++) if ($start + $kmess * $nCont <= $tmpMaxPages) {
            $tmpStart = $start + $kmess * $nCont;
            $out[] = sprintf($base_link, $tmpStart / $kmess + 1, $tmpStart / $kmess + 1);
        }
        if ($start + $kmess * ($neighbors + 1) < $tmpMaxPages) $out[] = '<li><a class="page-link border-0 rounded-circle text-dark round-32 mx-1 d-flex align-items-center justify-content-center" href="#">...</a></li>';
        if ($start + $kmess * $neighbors < $tmpMaxPages) $out[] = sprintf($base_link, $tmpMaxPages / $kmess + 1, $tmpMaxPages / $kmess + 1);
        if ($start + $kmess < $total) {
            $display_page = ($start + $kmess) > $total ? $total : ($start / $kmess + 2);
            $out[] = sprintf($base_link, $display_page, '<i class="ti ti-chevron-right"></i>');
        }
        $out[] = '</ul></nav>';
        return implode('', $out);
    
    }
}
?>