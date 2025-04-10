package com.cp.aitg.notification;

import com.cp.aitg.persistence.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {
    private final TelegramLongPollingBot telegramBot;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public NotificationService(TelegramLongPollingBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendPriceAlertNotification(String symbol, double targetPrice, double currentPrice) {
        SendMessage message = new SendMessage();
        String telegramChatId = System.getProperty("telegram.chat.id");

        message.setChatId(telegramChatId);
        message.setText("Price Alert: " + symbol + " reached " + currentPrice);

        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送通用文本消息到配置的默认 Chat ID.
     *
     * @param text 消息内容
     */
    public void sendGeneralMessage(String text) {
        SendMessage message = new SendMessage();
        String telegramChatId = System.getProperty("telegram.chat.id");

        message.setChatId(telegramChatId);
        message.setText(text);
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            log.warn("通用 Telegram 消息发送失败.");
            e.printStackTrace();
        }

    }

    /**
     * 发送通用文本消息到配置的默认 Chat ID.
     *
     * @param text 消息内容
     */
    public void sendMessage(String text) {
        SendMessage message = new SendMessage();
        String telegramChatId = System.getProperty("telegram.chat.id");

        message.setChatId(telegramChatId);
        message.setText(text);
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            log.warn("通用 Telegram 消息发送失败.");
            e.printStackTrace();
        }

    }

    /**
     * 发送开仓成功通知.
     *
     * @param state 包含订单信息的 OrderState 对象
     */
    public void sendPositionOpened(OrderState state) {
        if (state == null) return;
        String entryPrice = StringUtils.hasText(state.getActualEntryPrice()) ? state.getActualEntryPrice() : state.getEntryPrice();
        String openedAtStr = state.getOpenedAt() != null ? state.getOpenedAt().format(DTF) : "N/A";

        String msg = String.format(
                "*开仓成功* ✅\n" +
                        "-------\n" +
                        "🕰️ 时间: `%s`\n" +
                        "🏷️ 来源: `%s`\n" +
                        "📉 交易对: `%s`\n" +
                        "🧭 方向: `%s %s`\n" +
                        "🔢 数量: `%s`\n" +
                        "💰 开仓价: `%s`\n" +
                        "🚫 止损触发: `%s`\n" +
                        "🎯 止盈触发: `%s`\n" +
                        "🆔 AlgoID: `%s`\n" +
                        "👤 ClOrdID: `%s`",
                openedAtStr,
                state.getSource() != null ? state.getSource() : "未知",
                state.getInstId(),
                state.getSide() != null ? state.getSide().toUpperCase() : "?",
                state.getPosSide() != null ? state.getPosSide().toUpperCase() : "?",
                state.getSize(),
                entryPrice != null ? entryPrice : "N/A",
                state.getSlTriggerPx() != null ? state.getSlTriggerPx() : "未设置",
                state.getTpTriggerPx() != null ? state.getTpTriggerPx() : "未设置",
                state.getAlgoId(),
                state.getClOrdId()
        );
        log.info("准备发送开仓成功通知: AlgoID={}", state.getAlgoId());
        sendMessage(msg);
    }

    /**
     * 发送平仓通知.
     *
     * @param state 包含订单信息的 OrderState 对象
     */
    public void sendPositionClosed(OrderState state) {
        if (state == null) return;
        String reason = StringUtils.hasText(state.getCloseReason()) ? state.getCloseReason() : "未知";
        String closedAtStr = state.getClosedAt() != null ? state.getClosedAt().format(DTF) : "N/A";
        String pnlStr = calculateAndFormatPnl(state); // 计算盈亏

        String msg = String.format(
                "*平仓通知* 🏁\n" +
                        "-------\n" +
                        "🕰️ 时间: `%s`\n" +
                        "🏷️ 来源: `%s`\n" +
                        "📉 交易对: `%s`\n" +
                        "🧭 方向: `%s %s`\n" +
                        "🔢 数量: `%s`\n" +
                        "💰 开仓价: `%s`\n" +
                        "💰 平仓价: `%s`\n" +
                        "📄 平仓原因: `%s`\n" +
                        "💸 盈亏: *%s*\n" + // 盈亏加粗显示
                        "🆔 AlgoID: `%s`\n" +
                        "👤 ClOrdID: `%s`",
                closedAtStr,
                state.getSource() != null ? state.getSource() : "未知",
                state.getInstId(),
                state.getSide() != null ? state.getSide().toUpperCase() : "?",
                state.getPosSide() != null ? state.getPosSide().toUpperCase() : "?",
                state.getSize(),
                state.getActualEntryPrice() != null ? state.getActualEntryPrice() : (state.getEntryPrice() != null ? state.getEntryPrice() + "(计划)" : "N/A"),
                state.getExitPrice() != null ? state.getExitPrice() : "N/A",
                reason,
                pnlStr,
                state.getAlgoId(),
                state.getClOrdId()
        );
        log.info("准备发送平仓通知: AlgoID={}, Reason={}", state.getAlgoId(), reason);
        sendMessage(msg);
    }

    /**
     * 发送错误或告警通知.
     *
     * @param errorMessage 错误摘要信息
     * @param throwable    可选的异常对象，用于打印堆栈信息（注意控制长度）
     */
    public void sendErrorAlert(String errorMessage, Throwable throwable) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("*系统告警* ⚠️\n");
        msgBuilder.append("-------\n");
        msgBuilder.append("错误信息: `").append(errorMessage).append("`\n");

        if (throwable != null) {
            msgBuilder.append("异常类型: `").append(throwable.getClass().getSimpleName()).append("`\n");
            // 只取前几行堆栈信息，防止消息过长
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                msgBuilder.append("堆栈追踪 (部分):\n");
                msgBuilder.append("```\n"); // 使用代码块格式化堆栈
                for (int i = 0; i < Math.min(stackTrace.length, 5); i++) { // 最多显示 5 行
                    msgBuilder.append(stackTrace[i].toString()).append("\n");
                }
                if (stackTrace.length > 5) {
                    msgBuilder.append("...\n");
                }
                msgBuilder.append("```\n");
            }
        }
        log.error("准备发送错误告警: {}", errorMessage); // 使用 error 级别记录告警
        sendMessage(msgBuilder.toString());
    }


    // --- 辅助方法 ---

    /**
     * 简单计算并格式化盈亏 (需要完善).
     * 注意：此方法非常简化，未考虑合约面值、手续费、资金费率等。
     * 实际项目中需要根据合约类型精确计算。
     *
     * @param state 订单状态
     * @return 格式化的盈亏字符串，或 "N/A"
     */
    private String calculateAndFormatPnl(OrderState state) {
        if (state == null || !StringUtils.hasText(state.getActualEntryPrice()) || !StringUtils.hasText(state.getExitPrice()) || !StringUtils.hasText(state.getSize())) {
            return "N/A (数据不足)";
        }

        try {
            BigDecimal entryPrice = new BigDecimal(state.getActualEntryPrice());
            BigDecimal exitPrice = new BigDecimal(state.getExitPrice());
            BigDecimal size = new BigDecimal(state.getSize()); // 假设 size 是数量单位

            // TODO: 这里需要根据 instId 判断是 U 本位、币本位、合约面值等，计算方法完全不同！
            // 假设是最简单的线性合约（如 BTC-USDT），size 是 BTC 数量
            // 并且假设是 U 本位合约，盈亏 = (平仓价 - 开仓价) * 数量
            BigDecimal pnl;
            if ("buy".equalsIgnoreCase(state.getSide()) || "long".equalsIgnoreCase(state.getPosSide())) { // 买入开多
                pnl = (exitPrice.subtract(entryPrice)).multiply(size);
            } else if ("sell".equalsIgnoreCase(state.getSide()) || "short".equalsIgnoreCase(state.getPosSide())) { // 卖出开空
                pnl = (entryPrice.subtract(exitPrice)).multiply(size);
            } else {
                return "N/A (方向未知)";
            }

            // 格式化输出，保留合理小数位数 (例如 4 位)
            String formattedPnl = pnl.setScale(4, RoundingMode.HALF_UP).toPlainString();
            String prefix = pnl.compareTo(BigDecimal.ZERO) >= 0 ? "+" : ""; // 正数加号
            return prefix + formattedPnl;

        } catch (NumberFormatException e) {
            log.warn("计算盈亏时数字格式错误: Entry={}, Exit={}, Size={}", state.getActualEntryPrice(), state.getExitPrice(), state.getSize(), e);
            return "N/A (计算错误)";
        } catch (Exception e) {
            log.error("计算盈亏时发生未知异常: AlgoID={}", state.getAlgoId(), e);
            return "N/A (计算异常)";
        }
    }
}
