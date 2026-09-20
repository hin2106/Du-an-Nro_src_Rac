package mail;

/**
 * MAIL SYSTEM - BEST PRACTICES & OPTIMIZATION GUIDE
 * 
 * ==============================================================================
 * 1. ARCHITECTURE TIERS
 * ==============================================================================
 * 
 * Tầng 1: PlayerMail (Mail instance - Per player)
 *    └─ Một mail thực tế của một player
 *    └─ Luôn là dữ liệu gửi trực tiếp
 *
 * Tầng 2: MailReward (Reward attachment)
 *    └─ Reward đính kèm với mail
 *    └─ Chỉ lưu reference, không lưu item object
 *
 * Tầng 3: RewardClaimState (Claim tracking)
 *    └─ Theo dõi trạng thái nhận reward
 *    └─ Có thể merge vào MailReward.claimed
 * 
 * ==============================================================================
 * 2. KEY POINTS
 * ==============================================================================
 * 
 * ✅ DO:
 * 
 * 1. Dùng Queue cho broadcast 1M+ players
 *    // Không: INSERT 1M records ngay
 *    // Đúng: queue + batch worker (1000/batch)
 *
 * 2. Lazy load rewards
 *    // List mails: không load rewards
 *    List<PlayerMail> mails = mailDAO.getActive(playerId);
 *    // Detail mail: load rewards
 *    List<MailReward> rewards = rewardDAO.getByMailId(mailId);
 *
 * 3. Validate claim idempotency
 *    // Check: claimed = false TRƯỚC khi claim
 *    if (reward.claimed) throw new Exception("Already claimed");
 *    // Xử lý: claim
 *    addInventory(reward);
 *    // Cập nhật: claimed = true
 *    rewardDAO.markClaimed(rewardId);
 *
 * 4. Index strategy
 *    CREATE INDEX idx_player_status ON player_mail(player_id, status);
 *    CREATE INDEX idx_expired ON player_mail(expired_at);
 *    CREATE INDEX idx_reward_mail ON mail_reward(mail_id, claimed);
 * 
 * 5. Batch operations
 *    // Batch insert 1000 mails một lần
 *    List<PlayerMail> batch = new ArrayList<>();
 *    for (int i = 0; i < 1000; i++) {
 *        batch.add(createMail(...));
 *    }
 *    mailDAO.saveBatch(batch);
 *
 * 6. Limit per player
 *    static final int MAX_MAIL = 100;
 *    if (mailCount > MAX_MAIL) {
 *        deleteOldReadMails(playerId);
 *    }
 *
 * 7. Cache frequently accessed data
 *    // Cache active mails
 *    mailCache.put(playerId, getActiveMails(playerId));
 *    // Invalidate on change
 *    mailCache.remove(playerId);
 *
 * 8. Use transactions
 *    @Transactional
 *    public void claimReward(rewardId) {
 *        addInventory();  // Can fail
 *        markClaimed();   // Will rollback if above fails
 *    }
 * 
 * ❌ DON'T:
 * 
 * 1. Lưu item object trong mail
 *    // WRONG:
 *    class PlayerMail {
 *        Item item;  // Lưu object
 *    }
 *    // RIGHT:
 *    class MailReward {
 *        int rewardType;  // Chỉ lưu reference
 *        long rewardId;
 *        int amount;
 *    }
 *
 * 2. Không load rewards cho list mails
 *    // WRONG:
 *    List<PlayerMail> mails = mailDAO.getActive(playerId);
 *    // N+1 query issue
 *    for (PlayerMail mail : mails) {
 *        List rewards = rewardDAO.getByMailId(mail.id);
 *    }
 *    // RIGHT:
 *    List<PlayerMail> mails = mailDAO.getActive(playerId);
 *    // Chỉ load rewards khi detail
 *    MailDetail detail = mailDAO.getDetail(mailId);
 *
 * 3. Không claim reward nhiều lần
 *    // Nếu không check claimed, có thể claim 2 lần
 *    // Luôn validate: if (reward.claimed) throw;
 *
 * 4. Không insert 1M records ngay lập tức
 *    // WRONG:
 *    for (playerId : 1_000_000_players) {
 *        insertMail(playerId);  // 1M queries!
 *    }
 *    // RIGHT:
 *    createQueue(templateId, playerIds);
 *    worker.processBatch(1000);  // Batch processing
 *
 * 5. Không quên dọn mail hết hạn
 *    // Tạo cron job
 *    @Scheduled(cron = "0 0 2 * * *")
 *    public void cleanup() {
 *        DELETE FROM player_mail WHERE expired_at < NOW();
 *    }
 *
 * 6. Không quên index trên foreign keys
 *    // Queries thường sử dụng:
 *    - player_mail(player_id, status)
 *    - mail_reward(mail_id)
 *    - player_mail(expired_at) <- dọn mail
 *
 * 7. Không hardcode constants
 *    // Use enums:
 *    public enum RewardType {
 *        ITEM(1), GOLD(2), GEM(3)
 *    }
 * 
 * ==============================================================================
 * 3. SCALING CONSIDERATIONS
 * ==============================================================================
 * 
 * Database Partitioning:
 * - Partition player_mail theo player_id ranges
 * - Hash partitioning: player_id % 256
 * - Pro: Distribute load, faster queries
 * - Con: Complex joins
 * 
 * Caching Layer:
 * - Redis cache active mails
 * - TTL = mail.expireTime
 * - Invalidate on: claim, delete, read
 * 
 * Archive Strategy:
 * - Move mails > 180 days to archive table
 * - Monthly archived_player_mail_2024_03
 * - Keep recent in main table
 * 
 * Message Queue:
 * - Use Kafka/RabbitMQ cho broadcast
 * - Mail producer -> Queue -> Mail consumer
 * - Scale workers independently
 * 
 * Async Processing:
 * - Claim reward asynchronously
 * - Worker thread pool
 * - Eventual consistency
 * 
 * ==============================================================================
 * 4. SQL QUERY OPTIMIZATION
 * ==============================================================================
 * 
 * Query 1: Load active mails (Player login)
 * SELECT id, title, content, is_read, expired_at, has_reward
 * FROM player_mail
 * WHERE player_id = ? AND status = 1 AND expired_at > NOW()
 * ORDER BY created_at DESC
 * INDEX: (player_id, status)
 * EXPLAIN: Should use index, rows <= 100
 * 
 * Query 2: Get mail with rewards (Mail detail)
 * SELECT pm.*, mr.* FROM player_mail pm
 * LEFT JOIN mail_reward mr ON pm.id = mr.mail_id
 * WHERE pm.id = ?
 * INDEX: (id) on both tables
 * 
 * Query 3: Load rewards for claim (Claim flow)
 * SELECT id, reward_type, reward_id, amount, claimed
 * FROM mail_reward
 * WHERE mail_id = ? AND claimed = false
 * INDEX: (mail_id, claimed)
 * 
 * Query 4: Bulk insert mails (Broadcast)
 * INSERT INTO player_mail (...) VALUES (...),...,(...) [1000 values]
 * Batch: 1000 rows per query
 * Performance: ~10-50ms per 1000 rows
 * 
 * Query 5: Delete expired mails (Cleanup cron)
 * DELETE FROM player_mail WHERE expired_at < NOW()
 * INDEX: (expired_at)
 * Safety: Run in off-peak hours, limit 10000 per run
 * 
 * ==============================================================================
 * 5. PERFORMANCE METRICS
 * ==============================================================================
 * 
 * Typical Performance (Per player, 100 mails):
 * - Load mails: < 10ms (with index)
 * - Load mail detail: < 20ms
 * - Claim reward: < 50ms (with transaction)
 * - Delete mail: < 5ms
 * 
 * Broadcast Performance (1M players):
 * - Create queue: < 1s (bulk insert)
 * - Process batch: ~1-2s per 1000 mails
 * - Total time: ~15-30 min (depends on worker threads)
 * 
 * Storage Estimation:
 * - player_mail: ~500 bytes per row
 * - mail_reward: ~50 bytes per row
 * - 1M players × 100 mails × 500B = 50GB
 * - 1M players × 100 mails × 2 rewards × 50B = 10GB (rewards)
 * - Total: ~60GB (before indexes)
 * 
 * ==============================================================================
 * 6. ERROR HANDLING
 * ==============================================================================
 * 
 * Claim Reward Errors:
 * 
 * 1. Reward not found
 *    Exception: "Reward không tồn tại"
 *    Action: Log error, return 404
 *
 * 2. Already claimed
 *    Exception: "Reward đã nhận"
 *    Action: Return 400 (already processed)
 *
 * 3. Inventory full
 *    Exception: "Kho đầy"
 *    Action: Return 400, don't mark claimed
 *
 * 4. Transaction timeout
 *    Exception: "Timeout khi xử lý"
 *    Action: Rollback, return 500, retry
 *
 * Best practice:
 * try {
 *     mailService.claimReward(rewardId);
 * } catch (RewardAlreadyClaimedException e) {
 *     return ResponseEntity.ok("Already claimed");  // Idempotent
 * } catch (InventoryFullException e) {
 *     return ResponseEntity.status(400).body("Inventory full");
 * } catch (Exception e) {
 *     log.error("Claim failed", e);
 *     return ResponseEntity.status(500).body("Server error");
 * }
 * 
 * ==============================================================================
 * 7. TESTING CHECKLIST
 * ==============================================================================
 * 
 * Unit Tests:
 * ✓ PlayerMail creation
 * ✓ PlayerMail expiration logic
 * ✓ MailReward claim validation
 * ✓ Service layer logic
 *
 * Integration Tests:
 * ✓ DAO operations
 * ✓ Transaction rollback
 * ✓ Cascade delete
 *
 * Stress Tests:
 * ✓ 10K concurrent claims
 * ✓ 1M bulk insert
 * ✓ Database backup during cleanup
 *
 * Edge Cases:
 * ✓ Claim same reward twice
 * ✓ Load expired mail
 * ✓ Move mail > 200 limit
 * ✓ Broadcast to offline players
 * ✓ Database failover
 * 
 * ==============================================================================
 * 8. MONITORING & ALERTING
 * ==============================================================================
 * 
 * Metrics to track:
 * - Average mails per player
 * - Claim success rate
 * - Broadcast queue length
 * - Database query time
 * - Cleanup job duration
 * 
 * Alerting thresholds:
 * - Mail cleanup > 5 minutes
 * - Queue size > 100K
 * - Claim error rate > 1%
 * - Database slow queries > 1s
 * - Storage growth > 10GB/day
 * 
 * ==============================================================================
 */
public class MailSystemBestPractices {
    
    // This is a documentation class
    // See MAIL_SYSTEM.md for detailed guide
}
