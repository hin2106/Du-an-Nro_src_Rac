<?php
define("BLOCK_JOIN", true);
require_once $_SERVER['DOCUMENT_ROOT'] . "/Controller/Autoload.php";

$root = $_SERVER['DOCUMENT_ROOT'];

if (isset($_GET['role']) && $_GET['role'] == 'Admin') {
    if (isset($_SESSION['admin']) && isset($user['is_admin'])) {
        require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/Head.php';
        require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/Navbar.php';     

        if (isset($_GET['role']) && $_GET['role'] == 'Admin' && !isset($_GET['modun']) && !isset($_GET['act'])) {

            require_once $root . '/Page/Admin/Home/index.php';

        } else {

            $role = isset($_GET['role']) ? $_GET['role'] : '';
            $modun = isset($_GET['modun']) ? $_GET['modun'] : '';
            $act = isset($_GET['act']) ? $_GET['act'] : '';

            if ($modun !== '' && $act !== '' && file_exists($root . '/Page/' . $role . '/' . $modun . '/' . $act . '.php')) {
                require $root . '/Page/' . $role . '/' . $modun . '/' . $act . '.php';
            } else {
                echo "<center>404 - Not Found</center>";

            }

        }
        require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Admin/Sidebar.php';
        require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/Footer.php';
        require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/End.php';
    } else {
        header("Location: /admin/login");
    }
} else {


    require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/Head.php';
    require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/Navbar.php';

    if (!isset($_GET['modun']) && !isset($_GET['act'])) {

        require_once $root . '/Page/Client/Home.php';

    } else {

        $role = isset($_GET['role']) ? $_GET['role'] : '';
        $modun = isset($_GET['modun']) ? $_GET['modun'] : '';
        $act = isset($_GET['act']) ? $_GET['act'] : '';

        if ($role !== '' && $modun !== '' && $act !== '' && file_exists($root . '/Page/' . $role . '/' . $modun . '/' . $act . '.php')) {
            require $root . '/Page/' . $role . '/' . $modun . '/' . $act . '.php';
        } else {
            echo "<center>404 - Not Found</center>";
        }

    }

    require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/Footer.php';
    require_once $_SERVER['DOCUMENT_ROOT'] . '/Themes/Client/End.php';


}