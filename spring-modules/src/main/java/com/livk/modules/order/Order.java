package com.livk.modules.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * Order
 * </p>
 *
 * @author livk
 * @date 2026/2/28
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {

	private String no;

	private List<Item> items;

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class Item {

		private String productId;

		private Integer quantity;

		private Integer price;

	}

}
