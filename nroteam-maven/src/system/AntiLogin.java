package system;

import utils.Util;

public class AntiLogin {

    // Số lần sai mật khẩu tối đa
    private static final byte MAX_WRONG = 5;
    private static final int TIME_ANTI = 60000;   
    private static final long EXPIRE_IDLE_MS = 30 * 60_000L; // 30 phút không dùng thì xóa
    private long lastTimeLogin = -1;  
    private int timeCanLogin;
    public byte wrongLogin;    
    private long lastSeenMs = System.currentTimeMillis();

    public boolean canLogin() {        
        lastSeenMs = System.currentTimeMillis();
        if (lastTimeLogin != -1) {
            if (Util.canDoWithTime(lastTimeLogin, timeCanLogin)) {
                this.reset();  
                return true; 
            }
        }        
        return wrongLogin < MAX_WRONG;
    }
  
    public void wrong() {
        wrongLogin++;
      
        if (wrongLogin >= MAX_WRONG) {
            this.lastTimeLogin = System.currentTimeMillis();  
            this.timeCanLogin = TIME_ANTI;  
        }
    }

    /**
     * Reset lại số lần đăng nhập sai và các thông số liên quan
     */
    public void reset() {
        this.wrongLogin = 0; 
        this.lastTimeLogin = -1; 
        this.timeCanLogin = 0;  
    }

    
   public String getNotifyCannotLogin() {
    if (lastTimeLogin != -1) {
        long timeRemaining = (lastTimeLogin + timeCanLogin - System.currentTimeMillis()) / 1000;  // Số giây còn lại
        if (timeRemaining > 0) {
            return "Bạn đã đăng nhập tài khoản sai quá nhiều lần. Vui lòng thử lại sau " + timeRemaining + " giây.";
        }
    }
    return "Hãy thử đăng nhập lại"; 
}

    /** True nếu entry này không còn cần thiết (không bị ban + idle lâu). */
    public boolean isExpired() {
        // Còn đang trong thời gian cấm → chưa expire
        if (lastTimeLogin != -1 && !Util.canDoWithTime(lastTimeLogin, timeCanLogin)) {
            return false;
        }
        return (System.currentTimeMillis() - lastSeenMs) > EXPIRE_IDLE_MS;
    }

}

