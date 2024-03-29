package zerobase.demo.customer.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import zerobase.demo.common.entity.Coupon;
import zerobase.demo.common.entity.Menu;
import zerobase.demo.common.entity.Order;
import zerobase.demo.common.entity.Review;
import zerobase.demo.common.entity.UserCouponTbl;
import zerobase.demo.common.exception.CustomerException;
import zerobase.demo.common.exception.UserException;
import zerobase.demo.common.type.OrderStatus;
import zerobase.demo.common.type.ResponseCode;
import zerobase.demo.common.type.SelectStoreOpenType;
import zerobase.demo.common.type.SoldOutStatus;
import zerobase.demo.common.type.SortType;
import zerobase.demo.common.type.StoreOpenCloseStatus;
import zerobase.demo.coupon.repository.CouponRepository;
import zerobase.demo.customer.dto.CustomerStoreDetail;
import zerobase.demo.customer.dto.CustomerStoreInfo;
import zerobase.demo.customer.mapper.CustomerStoreMapper;
import zerobase.demo.customer.service.CustomerService;
import zerobase.demo.order.dto.OrderDto;
import zerobase.demo.order.repository.OrderRepository;
import zerobase.demo.owner.dto.MenuInfo;
import zerobase.demo.owner.repository.MenuRepository;
import zerobase.demo.owner.repository.StoreRepository;
import zerobase.demo.owner.service.MenuService;
import zerobase.demo.review.dto.ReviewDto;
import zerobase.demo.review.repository.ReviewRepository;
import zerobase.demo.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Primary
public class CustomerServiceImpl implements CustomerService {

	private final UserRepository userRepository;
	private final ReviewRepository reviewRepository;
	private final OrderRepository orderRepository;
	private final StoreRepository storeRepository;
	private final MenuRepository menuRepository;
	private final CustomerStoreMapper customerStoreMapper;
	private final MenuService menuService;
	private final CouponRepository couponRepository;
	@Override
	public boolean userAddReview(ReviewDto fromRequest, String userId) {

		Order order = orderRepository.findById(fromRequest.getOrderId())
			.orElseThrow(() -> new UserException(ResponseCode.ORDER_NOT_FOUND));

		if (order.isReviewed()) {
			throw new CustomerException(ResponseCode.ALREADY_ADDED_REVIEW);
		}
		if (order.getStatus() != OrderStatus.DELIVERY_COMPLETE) {
			throw new CustomerException(ResponseCode.DID_NOT_DELIVERY_COMPLETE);
		}
		if (!Objects.equals(order.getRestaurantId(), fromRequest.getRestaurantId())) {
			throw new CustomerException(ResponseCode.DIFF_ORDER_ID);
		}
		if (order.getOrderTime().plusDays(3).isBefore(LocalDateTime.now())) {
			throw new CustomerException(ResponseCode.TOO_OLD_AN_ORDER);
		}
		if (!order.getUserId().equals(userRepository.findByUserId(userId).get().getId())) {
			throw new CustomerException(ResponseCode.NOT_MY_ORDER);
		}

		Review review = new Review().builder()
			.writerId(userRepository.findByUserId(userId).get().getId())
			.orderId(fromRequest.getOrderId())
			.restaurantId(fromRequest.getRestaurantId())
			.summary(fromRequest.getSummary())
			.content(fromRequest.getContent())
			.reviewAddTime(LocalDateTime.now())
			.build();

		reviewRepository.save(review);
		order.setReviewed(true);
		orderRepository.save(order);
		return false;
	}

	@Override
	public List<OrderDto> getMyOrderList(String userId) {

		List<Order> orderDtoList = orderRepository
			.findAllByUserId(userRepository.findByUserId(userId).get().getId());
		if (orderDtoList.isEmpty()) {
			throw new CustomerException(ResponseCode.THERE_IS_NO_ORDER);
		}

		return OrderDto.fromEntity(orderDtoList);
	}

	@Override
	public List<Review> getStoreReview(Integer storeId) {
		if (!storeRepository.findById(storeId).isPresent()) {
			throw new CustomerException(ResponseCode.STORE_NOT_FOUND);
		}
		List<Review> list = reviewRepository.findAllByRestaurantId(storeId);
		if (list.isEmpty()) {
			throw new CustomerException(ResponseCode.THERE_IS_NO_REVIEW);
		}
		return list;
	}

	@Override
	public OrderDto putShoppingBasket(Integer storeId, String username, Integer menuId,
		Integer count) {
		Menu menu = menuRepository.findById(menuId).orElseThrow(
			() -> new CustomerException(ResponseCode.MENU_NOT_FOUND));
		if (menu.getSoldOutStatus() == SoldOutStatus.SOLD_OUT) {
			throw new CustomerException(ResponseCode.MENU_SOLD_OUT);
		}

		Integer userId = userRepository.findByUserId(username).get().getId();
		Optional<Order> order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.SHOPPING);
		Order newOrder;
		if (!order.isPresent()) {
			List<Integer> newMenus = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				newMenus.add(menuId);
			}
			newOrder = Order.builder()
				.price(menu.getPrice() * count)
				.menus(newMenus)
				.userId(userId)
				.status(OrderStatus.SHOPPING)
				.restaurantId(storeId)
				.reviewed(false)
				.build();
		} else {
			newOrder = order.get();
			if (!menu.getStore().getId().equals(newOrder.getRestaurantId())) {
				throw new CustomerException(ResponseCode.NOT_THIS_STORE_MENU);
			}
			List<Integer> newMenus = newOrder.getMenus();
			for (int i = 0; i < count; i++) {
				newMenus.add(menuId);
			}
			newOrder.setPrice(newOrder.getPrice() + (menu.getPrice() * count));
			newOrder.setMenus(newMenus);
		}
		orderRepository.save(newOrder);
		return OrderDto.request(newOrder);
	}

	@Override
	public OrderDto pullShoppingBasket(String username, Integer menuId) {
		Order order = orderRepository.findByUserIdAndStatus(
				userRepository.findByUserId(username).get().getId()
				, OrderStatus.SHOPPING)
			.orElseThrow(() -> new CustomerException(ResponseCode.ORDER_NOT_FOUND));

		Menu menu = menuRepository.findById(menuId).orElseThrow(
			() -> new CustomerException(ResponseCode.MENU_NOT_FOUND));

		List<Integer> newMenus = order.getMenus();
		if (!newMenus.contains(menuId)) {
			throw new CustomerException(ResponseCode.MENU_NOT_FOUND);
		}
		newMenus.remove(menuId);
		if (newMenus.isEmpty()) {
			orderRepository.delete(order);
			return null;
		}
		order.setMenus(newMenus);
		order.setPrice(order.getPrice() - menu.getPrice());
		orderRepository.save(order);

		return OrderDto.request(order);
	}

	@Override
	public OrderDto orderPayment(String username) {
		Order order = orderRepository.findByUserIdAndStatus(
				userRepository.findByUserId(username).get().getId()
				, OrderStatus.SHOPPING)
			.orElseThrow(() -> new CustomerException(ResponseCode.ORDER_NOT_FOUND));

		if (storeRepository.findById(order.getRestaurantId()).get().getOpenClose()
			== StoreOpenCloseStatus.CLOSE) {
			throw new CustomerException(ResponseCode.STORE_CLOSED);
		}

		order.setStatus(OrderStatus.PAYMENT);
		order.setDeliveryTime(60);
		order.setOrderTime(LocalDateTime.now());
		orderRepository.save(order);
		return OrderDto.request(order);
	}

	@Override
	public OrderDto cancelOrder(String username, Integer orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new CustomerException(
			ResponseCode.ORDER_NOT_FOUND));

		if (order.getStatus() != OrderStatus.PAYMENT) {
			throw new CustomerException(ResponseCode.ORDER_NOT_PAYMENT);
		}
		if (order.getOrderTime().plusMinutes(20).isBefore(LocalDateTime.now())) {
			throw new CustomerException(ResponseCode.TOO_OLD_AN_ORDER);
		}

		order.setStatus(OrderStatus.CANCEL);
		order.setDeliveryTime(null);
		orderRepository.save(order);
		return OrderDto.request(order);
	}

	@Override
	public OrderDto useCoupon(String username, Integer couponId) {
		Integer userId = userRepository.findByUserId(username).get().getId();
		Order order = orderRepository.findByUserIdAndStatus(userId,
			OrderStatus.SHOPPING).orElseThrow(() -> new CustomerException(ResponseCode.ORDER_NOT_FOUND));
		UserCouponTbl userCoupon = couponRepository.findByUserIdAndAndCouponId(userId, couponId)
			.orElseThrow(() -> new CustomerException(ResponseCode.NOT_HAVE_COUPON));

		if (userCoupon.getUsedTime() != null) throw new CustomerException(ResponseCode.USED_COUPON);
		if (!userCoupon.getCoupon().getRestaurantId().equals(order.getRestaurantId()))
			throw new CustomerException(ResponseCode.NOT_THIS_STORE_COUPON);

		order.setPrice(order.getPrice()-userCoupon.getCoupon().getSalePrice());
		if (order.getPrice() < 0) throw new CustomerException(ResponseCode.THERE_IS_NO_DISCOUNT);

		List<Coupon> list = order.getUseCoupon();
		list.add(userCoupon.getCoupon());
		order.setUseCoupon(list);
		userCoupon.setUsedTime(LocalDateTime.now());

		orderRepository.save(order);
		couponRepository.save(userCoupon);
		return OrderDto.request(order);
	}

	@Override
	public CustomerStoreInfo.Response getStoreList(CustomerStoreInfo.ListParam param) {

		Double userLat = param.getUserLat();
		Double userLon = param.getUserLon();
		//필수값 미입력
		if(userLat == null || userLon == null) throw new CustomerException(ResponseCode.BAD_REQUEST);
		//대한민국을 벗어날 경우
		if(isOutOfKorea(userLat, userLon)) { throw new CustomerException(ResponseCode.BAD_REQUEST); }

		setNull2Default(param);
		List<CustomerStoreInfo> customerStoreInfo =
			customerStoreMapper.selectList(param);

		return new CustomerStoreInfo.Response(ResponseCode.SELECT_STORE_SUCCESS, customerStoreInfo);
	}
	private void setNull2Default(CustomerStoreInfo.ListParam param)  {
		//기본값
		if(param.getMaxDistanceKm() == null) param.setMaxDistanceKm(3.0);
		if(param.getOpenType() ==null) param.setOpenType(SelectStoreOpenType.OPEN);
		if(param.getSortType() == null) param.setSortType(SortType.DISTANCE);
	}

	@Override
	public CustomerStoreDetail.Response getStoreDetail(CustomerStoreDetail.Request request) {

		CustomerStoreInfo customerStoreInfo = getStoreInfo(
			CustomerStoreInfo.SelectParam
				.builder()
				.storeId(request.getStoreId())
				.userLat(request.getUserLat())
				.userLon(request.getUserLon())
				.build()
		);

		MenuInfo.Response response = menuService.getMenuInfoByStoreId(request.getStoreId());

		CustomerStoreDetail customerStoreDetail = CustomerStoreDetail.from(customerStoreInfo, response.getMenuInfoList());

		return new CustomerStoreDetail.Response(ResponseCode.SELECT_STORE_DETAIL_SUCCESS, customerStoreDetail);
	}

	private CustomerStoreInfo getStoreInfo(CustomerStoreInfo.SelectParam param) {

		Double userLat = param.getUserLat();
		Double userLon = param.getUserLon();

		//필수값 미입력
		if(userLat == null || userLon == null) throw new CustomerException(ResponseCode.BAD_REQUEST);
		if(isOutOfKorea(userLat, userLon)) {throw new CustomerException(ResponseCode.BAD_REQUEST);}

		CustomerStoreInfo customerStoreInfo =  customerStoreMapper.selectStoreById(param)
			.orElseThrow(() -> new CustomerException(ResponseCode.STORE_NOT_FOUND));

		return customerStoreInfo;
	}

	private boolean isOutOfKorea(Double userLat, Double userLon) {
		if(userLat < 33.12 || userLat > 38.58
			|| userLon < 125.11 || userLon > 131.86) {
			return true;
		}
		return false;
	}
}
