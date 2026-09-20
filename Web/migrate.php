<?php
if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

$_SERVER['DOCUMENT_ROOT'] = __DIR__;
require_once __DIR__ . '/Controller/Setting.php';
require_once __DIR__ . '/Controller/Database.php';
require_once __DIR__ . '/Controller/Core.php';

$system = new System();
$system->ensure_schema_compatibility();
fwrite(STDOUT, "Database migration completed.\n");
