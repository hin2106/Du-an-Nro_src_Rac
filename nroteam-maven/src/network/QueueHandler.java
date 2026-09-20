package network;

import lombok.NonNull;
import lombok.Setter;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import interfaces.IMessageHandler;
import interfaces.ISession;

/**
 * QueueHandler - processes messages in dedicated thread
 * Kept original logic for backward compatibility
 */
public class QueueHandler implements Runnable {

    private ISession session;
    private BlockingDeque<Message> messages;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    @Setter
    private IMessageHandler messageHandler;

    public QueueHandler(@NonNull ISession session) {
        try {
            this.session = session;
            this.messages = new LinkedBlockingDeque<>();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void run() {
        scheduleProcessing();
    }

    public void addMessage(Message msg) {
        try {
            boolean isAuthed = (session instanceof MySession) && ((MySession) session).joinedGame;
            int cap = 8;
            if (isAuthed) {
                cap = 200;
            } else if (session instanceof MySession) {
                MySession mySession = (MySession) session;
                if (mySession.hasLoginIntent()) {
                    cap = 24;
                } else if (mySession.isLoginFlowStarted()) {
                    cap = 12;
                }
            }
            if (session.isConnected() && messages.size() < cap) {
                messages.add(msg);
                scheduleProcessing();
            } else {
                msg.cleanup();
            }
        } catch (Exception ignored) {
            msg.cleanup();
        }
    }

    private void scheduleProcessing() {
        if (session == null || messages == null || messages.isEmpty()
                || !processing.compareAndSet(false, true)) {
            return;
        }
        NetworkThreadPool.gI().submitMessageTask(this::drainMessages);
    }

    private void drainMessages() {
        try {
            int processed = 0;
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(10);
            while (session != null && session.isConnected() && processed < 64
                    && System.nanoTime() < deadline) {
                Message message = messages.poll();
                if (message == null) {
                    break;
                }
                try {
                    if (messageHandler != null) {
                        messageHandler.onMessage(session, message);
                    }
                } finally {
                    message.cleanup();
                }
                processed++;
            }
        } catch (Exception ignored) {
        } finally {
            processing.set(false);
            if (messages != null && !messages.isEmpty()) {
                scheduleProcessing();
            }
        }
    }

    public void close() {
        if (messages != null) {
            Message message;
            while ((message = messages.poll()) != null) {
                message.cleanup();
            }
        }
    }

    public void dispose() {
        this.session = null;
        this.messages = null;
        this.messageHandler = null;
    }
}
