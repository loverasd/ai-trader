package com.cp.aitg.notification;

/**
 * 简单的控制台通知实现，直接在控制台输出通知信息。
 */
public class ConsoleNotificationService implements NotificationService {
    @Override
    public void notify(String message) {
        System.out.println("【通知】" + message);
    }
}
