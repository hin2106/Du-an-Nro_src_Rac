<?php
// Tắt hiển thị lỗi
ini_set('display_errors', 0);
ini_set('display_startup_errors', 0);

// Custom error handler
set_error_handler(function($errno, $errstr, $errfile, $errline) {
    $error_types = [
        E_ERROR => 'ERROR',
        E_WARNING => 'WARNING',
        E_PARSE => 'PARSE',
        E_NOTICE => 'NOTICE',
        E_CORE_ERROR => 'CORE_ERROR',
        E_CORE_WARNING => 'CORE_WARNING',
        E_COMPILE_ERROR => 'COMPILE_ERROR',
        E_COMPILE_WARNING => 'COMPILE_WARNING',
        E_USER_ERROR => 'USER_ERROR',
        E_USER_WARNING => 'USER_WARNING',
        E_USER_NOTICE => 'USER_NOTICE',
        E_STRICT => 'STRICT',
        E_RECOVERABLE_ERROR => 'RECOVERABLE_ERROR',
        E_DEPRECATED => 'DEPRECATED',
        E_USER_DEPRECATED => 'USER_DEPRECATED'
    ];
    
    $error_type = $error_types[$errno] ?? 'UNKNOWN';
    
    // Format log message
    $log_message = sprintf(
        "[%s] [%s] %s in %s on line %d",
        date('Y-m-d H:i:s'),
        $error_type,
        $errstr,
        $errfile,
        $errline
    );
    
    // Ghi log vào file
    error_log($log_message . PHP_EOL, 3, __DIR__ . FULL_URL('/') . '/log_err/php_errors.log');
    
    // KHÔNG hiển thị gì trên giao diện
    return true; // Ngăn PHP xử lý lỗi mặc định
});

// Xử lý exceptions
set_exception_handler(function($exception) {
    $log_message = sprintf(
        "[%s] [EXCEPTION] %s in %s on line %d" . PHP_EOL . "Stack trace:%s",
        date('Y-m-d H:i:s'),
        $exception->getMessage(),
        $exception->getFile(),
        $exception->getLine(),
        $exception->getTraceAsString()
    );
    
    error_log($log_message . PHP_EOL, 3, __DIR__ . FULL_URL('/') . '/log_err/php_errors.log');
});

// Xử lý fatal errors
register_shutdown_function(function() {
    $error = error_get_last();
    if ($error && in_array($error['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR])) {
        $log_message = sprintf(
            "[%s] [FATAL] %s in %s on line %d",
            date('Y-m-d H:i:s'),
            $error['message'],
            $error['file'],
            $error['line']
        );
        
        error_log($log_message . PHP_EOL, 3, __DIR__ . FULL_URL('/') . '/log_err/php_errors.log');
    }
});
?>