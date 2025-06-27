package com.shuaibu.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.shuaibu.model.ProductModel;
import com.shuaibu.model.UserModel;
import com.shuaibu.repository.ProductRepository;
import com.shuaibu.repository.UserRepository;

@Controller
@RequestMapping("/")
public class HomeController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public HomeController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    
    @GetMapping
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("userModel", authentication.getName());
        } else {
            model.addAttribute("userModel", "Guest"); // Default text if not logged in
        }

        return "layout";
    }


    @GetMapping("/api/low-stock")
    @ResponseBody
    public Map<String, Object> getLowStockProducts() {
        List<ProductModel> productModels = productRepository.findAll().stream()
                .filter(p -> p.getQuantity() <= p.getLowQuantityAlert())
                .toList();

        Long count = (long) productModels.size();

        return Map.of(
                "count", count,
                "products", productModels.stream()
                        .map(ProductModel::getName) // Assuming ProductModel has a getName() method
                        .collect(Collectors.toList())
        );
    }

}
