<?php
/** Lightweight per-IP token bucket executed before PHP sessions and MySQL. */
final class RequestGuard
{
    private const STATE_TTL_SECONDS = 900;

    public static function enforce(): void
    {
        if (PHP_SAPI === 'cli' && getenv('NRO_REQUEST_GUARD_TEST') !== '1') return;
        $ip = $_SERVER['REMOTE_ADDR'] ?? '';
        if (!filter_var($ip, FILTER_VALIDATE_IP)) self::reject(60);

        [$capacity, $refillPerSecond] = self::policy();
        $directory = rtrim(sys_get_temp_dir(), DIRECTORY_SEPARATOR)
                . DIRECTORY_SEPARATOR . 'nro_web_guard';
        if (!is_dir($directory) && !@mkdir($directory, 0700, true) && !is_dir($directory)) return;

        $path = $directory . DIRECTORY_SEPARATOR
                . hash('sha256', $ip . '|' . self::routeClass()) . '.json';
        $handle = @fopen($path, 'c+');
        if ($handle === false) return;

        $allowed = true;
        $retryAfter = 1;
        if (flock($handle, LOCK_EX)) {
            $raw = stream_get_contents($handle);
            $state = json_decode($raw ?: '', true);
            $now = microtime(true);
            $tokens = isset($state['tokens']) ? (float)$state['tokens'] : (float)$capacity;
            $updated = isset($state['updated']) ? (float)$state['updated'] : $now;
            $tokens = min((float)$capacity, $tokens + max(0.0, $now - $updated) * $refillPerSecond);
            if ($tokens < 1.0) {
                $allowed = false;
                $retryAfter = max(1, (int)ceil((1.0 - $tokens) / $refillPerSecond));
            } else {
                $tokens -= 1.0;
            }
            rewind($handle);
            ftruncate($handle, 0);
            fwrite($handle, json_encode(['tokens' => $tokens, 'updated' => $now]));
            fflush($handle);
            flock($handle, LOCK_UN);
        }
        fclose($handle);

        self::maybeCleanup($directory);
        if (!$allowed) self::reject($retryAfter);
    }

    private static function policy(): array
    {
        return match (self::routeClass()) {
            'auth' => [8, 8 / 60],
            'admin-auth' => [5, 5 / 60],
            'write-api' => [20, 20 / 60],
            'webhook' => [120, 120 / 60],
            default => [90, 90 / 60],
        };
    }

    private static function routeClass(): string
    {
        $uri = strtolower(parse_url($_SERVER['REQUEST_URI'] ?? '/', PHP_URL_PATH) ?: '/');
        if ($uri === '/webhook' || str_contains($uri, '/user/webhook')) return 'webhook';
        if (str_contains($uri, '/api/admin/login')) return 'admin-auth';
        if (str_contains($uri, '/api/auth/login') || str_contains($uri, '/api/auth/register')
                || str_contains($uri, 'forgotpassword') || str_contains($uri, 'resetpassword')) return 'auth';
        if (str_starts_with($uri, '/api/') && ($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'GET') return 'write-api';
        return 'page';
    }

    private static function maybeCleanup(string $directory): void
    {
        $marker = $directory . DIRECTORY_SEPARATOR . '.cleanup';
        if (is_file($marker) && time() - (int)@filemtime($marker) < 300) return;
        $lock = @fopen($marker, 'c+');
        if ($lock === false || !flock($lock, LOCK_EX | LOCK_NB)) {
            if (is_resource($lock)) fclose($lock);
            return;
        }
        touch($marker);
        $cutoff = time() - self::STATE_TTL_SECONDS;
        foreach (glob($directory . DIRECTORY_SEPARATOR . '*.json') ?: [] as $file) {
            if (@filemtime($file) < $cutoff) @unlink($file);
        }
        flock($lock, LOCK_UN);
        fclose($lock);
    }

    private static function reject(int $retryAfter): never
    {
        http_response_code(429);
        header('Retry-After: ' . $retryAfter);
        header('Cache-Control: no-store');
        header('Content-Type: application/json; charset=utf-8');
        echo json_encode(['success' => false, 'message' => 'Bạn thao tác quá nhanh, vui lòng thử lại sau.']);
        exit;
    }
}

RequestGuard::enforce();
