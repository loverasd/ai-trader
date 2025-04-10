package com.cp.aitg.okx.common;

public class OkxApiConstants {

    // Headers
    public static final String OK_ACCESS_KEY_HEADER = "OK-ACCESS-KEY";
    public static final String OK_ACCESS_SIGN_HEADER = "OK-ACCESS-SIGN";
    public static final String OK_ACCESS_TIMESTAMP_HEADER = "OK-ACCESS-TIMESTAMP";
    public static final String OK_ACCESS_PASSPHRASE_HEADER = "OK-ACCESS-PASSPHRASE";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String OK_SIMULATED_TRADING_HEADER = "x-simulated-trading"; // 模拟盘

    // API Paths (V5)
    public static final String PLACE_ORDER_PATH = "/api/v5/trade/order";
    public static final String PLACE_ALGO_ORDER_PATH = "/api/v5/trade/order-algo";
    public static final String CANCEL_ORDER_PATH = "/api/v5/trade/cancel-order";
    public static final String CANCEL_ALGO_ORDER_PATH = "/api/v5/trade/cancel-algo-order";
    public static final String GET_ORDER_DETAILS_PATH = "/api/v5/trade/order"; // GET method
    public static final String GET_ALGO_ORDER_PENDING_PATH = "/api/v5/trade/orders-algo-pending";
    public static final String GET_ALGO_ORDER_HISTORY_PATH = "/api/v5/trade/orders-algo-history"; // state=live/paused
    public static final String GET_POSITIONS_PATH = "/api/v5/account/positions";

    // Success Code
    public static final String SUCCESS_CODE = "0";

}