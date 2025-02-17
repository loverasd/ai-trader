package com.cp.aitg.notification;

/**
 * 通知服务接口，用于将重要事件进行消息推送。
 */
public interface NotificationService {
    /**
     * 发送通知消息。
     * @param message 通知内容
     */
    void notify(String message);
}
