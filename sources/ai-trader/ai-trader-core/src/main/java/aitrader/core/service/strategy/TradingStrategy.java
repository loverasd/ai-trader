package aitrader.core.service.strategy;

import aitrader.core.model.Position;
import aitrader.core.model.Symbol;
import binance.futures.enums.PositionSide;
import technicals.model.TechCandle;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 指标值的条目类
 */
class IndicatorEntry {
    // 字段定义
    TechCandle candle;
    double value;
}

/**
 * MACD指标的条目类
 */
class MACDEntry extends IndicatorEntry {
    // 字段定义
    double macdLine;
    double signalLine;
    double histogram;
}

/**
 * 移动平均线（MA）计算类
 */
class MovingAverage {
    public static IndicatorEntry[] calculateSMA(TechCandle[] candles, int periods) {
        if (candles.length < periods) {
            throw new IllegalArgumentException("数据不足，无法计算SMA");
        }

        IndicatorEntry[] smaEntries = new IndicatorEntry[candles.length - periods + 1];

        for (int i = periods - 1; i < candles.length; i++) {
            double sum = 0;
            for (int j = i - periods + 1; j <= i; j++) {
                sum += candles[j].getClosePrice();
            }
            double sma = sum / periods;
            IndicatorEntry entry = new IndicatorEntry();
            entry.candle = candles[i];
            entry.value = sma;
            smaEntries[i - periods + 1] = entry;
        }

        return smaEntries;
    }
}

/**
 * MACD指标计算类
 */
class MACD {
    public static MACDEntry[] calculateMACD(TechCandle[] candles, int shortPeriod, int longPeriod, int signalPeriod) {
        if (candles.length < longPeriod + signalPeriod) {
            throw new IllegalArgumentException("数据不足，无法计算MACD");
        }

        double[] emaShort = calculateEMA(candles, shortPeriod);
        double[] emaLong = calculateEMA(candles, longPeriod);

        int offset = emaShort.length - emaLong.length;
        double[] macdLine = new double[emaLong.length];
        for (int i = 0; i < emaLong.length; i++) {
            macdLine[i] = emaShort[i + offset] - emaLong[i];
        }

        double[] emaSignal = calculateEMA(macdLine, signalPeriod);
        double[] macdHistogram = new double[emaSignal.length];
        for (int i = 0; i < emaSignal.length; i++) {
            macdHistogram[i] = macdLine[i + signalPeriod - 1] - emaSignal[i];
        }

        MACDEntry[] macdEntries = new MACDEntry[emaSignal.length];
        for (int i = 0; i < macdEntries.length; i++) {
            int candleIndex = i + longPeriod + signalPeriod - 2;
            MACDEntry entry = new MACDEntry();
            entry.candle = candles[candleIndex];
            entry.macdLine = macdLine[i + signalPeriod - 1];
            entry.signalLine = emaSignal[i];
            entry.histogram = macdHistogram[i];
            macdEntries[i] = entry;
        }

        return macdEntries;
    }

    private static double[] calculateEMA(TechCandle[] candles, int periods) {
        double[] ema = new double[candles.length];
        double multiplier = 2.0 / (periods + 1);

        // 初始EMA为前periods个收盘价的平均值
        double sum = 0;
        for (int i = 0; i < periods; i++) {
            sum += candles[i].getClosePrice();
        }
        ema[periods - 1] = sum / periods;

        // 计算EMA
        for (int i = periods; i < candles.length; i++) {
            ema[i] = ((candles[i].getClosePrice() - ema[i - 1]) * multiplier) + ema[i - 1];
        }

        // 返回有效的EMA数组
        double[] result = new double[candles.length - periods + 1];
        System.arraycopy(ema, periods - 1, result, 0, result.length);
        return result;
    }

    private static double[] calculateEMA(double[] values, int periods) {
        double[] ema = new double[values.length];
        double multiplier = 2.0 / (periods + 1);

        double sum = 0;
        for (int i = 0; i < periods; i++) {
            sum += values[i];
        }
        ema[periods - 1] = sum / periods;

        for (int i = periods; i < values.length; i++) {
            ema[i] = ((values[i] - ema[i - 1]) * multiplier) + ema[i - 1];
        }

        double[] result = new double[values.length - periods + 1];
        System.arraycopy(ema, periods - 1, result, 0, result.length);
        return result;
    }
}

/**
 * 技术指标计算类
 */
class TechnicalRatings {
    public static final int UP_TREND = 1;
    public static final int DOWN_TREND = -1;
    public static final int NEUTRAL = 0;

    private int pricePrecision;
    private TechCandle[] candles;
    // 新增字段，用于存储计算好的指标数组
    private double[] sma30Array;
    private int[] macdStatusArray;

    private double[] sma;
    private int[] smaTrend;
    private double macd;
    private int macdStatus;
    private int trend;

    public TechnicalRatings(int pricePrecision) {
        this.pricePrecision = pricePrecision;
    }

    // 修改 calculate 方法，预先计算指标数组
    public void calculate(TechCandle[] candles, int[] smaPeriods) throws Exception {
        this.candles = candles;

        // 计算 SMA(30)
        sma30Array = calculateSMAArray(candles, 30);

        // 计算 MACD 状态数组
       macdStatusArray = calculateMACDStatusArray(candles, 12, 26, 9);
    }

    // 计算 SMA 数组
    private double[] calculateSMAArray(TechCandle[] candles, int period) {
        double[] smaArray = new double[candles.length];
        for (int i = 0; i < candles.length; i++) {
            if (i >= period - 1) {
                double sum = 0;
                for (int j = i - period + 1; j <= i; j++) {
                    sum += candles[j].getClosePrice();
                }
                smaArray[i] = sum / period;
            } else {
                smaArray[i] = Double.NaN; // 数据不足，无法计算
            }
        }
        return smaArray;
    }

    // 计算 MACD 状态数组
    private int[] calculateMACDStatusArray(TechCandle[] candles, int fastPeriod, int slowPeriod, int signalPeriod) {
        int[] macdStatusArray = new int[candles.length];
        MACDEntry[] macdEntries = MACD.calculateMACD(candles, fastPeriod, slowPeriod, signalPeriod);

        for (int i = 0; i < candles.length; i++) {
            if (i >= slowPeriod + signalPeriod - 2) {
                double macd = macdEntries[i - (slowPeriod + signalPeriod - 2)].macdLine;
                double signal = macdEntries[i - (slowPeriod + signalPeriod - 2)].signalLine;
                if (macd > signal) {
                    macdStatusArray[i] = 1;
                } else if (macd < signal) {
                    macdStatusArray[i] = -1;
                } else {
                    macdStatusArray[i] = 0;
                }
            } else {
                macdStatusArray[i] = 0; // 数据不足，无法计算
            }
        }
        return macdStatusArray;
    }

    private void calcTrend() {
        // 使用MA250和MA30来判断趋势
        int indexMA30 = periodIndexOf(30, new int[]{30, 250});
        int indexMA250 = periodIndexOf(250, new int[]{30, 250});

        if (smaTrend[indexMA250] == UP_TREND && smaTrend[indexMA30] == UP_TREND) {
            trend = UP_TREND;
        } else if (smaTrend[indexMA250] == DOWN_TREND && smaTrend[indexMA30] == DOWN_TREND) {
            trend = DOWN_TREND;
        } else {
            trend = NEUTRAL;
        }
    }

    private int calcMAvgTrend(double avgPrice, double closePrice) {
        BigDecimal close = BigDecimal.valueOf(closePrice).setScale(pricePrecision - 1, BigDecimal.ROUND_HALF_UP);
        BigDecimal price = BigDecimal.valueOf(avgPrice).setScale(pricePrecision - 1, BigDecimal.ROUND_HALF_UP);

        return (price.doubleValue() < close.doubleValue()) ? UP_TREND : (price.doubleValue() > close.doubleValue()) ? DOWN_TREND : NEUTRAL;
    }

    private int periodIndexOf(int period, int[] periods) {
        for (int i = 0; i < periods.length; i++) {
            if (periods[i] == period) {
                return i;
            }
        }
        throw new IllegalArgumentException("未找到周期：" + period);
    }

    // Getter方法

    public TechCandle[] getCandles() {
        return candles;
    }

    public double[] getSma() {
        return sma;
    }

    public int getMacdStatus() {
        return macdStatus;
    }

    public int getTrend() {
        return trend;
    }
    // 获取指标数组的方法
    public double[] getSma30Array() {
        return sma30Array;
    }

    public int[] getMacdStatusArray() {
        return macdStatusArray;
    }
}

/**
 * 交易策略类
 */
public class TradingStrategy {
    private double accountBalance;
    private List<Position> openPositions;
    private Map<Position, AdditionalData> positionDataMap; // 用于存储额外的数据，如stopLossPrice等
    private double maxRiskPerTrade;
    private double maxTotalRisk;
    private long lastTradeTime;

    public TradingStrategy(double accountBalance) {
        this.accountBalance = accountBalance;
        this.openPositions = new ArrayList<>();
        this.positionDataMap = new HashMap<>();
        this.maxRiskPerTrade = 0.03; // 单笔交易风险1%-3%
        this.maxTotalRisk = 0.1;     // 总体风险控制在10%以内
        this.lastTradeTime = 0;
    }

    public void executeStrategy(List<TechCandle> candles15M, List<TechCandle> candles30M, List<TechCandle> candles1H,
                                List<TechCandle> candles4H, List<TechCandle> candles1D) throws Exception {

        // 确定趋势，使用日线和4小时图的MA250和MA30
        TechnicalRatings dailyRatings = new TechnicalRatings(/* pricePrecision */ 2);
        dailyRatings.calculate(candles1D.toArray(new TechCandle[0]), new int[]{30, 250});

        TechnicalRatings ratings4H = new TechnicalRatings(/* pricePrecision */ 2);
        ratings4H.calculate(candles4H.toArray(new TechCandle[0]), new int[]{30, 250});

        int trendDaily = dailyRatings.getTrend();
        int trend4H = ratings4H.getTrend();

        int overallTrend;
        if (trendDaily == TechnicalRatings.UP_TREND && trend4H == TechnicalRatings.UP_TREND) {
            overallTrend = TechnicalRatings.UP_TREND;
        } else if (trendDaily == TechnicalRatings.DOWN_TREND && trend4H == TechnicalRatings.DOWN_TREND) {
            overallTrend = TechnicalRatings.DOWN_TREND;
        } else {
            overallTrend = TechnicalRatings.NEUTRAL;
        }

        // **预先计算15分钟K线数据的技术指标**
        TechnicalRatings entryRatings = new TechnicalRatings(/* pricePrecision */ 2);
        entryRatings.calculate(candles15M.toArray(new TechCandle[0]), new int[]{30});

        double[] sma30Array = entryRatings.getSma30Array();
        int[] macdStatusArray = entryRatings.getMacdStatusArray();

        // 遍历15分钟K线数据
        for (int i = 0; i < candles15M.size(); i++) {
            TechCandle candle = candles15M.get(i);
            double currentPrice = candle.getClosePrice();

            // 检查是否需要等待
            if (System.currentTimeMillis() - lastTradeTime < 15 * 60 * 1000) {
                continue; // 等待15分钟
            }

            // 检查数据是否足够
            if (Double.isNaN(sma30Array[i]) || macdStatusArray[i] == 0) {
                continue; // 数据不足，无法计算指标，跳过
            }

            double ma30 = sma30Array[i];
            int macdStatus = macdStatusArray[i];

            // 应用开单原则
            if (overallTrend == TechnicalRatings.UP_TREND) {
                // 线上不空，寻找多头入场机会
                if (shouldOpenLongPosition(candle, currentPrice, ma30, macdStatus, i, candles15M)) {
                    double stopLossPrice = calculateStopLoss(candles15M, i, true);
                    openPosition(true, currentPrice, stopLossPrice, candle);
                    lastTradeTime = System.currentTimeMillis();
                }
            } else if (overallTrend == TechnicalRatings.DOWN_TREND) {
                // 线下不多，寻找空头入场机会
                if (shouldOpenShortPosition(candle, currentPrice, ma30, macdStatus, i, candles15M)) {
                    double stopLossPrice = calculateStopLoss(candles15M, i, false);
                    openPosition(false, currentPrice, stopLossPrice, candle);
                    lastTradeTime = System.currentTimeMillis();
                }
            } else {
                // 趋势不明朗，不开仓
                continue;
            }

            // 检查持仓，判断是否止盈止损
            checkPositions(currentPrice);

            // 管理仓位
            managePositions();
        }
    }

    private boolean shouldOpenLongPosition(TechCandle candle, double currentPrice, double ma30, int macdStatus, int index, List<TechCandle> candles) {
        if (currentPrice > ma30 && macdStatus == 1) {
            if (isThreeLevelRetracement(candles.toArray(new TechCandle[0]), index, true)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldOpenShortPosition(TechCandle candle, double currentPrice, double ma30, int macdStatus, int index, List<TechCandle> candles) {
        if (currentPrice < ma30 && macdStatus == -1) {
            if (isThreeLevelRetracement(candles.toArray(new TechCandle[0]), index, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查15分钟周期内是否出现三级回踩或反弹
     */
    private boolean isThreeLevelRetracement(TechCandle[] candles, int index, boolean isUptrend) {
        int levels = 0;
        for (int i = index - 1; i > Math.max(0, index - 20); i--) {
            if (isUptrend) {
                // 检查是否连续三个低点抬高
                if (candles[i].getLowPrice() < candles[i + 1].getLowPrice()) {
                    levels++;
                } else {
                    break;
                }
            } else {
                // 检查是否连续三个高点降低
                if (candles[i].getHighPrice() > candles[i + 1].getHighPrice()) {
                    levels++;
                } else {
                    break;
                }
            }
            if (levels >= 3) {
                return true;
            }
        }
        return false;
    }

    private double calculateStopLoss(List<TechCandle> candles, int index, boolean isLong) {
        // 定义回溯周期，您可以根据策略需求调整该值
        int lookbackPeriod = 20; // 例如，使用最近20根K线
        if (candles.isEmpty() || index < 0) return 0;

        // 计算起始索引，确保不越界
        int start = Math.max(0, index - lookbackPeriod + 1);
        // 获取从起始索引到当前索引的子列表
        List<TechCandle> recentCandles = candles.subList(start, index + 1);

        if (isLong) {
            // 多单，止损设在最近的低点下方
            double recentLow = recentCandles.stream().mapToDouble(c -> c.getLowPrice()).min().orElse(0);
            return recentLow;
        } else {
            // 空单，止损设在最近的高点上方
            double recentHigh = recentCandles.stream().mapToDouble(c -> c.getHighPrice()).max().orElse(0);
            return recentHigh;
        }
    }
    private void openPosition(boolean isLong, double entryPrice, double stopLossPrice, TechCandle entryCandle) {
        // 计算头寸大小，控制单笔交易风险在1%-3%之间
        double riskPerTrade = accountBalance * maxRiskPerTrade;
        double stopLossDistance = Math.abs(entryPrice - stopLossPrice);
        double positionSize = riskPerTrade / stopLossDistance;

        // 确保总风险不超过最大限制
        double totalRisk = openPositions.stream().mapToDouble(p -> getRiskAmount(p)).sum() + riskPerTrade;
        if (totalRisk > accountBalance * maxTotalRisk) {
            System.out.println("无法开立新仓位，超过总风险限制。");
            return;
        }

        // 创建新的Position对象，使用您的Position类
        Position position = new Position();
        position.setSymbol(new Symbol("BTCUSDT",null,null,null,
                0,null,null,0,null)); // 假设交易对为BTCUSDT
        position.setOpen(true);
        position.setPositionSide(isLong ? PositionSide.LONG : PositionSide.SHORT);
        position.setMarginType("Cross"); // 假设为全仓
        position.setLeverage(BigDecimal.valueOf(20)); // 假设杠杆为20倍
        position.setEntryPrice(BigDecimal.valueOf(entryPrice));
        position.setQuantity(BigDecimal.valueOf(positionSize));
        position.setPnl(BigDecimal.ZERO);

        openPositions.add(position);

        // 存储额外的数据，如stopLossPrice和entryCandle
        AdditionalData additionalData = new AdditionalData();
        additionalData.stopLossPrice = stopLossPrice;
        additionalData.entryCandle = entryCandle;
        positionDataMap.put(position, additionalData);

        System.out.println("开立" + (isLong ? "多头" : "空头") + "仓位，价格：" + entryPrice + "，数量：" + positionSize);
    }

    private void checkPositions(double currentPrice) {
        List<Position> positionsToClose = new ArrayList<>();
        for (Position position : openPositions) {
            AdditionalData additionalData = positionDataMap.get(position);
            double stopLossPrice = additionalData.stopLossPrice;

            boolean isLong = position.getPositionSide() == PositionSide.LONG;
            if (isLong && currentPrice <= stopLossPrice) {
                positionsToClose.add(position);
                System.out.println("多头仓位触发止损，价格：" + currentPrice);
            } else if (!isLong && currentPrice >= stopLossPrice) {
                positionsToClose.add(position);
                System.out.println("空头仓位触发止损，价格：" + currentPrice);
            } else {
                // 可在此处添加止盈条件
                // 例如，当达到一定利润时止盈
            }
        }
        for (Position position : positionsToClose) {
            closePosition(position, currentPrice);
        }
    }

    private void closePosition(Position position, double exitPrice) {
        AdditionalData additionalData = positionDataMap.get(position);

        openPositions.remove(position);
        positionDataMap.remove(position);

        double entryPrice = position.getEntryPrice().doubleValue();
        double positionSize = position.getQuantity().doubleValue();
        boolean isLong = position.getPositionSide() == PositionSide.LONG;

        double profitLoss = (exitPrice - entryPrice) * positionSize * (isLong ? 1 : -1);
        accountBalance += profitLoss;

        position.setOpen(false);
        position.setPnl(BigDecimal.valueOf(profitLoss));

        System.out.println("平仓" + (isLong ? "多头" : "空头") + "仓位，价格：" + exitPrice + "，盈亏：" + profitLoss);
    }

    private double getRiskAmount(Position position) {
        AdditionalData additionalData = positionDataMap.get(position);
        double entryPrice = position.getEntryPrice().doubleValue();
        double stopLossPrice = additionalData.stopLossPrice;
        double positionSize = position.getQuantity().doubleValue();
        return Math.abs(entryPrice - stopLossPrice) * positionSize;
    }

    private void managePositions() {
        // 实现仓位管理，例如调整止损，或者根据新信号平仓
    }
}

/**
 * 额外数据类，用于存储Position中未包含的字段
 */
class AdditionalData {
    public double stopLossPrice;
    public TechCandle entryCandle;
}

