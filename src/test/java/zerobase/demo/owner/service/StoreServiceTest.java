package zerobase.demo.owner.service;

import static org.junit.jupiter.api.Assertions.*;
import static zerobase.demo.common.type.ResponseCode.*;

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

import zerobase.demo.common.entity.Store;
import zerobase.demo.common.entity.User;
import zerobase.demo.common.exception.OwnerException;
import zerobase.demo.common.exception.UserException;
import zerobase.demo.common.type.Result;
import zerobase.demo.common.type.StoreOpenCloseStatus;
import zerobase.demo.common.type.UserStatus;
import zerobase.demo.owner.dto.CreateStore;
import zerobase.demo.owner.dto.OpenCloseStore;
import zerobase.demo.owner.dto.SetCommission;
import zerobase.demo.owner.dto.StoreInfo;
import zerobase.demo.owner.dto.UpdateStore;
import zerobase.demo.owner.repository.StoreRepository;
import zerobase.demo.owner.service.impl.StoreServiceImpl;
import zerobase.demo.redis.repository.RedisStoreInfoRepository;
import zerobase.demo.user.repository.UserRepository;
import zerobase.demo.user.service.UserService;


// @AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Transactional
public class StoreServiceTest {

	@Autowired
	private  UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private StoreServiceImpl storeService;

	@Autowired
	private StoreRepository storeRepository;

	public void createUser(String userId, UserStatus status) {
		User user = User.builder()
			.userId(userId)
			.status(status)
			.emailAuth(true)
			.password("1234")
			.build();
		userRepository.save(user);
	}

	@BeforeEach
	public void setRegisteredUser() {
		createUser("narangd2083", UserStatus.OWNER);
		createUser("cocacola2083", UserStatus.OWNER);
		createUser("coffee2083", UserStatus.USER);
	}

	@AfterEach
	public void deleteAll() {
		storeRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("???????????? ??????")
	void createStoreSuccess() throws Exception {

		//given
		String userId = "narangd2083";
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		String ownerId = userId;
		String name = "????????????";
		String storeAddr = "?????? ??????";
		String summary = "?????????????????????.";
		String pictureUrl = "https://naver.com";
		Double commission = 3.5;
		Double deliveryDistanceKm = 5.0;
		Integer deliveryTip = 3000;
		Double lat = 123.4545;
		Double lon = 92.332;


		CreateStore dto = CreateStore.builder()
			.loggedInUser(loggedUser)
			.ownerId(ownerId)
			.name(name)
			.storeAddr(storeAddr)
			.summary(summary)
			.pictureUrl(pictureUrl)
			.commission(commission)
			.deliveryDistanceKm(deliveryDistanceKm)
			.deliveryTip(deliveryTip)
			.lat(lat)
			.lon(lon)
			.build();

		//when
		CreateStore.Response response = storeService.createStore(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
	}

	@Test
	@DisplayName("???????????? ?????? - ????????? ????????? ???????????? ????????????")
	void createStoreUserNotFound() throws Exception {
		//given
		String userId = "narangd2083";
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		String ownerId = "coca-zero"; // not exist
		String name = "????????????";
		String storeAddr = "?????? ??????";
		String summary = "?????????????????????.";
		String pictureUrl = "https://naver.com";
		Double commission = 3.5;
		Double deliveryDistanceKm = 5.0;
		Integer deliveryTip = 3000;

		CreateStore dto = CreateStore.builder()
			.loggedInUser(loggedUser)
			.ownerId(ownerId)
			.name(name)
			.storeAddr(storeAddr)
			.summary(summary)
			.pictureUrl(pictureUrl)
			.commission(commission)
			.deliveryDistanceKm(deliveryDistanceKm)
			.deliveryTip(deliveryTip)
			.build();

		//when
		UserException exception = (UserException)assertThrows(RuntimeException.class, () -> {
			storeService.createStore(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), USER_NOT_FOUND);
	}

	@Test
	@DisplayName("???????????? ?????? - ???????????? ????????? ????????? ????????? ?????? ??????")
	void createStoreNotAuthorized() throws Exception {
		//given
		String userId = "narangd2083";
		UserDetails loggedUser = userService.loadUserByUsername(userId);

		String ownerId = "cocacola2083"; //different from Logged user
		String name = "????????????";
		String storeAddr = "?????? ??????";
		String summary = "?????????????????????.";
		String pictureUrl = "https://naver.com";
		Double commission = 3.5;
		Double deliveryDistanceKm = 5.0;
		Integer deliveryTip = 3000;

		CreateStore dto = CreateStore.builder()
			.loggedInUser(loggedUser)
			.ownerId(ownerId)
			.name(name)
			.storeAddr(storeAddr)
			.summary(summary)
			.pictureUrl(pictureUrl)
			.commission(commission)
			.deliveryDistanceKm(deliveryDistanceKm)
			.deliveryTip(deliveryTip)
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			storeService.createStore(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), NOT_AUTHORIZED);
	}


	Store createStore(String ownerId) {

		UserDetails loggedUser = userService.loadUserByUsername(ownerId);

		String name = "????????????";
		String storeAddr = "?????? ??????";
		String summary = "?????????????????????.";
		String pictureUrl = "https://naver.com";
		Double commission = 3.5;
		Double deliveryDistanceKm = 5.0;
		Integer deliveryTip = 3000;

		CreateStore dto = CreateStore.builder()
			.loggedInUser(loggedUser)
			.ownerId(ownerId)
			.name(name)
			.storeAddr(storeAddr)
			.summary(summary)
			.pictureUrl(pictureUrl)
			.commission(commission)
			.deliveryDistanceKm(deliveryDistanceKm)
			.deliveryTip(deliveryTip)
			.build();

		storeService.createStore(dto);

		List<Store> storeList = storeRepository.findAllByName("????????????");
		return storeList.get(0);
	}

	@Test
	@DisplayName("?????? ?????? ??????")
	void openCloseSuccess() throws Exception {

		//given
		Store store = createStore("narangd2083");

		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");
		StoreOpenCloseStatus newOpenCloseStatus = StoreOpenCloseStatus.OPEN;

		OpenCloseStore dto = OpenCloseStore.builder()
			.storeId(store.getId())
			.openClose(newOpenCloseStatus)
			.build();

		dto.setLoggedInUser(loggedUser);


		//when
		OpenCloseStore.Response response = storeService.openCloseStore(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), OPEN_STORE_SUCCESS);

	}

	@Test
	@DisplayName("?????? ?????? ?????? - ?????? ?????? ????????? ??????")
	void openCloseAlreadyOpened() throws Exception {

		//given
		Store store = createStore("narangd2083");

		UserDetails loggedUser = userService.loadUserByUsername("narangd2083");
		StoreOpenCloseStatus newOpenCloseStatus = StoreOpenCloseStatus.CLOSE;

		OpenCloseStore dto = OpenCloseStore.builder()
			.storeId(store.getId())
			.openClose(newOpenCloseStatus)
			.build();

		dto.setLoggedInUser(loggedUser);

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			storeService.openCloseStore(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), ALREADY_CLOSE);

	}

	@Test
	@DisplayName("?????? ?????? ?????? - ???????????? ????????? ????????? ????????? ?????? ??????")
	void openCloseNotAuthorized() throws Exception {

		//given
		Store store = createStore("narangd2083");

		UserDetails loggedUser = userService.loadUserByUsername("cocacola2083");
		StoreOpenCloseStatus newOpenCloseStatus = StoreOpenCloseStatus.CLOSE;

		OpenCloseStore dto = OpenCloseStore.builder()
			.storeId(store.getId())
			.openClose(newOpenCloseStatus)
			.build();

		dto.setLoggedInUser(loggedUser);

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			storeService.openCloseStore(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), NOT_AUTHORIZED);

	}

	@Test
	@DisplayName("?????? ?????? ?????? ??????")
	void getStoreInfoByOwnerIdSuccess() throws Exception {

		//given
		String ownerId = "narangd2083";
		Store store = createStore(ownerId);

		//when
		StoreInfo.Response response = storeService.getStoreInfoByOwnerId(ownerId);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);
		assertEquals(response.getCode(), SELECT_STORE_SUCCESS);
		assertFalse(response.getStoreInfoList().isEmpty());

		// System.out.println("############################################");
		// System.out.println(response.getStoreInfoList().get(0).getName());

	}

	@Test
	@DisplayName("?????? ?????? ?????? ?????? - ????????? ????????? owner??? ?????? ??????")
	void getStoreInfoByOwnerIdNotOwner() throws Exception {

		//given
		Store store = createStore("narangd2083");
		String ownerId = "coffee2083";

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			storeService.getStoreInfoByOwnerId(ownerId);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), NOT_OWNER);

	}

	@Test
	@DisplayName("???????????? ?????? ??????")
	void updateStoreSuccess() throws Exception {

		//given
		String userId = "narangd2083";
		Store store = createStore(userId);

		UserDetails loggedUser = userService.loadUserByUsername(userId);

		UpdateStore dto = UpdateStore.builder()
			.storeId(store.getId())
			.loggedInUser(loggedUser)
			.name("????????????")
			.summary("???????????? ?????????.")
			.build();

		//when
		UpdateStore.Response response = storeService.updateStore(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);

		Optional<Store> optionalStore = storeRepository.findById(store.getId());
		assertEquals(optionalStore.get().getName(), "????????????");
		assertEquals(optionalStore.get().getSummary(), "???????????? ?????????.");

	}

	@Test
	@DisplayName("???????????? ?????? ?????? - ???????????? ?????? ??????")
	void updateStoreNotExistStore() throws Exception {

		//given
		String userId = "narangd2083";
		Store store = createStore(userId);

		UserDetails loggedUser = userService.loadUserByUsername(userId);

		UpdateStore dto = UpdateStore.builder()
			.storeId(99)
			.loggedInUser(loggedUser)
			.name("????????????")
			.summary("???????????? ?????????.")
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			storeService.updateStore(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), STORE_NOT_FOUND);

	}

	@Test
	@DisplayName("???????????? ?????? ?????? - ???????????? ????????? ????????? ??????")
	void updateStoreNotAuthorized() throws Exception {

		//given
		Store store = createStore("narangd2083");

		UserDetails loggedUser = userService.loadUserByUsername("cocacola2083");

		UpdateStore dto = UpdateStore.builder()
			.storeId(store.getId())
			.loggedInUser(loggedUser)
			.name("????????????")
			.summary("???????????? ?????????.")
			.build();

		//when
		OwnerException exception = (OwnerException)assertThrows(RuntimeException.class, () -> {
			storeService.updateStore(dto);
		});

		//then
		assertEquals(exception.getResponseCode().getResult(), Result.FAIL);
		assertEquals(exception.getResponseCode(), NOT_AUTHORIZED);

	}

	@Test
	@DisplayName("?????????/????????? ?????? ??????")
	void setCommissionSuccess() throws Exception {

		//given
		Store store = createStore("narangd2083");

		SetCommission dto = SetCommission.builder()
			.storeId(store.getId())
			.commission(30.0)
			.build();

		//when
		SetCommission.Response response = storeService.setCommission(dto);

		//then
		assertEquals(response.getResult(), Result.SUCCESS);

		Optional<Store> optionalStore = storeRepository.findById(store.getId());
		assertEquals(optionalStore.get().getCommission(), 30.0);

	}

}
