package com.shuaibu.controller;

import com.shuaibu.model.ProductModel;
import com.shuaibu.repository.ProductRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class StockWebSocketController {

    private final ProductRepository productRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public StockWebSocketController(ProductRepository productRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.productRepository = productRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void checkLowStock() {
        // System.out.println("Running low stock check...");

        List<ProductModel> lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getQuantity() <= p.getLowQuantityAlert())
                .collect(Collectors.toList());

        // System.out.println("Found " + lowStockProducts.size() + " low stock
        // products");

        List<String> productNames = lowStockProducts.stream()
                .map(ProductModel::getName)
                .collect(Collectors.toList());

        // Add debug logging before sending
        // System.out.println("Sending message to /topic/low-stock with: " +
        // Map.of("count", lowStockProducts.size(), "products", productNames));

        messagingTemplate.convertAndSend("/topic/low-stock",
                Map.of(
                        "count", lowStockProducts.size(),
                        "products", productNames));

        // Add debug logging after sending
        // System.out.println("Message sent to /topic/low-stock");
    }
}