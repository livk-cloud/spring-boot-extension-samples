package com.livk.modules;

import com.livk.modules.order.Order;
import com.livk.modules.order.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * <p>
 * App
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@SpringBootApplication
public class ModulesApp {

	void main(String[] args) {
		SpringApplication.run(ModulesApp.class, args);
	}

	@Bean
	CommandLineRunner run(OrderService orderService) {
		return _ -> {
			Order.Item item = new Order.Item("apple", 1, 500);
			Order order = new Order();
			order.setItems(List.of(item));

			orderService.createOrder(order);
		};
	}

}
