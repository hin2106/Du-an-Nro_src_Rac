<?php
unset($_COOKIE['token']);
setcookie('token', '', time() - (7 * 24 * 60 * 60), '/');
header("Location: /");
exit;
?>