package zerobase.demo.customer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zerobase.demo.common.config.AllExceptionHandler;
import zerobase.demo.common.type.ResponseCode;
import zerobase.demo.customer.service.CustomerService;
import zerobase.demo.menu.dto.MenuDto;
import zerobase.demo.order.dto.OrderDto;
import zerobase.demo.order.dto.OrderDto.ListResponse;
import zerobase.demo.review.dto.ReviewDto;
import zerobase.demo.review.dto.ReviewRequest;
import zerobase.demo.user.dto.UserDto.Response;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CustomerController extends AllExceptionHandler {

	private final CustomerService customerService;

	@PostMapping("/customer/addReview")
	Response userAddReview(@RequestBody ReviewRequest request, Principal principal) {
		boolean result = customerService.userAddReview(ReviewDto.fromRequest(request), principal.getName());

		return new Response(ResponseCode.ADD_REVIEW_SUCCESS);
	}

	@GetMapping("/customer/getMyOrder")
	public ListResponse getMyOrderList(Principal principal) {

		return new ListResponse(customerService.getMyOrderList(principal.getName()), ResponseCode.GET_MY_ORDER_SUCCESS);
	}

	@GetMapping("/customer/{storeId}/review")
	public ReviewDto.Response getStoreReview(@PathVariable Integer storeId) {

		return new ReviewDto.Response(customerService.getStoreReview(storeId), ResponseCode.GET_STORE_REVIEW_SUCCESS);
	}

	@GetMapping("/customer/{storeId}/menu")
	public MenuDto.Response getStoreMenu(@PathVariable Integer storeId) {

		return new MenuDto.Response(customerService.getStoreMenu(storeId), ResponseCode.GET_STORE_MENU_SUCCESS);
	}

	@PostMapping("/customer/{storeId}")
	public OrderDto.Response putShoppingBasket(@PathVariable Integer storeId, Principal principal, @RequestParam Integer menuId,
		@RequestParam Integer count) {

		return new OrderDto.Response(customerService.putShoppingBasket(storeId, principal.getName(), menuId, count)
										, ResponseCode.PUT_THIS_MENU);
	}

	@DeleteMapping("/customer/pullShoppingBasket")
	public OrderDto.Response pullShoppingBasket(Principal principal, @RequestParam Integer menuId) {

		return new OrderDto.Response(customerService.pullShoppingBasket(principal.getName(), menuId)
			, ResponseCode.PULL_THIS_MENU);
	}

	@PutMapping("/customer/payment")
	public OrderDto.Response orderPayment(Principal principal) {

		return new OrderDto.Response(customerService.orderPayment(principal.getName()),
			ResponseCode.ORDER_SUCCESS);
	}

	@PutMapping("/customer/cancelOrder")
	public OrderDto.Response cancelOrder(Principal principal, @RequestParam Integer orderId) {
		return new OrderDto.Response(customerService.cancelOrder(principal.getName(), orderId),
			ResponseCode.ORDER_CANCEL_SUCCESS);
	}
}
