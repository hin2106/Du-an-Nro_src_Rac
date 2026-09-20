<?php
putenv('NRO_REQUEST_GUARD_TEST=1');
$_SERVER['REMOTE_ADDR'] = $argv[1] ?? '198.51.100.77';
$_SERVER['REQUEST_URI'] = $argv[2] ?? '/Api/Auth/Login';
$_SERVER['REQUEST_METHOD'] = $argv[3] ?? 'POST';
require dirname(__DIR__) . '/Controller/RequestGuard.php';
echo "ALLOWED\n";
