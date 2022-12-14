package zerobase.demo.owner.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import zerobase.demo.common.entity.Menu;
import zerobase.demo.common.entity.Store;
import zerobase.demo.common.entity.User;
import zerobase.demo.common.exception.OwnerException;
import zerobase.demo.common.type.ResponseCode;
import zerobase.demo.common.type.Result;
import zerobase.demo.common.type.SoldOutStatus;
import zerobase.demo.common.type.UserStatus;
import zerobase.demo.owner.dto.CreateMenu;
import zerobase.demo.owner.dto.CreateStore;
import zerobase.demo.owner.dto.DeleteMenu;
import zerobase.demo.owner.dto.MenuInfo;
import zerobase.demo.owner.dto.SetSoldOutStatus;
import zerobase.demo.owner.dto.UpdateMenu;
import zerobase.demo.owner.repository.MenuRepository;
import zerobase.demo.owner.repository.StoreRepository;
import zerobase.demo.user.repository.UserRepository;
import zerobase.demo.user.service.UserService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Transactional
class MenuServiceTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private StoreService storeService;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private MenuService menuService;
	@Autowired
	private MenuRepository menuRepository;

	@BeforeEach
	public void setUp() {
		String userName1 = "narangd2083";
		String userName2 = "cola2083";
		String storeName1 = "narangdStore";
		String storeName2 = "colaStore";

		createUser(userName1, UserStatus.OWNER);
		createUser(userName2, UserStatus.OWNER);

		createStore(userName1, storeName1);
		createStore(userName2, storeName2);
	}
	@AfterEach
	public void deleteAll() {
		menuRepository.deleteAll();
		storeRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("???????????? ??????")
	void createMenuSuccess() throws Exception {

		//given
		String userId = "narangd2083";
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		Store store = storeRepository.findAllByName("narangdStore").get(0);
		int storeId = store.getId();

		CreateMenu dto = CreateMenu.builder()
			.loggedInUser(loggedUser)
			.storeId(storeId)
			.name("??????????????????")
			.price(1000)
			.pictureUrl("https://naver.com")
			.summary("??????????????????.")
			.build();

		//when
		CreateMenu.Response response = menuService.createMenu(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), ResponseCode.CREATE_MENU_SUCCESS);
		assertFalse(menuRepository.findAllByName("??????????????????").isEmpty());
	}

	@Test
	@DisplayName("???????????? ?????? - ???????????? ?????? ????????? ??????")
	void createMenuStoreNotFound() throws Exception {

		//given
		String userId = "narangd2083";
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		int storeId = 5555; //not exist

		CreateMenu dto = CreateMenu.builder()
			.loggedInUser(loggedUser)
			.storeId(storeId)
			.name("??????????????????")
			.price(1000)
			.pictureUrl("https://naver.com")
			.summary("??????????????????.")
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			menuService.createMenu(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ResponseCode.STORE_NOT_FOUND);
	}

	@Test
	@DisplayName("???????????? ?????? - ???????????? ????????? ?????? ????????? ?????? ??????")
	void createMenuNotAuthorized() throws Exception {

		//given
		String userId = "narangd2083";
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		Store store = storeRepository.findAllByName("colaStore").get(0);
		int storeId = store.getId();

		CreateMenu dto = CreateMenu.builder()
			.loggedInUser(loggedUser)
			.storeId(storeId)
			.name("??????????????????")
			.price(1000)
			.pictureUrl("https://naver.com")
			.summary("??????????????????.")
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			menuService.createMenu(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ResponseCode.NOT_AUTHORIZED);
	}

	@Test
	@DisplayName("??????????????? ?????? ??????")
	void setForSaleSuccess() throws Exception {

		//given
		createMenu("narangd2083", "narangdStore", "cider");
		Menu menu = menuRepository.findAllByName("cider").get(0);
		Integer menuId = menu.getId();
		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");

		SetSoldOutStatus dto = SetSoldOutStatus.builder()
			.loggedInUser(loggedUser)
			.menuId(menuId)
			.soldOutStatus(SoldOutStatus.FOR_SALE)
			.build();

		//when
		SetSoldOutStatus.Response response = menuService.setSoldOutStatus(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), ResponseCode.SET_SOLD_OUT_STATUS_SUCCESS);
	}

	@Test
	@DisplayName("??????????????? ?????? ?????? - ?????? ???????????? ??????")
	void setForSaleAlreadyForSale() throws Exception {

		//given
		createMenu("narangd2083", "narangdStore", "cider");
		Menu menu = menuRepository.findAllByName("cider").get(0);
		Integer menuId = menu.getId();
		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");

		SetSoldOutStatus dto = SetSoldOutStatus.builder()
			.loggedInUser(loggedUser)
			.menuId(menuId)
			.soldOutStatus(SoldOutStatus.FOR_SALE)
			.build();

		menuService.setSoldOutStatus(dto);

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			menuService.setSoldOutStatus(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ResponseCode.ALREADY_FOR_SAIL);
	}

	@Test
	@DisplayName("??????????????? ?????? ?????? - ???????????? ?????? ??????")
	void setForSaleMenuNotFound() throws Exception {

		//given
		createMenu("narangd2083", "narangdStore", "cider");
		Menu menu = menuRepository.findAllByName("cider").get(0);
		Integer menuId = menu.getId()+9999;
		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");

		SetSoldOutStatus dto = SetSoldOutStatus.builder()
			.loggedInUser(loggedUser)
			.menuId(menuId)
			.soldOutStatus(SoldOutStatus.FOR_SALE)
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			menuService.setSoldOutStatus(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ResponseCode.MENU_NOT_FOUND);
	}

	@Test
	@DisplayName("??????????????? ?????? ?????? - ???????????? ????????? ????????? ?????? ??????")
	void setForSaleMenuNotAuthorized() throws Exception {

		//given
		createMenu("narangd2083", "narangdStore", "cider");
		Menu menu = menuRepository.findAllByName("cider").get(0);
		Integer menuId = menu.getId();
		UserDetails loggedUser = userService.loadUserByUsername("cola2083");

		SetSoldOutStatus dto = SetSoldOutStatus.builder()
			.loggedInUser(loggedUser)
			.menuId(menuId)
			.soldOutStatus(SoldOutStatus.FOR_SALE)
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			menuService.setSoldOutStatus(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ResponseCode.NOT_AUTHORIZED);
	}

	@Test
	@DisplayName("?????? ?????? ??????")
	void updateMenuSuccess() throws Exception {

		//given
		createMenu("narangd2083", "narangdStore", "cider");
		Menu menu = menuRepository.findAllByName("cider").get(0);
		Integer menuId = menu.getId();
		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");

		UpdateMenu dto = UpdateMenu.builder()
			.loggedInUser(loggedUser)
			.menuId(menuId)
			.name("cider2")
			.price(5555)
			.summary("?????????2 ?????????.")
			.pictureUrl("url Update")
			.build();

		//when
		UpdateMenu.Response response = menuService.updateMenu(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), ResponseCode.UPDATE_MENU_SUCCESS);

		Optional<Menu> optionalMenu = menuRepository.findById(menuId);
		assertTrue(optionalMenu.isPresent());
		assertEquals(optionalMenu.get().getName(), "cider2");
		assertEquals(optionalMenu.get().getPrice(), 5555);
	}

	@Test
	@DisplayName("?????? ?????? ?????? - ???????????? ????????? ????????? ?????? ??????")
	void updateMenuNotAuthorized() throws Exception {

		//given
		createMenu("narangd2083", "narangdStore", "cider");
		Menu menu = menuRepository.findAllByName("cider").get(0);
		Integer menuId = menu.getId();

		UserDetails loggedUser = userService.loadUserByUsername("cola2083");

		UpdateMenu dto = UpdateMenu.builder()
			.loggedInUser(loggedUser)
			.menuId(menuId)
			.name("cider2")
			.price(5555)
			.summary("?????????2 ?????????.")
			.pictureUrl("url Update")
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			menuService.updateMenu(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ResponseCode.NOT_AUTHORIZED);
	}

	@Test
	@DisplayName("?????? ?????? ??????")
	@Transactional
	void getMenuInfoByStoreIdSuccess() throws Exception {

		//given

		Integer storeId = storeRepository.findAllByName("narangdStore").get(0).getId();

		createMenu("narangd2083", "narangdStore", "cider1");
		createMenu("narangd2083", "narangdStore", "cider2");
		createMenu("narangd2083", "narangdStore", "cider3");

		//when
		MenuInfo.Response response = menuService.getMenuInfoByStoreId(storeId);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), ResponseCode.SELECT_MENU_SUCCESS);

		assertEquals(response.getMenuInfoList().size(), 3);
		System.out.println(response.getMenuInfoList());
	}


	private Integer createMenu(String userId, String storeName, String menuName) {
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		Store store = storeRepository.findAllByName(storeName).get(0);
		int storeId = store.getId();

		CreateMenu dto = CreateMenu.builder()
			.loggedInUser(loggedUser)
			.storeId(storeId)
			.name(menuName)
			.price(1000)
			.pictureUrl("https://naver.com")
			.summary("????????????")
			.build();

		menuService.createMenu(dto);
		List<Menu> list = menuRepository.findAllByName(menuName);
		return list.get(0).getId();
	}

	private void createUser(String userId, UserStatus status) {
		User user = User.builder()
			.userId(userId)
			.status(status)
			.emailAuth(true)
			.password("1234")
			.build();
		userRepository.save(user);
	}

	private void createStore(String ownerId, String storeName) {

		UserDetails loggedUser = userService.loadUserByUsername(ownerId);

		String storeAddr = "?????? ??????";
		String summary = "??????????????????";
		String pictureUrl = "https://naver.com";
		Double commission = 3.5;
		Double deliveryDistanceKm = 5.0;
		Integer deliveryTip = 3000;
		Double lat = 123.4545;
		Double lon = 92.332;

		CreateStore dto = CreateStore.builder()
			.loggedInUser(loggedUser)
			.ownerId(ownerId)
			.name(storeName)
			.storeAddr(storeAddr)
			.summary(summary)
			.pictureUrl(pictureUrl)
			.commission(commission)
			.deliveryDistanceKm(deliveryDistanceKm)
			.deliveryTip(deliveryTip)
			.lat(lat)
			.lon(lon)
			.build();

		storeService.createStore(dto);
	}

	@Test
	@DisplayName("???????????? ??????")
	void deleteMenuSuccess() throws Exception {

		//given
		Integer menuId = createMenu("narangd2083", "narangdStore", "cider1");

		String userId = "narangd2083";

		DeleteMenu dto = DeleteMenu.builder()
			.menuId(menuId)
			.build();

		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");
		dto.setLoggedInUser(loggedUser);
		//when
		DeleteMenu.Response response = menuService.deleteMenu(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), ResponseCode.DELETE_MENU_SUCCESS);
		assertTrue(menuRepository.findAllByName("cider1").isEmpty());

	}

}