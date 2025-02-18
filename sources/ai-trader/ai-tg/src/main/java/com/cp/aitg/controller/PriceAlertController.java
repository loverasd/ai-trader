package com.cp.aitg.controller;

import com.cp.aitg.service.PriceAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/price-alert")
public class PriceAlertController {
    private final PriceAlertService priceAlertService;

    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }

    @PostMapping("/set")
    public ResponseEntity<String> setPriceAlert(@RequestParam String symbol, @RequestParam double targetPrice) {
        priceAlertService.setPriceAlert(symbol, targetPrice);
        return ResponseEntity.ok("Price alert set for " + symbol + " at " + targetPrice);
    }
}
