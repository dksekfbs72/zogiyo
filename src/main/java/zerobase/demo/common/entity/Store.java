package zerobase.demo.common.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import zerobase.demo.common.type.StoreOpenCloseStatus;
import zerobase.demo.owner.dto.CreateStore;
import zerobase.demo.owner.dto.UpdateStore;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Store extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String storeAddr;
	private Integer orderCount;
	private String pictureUrl;
	private Double deliveryDistanceKm;
	private String summary;

	@Enumerated(EnumType.STRING)
	private StoreOpenCloseStatus openClose;

	private Integer deliveryTip;
	private Double commission;
	private LocalDateTime openCloseDt;

	private Double lat; //위도
	private Double lon; //경도

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user; //owner

	@OneToMany(mappedBy = "store")
	private List<Menu> menuList;

	public static Store fromCreateStore(CreateStore createStore) {

		return Store.builder()
			.name(createStore.getName())
			.commission(createStore.getCommission())
			.deliveryDistanceKm(createStore.getDeliveryDistanceKm())
			.deliveryTip(createStore.getDeliveryTip())
			.storeAddr(createStore.getStoreAddr())
			.summary(createStore.getSummary())
			.pictureUrl(createStore.getPictureUrl())
			.lat(createStore.getLat())
			.lon(createStore.getLon())
			.build();
	}

	public void setFromUpdateStoreDto(UpdateStore dto) {
		this.name = dto.getName();
		this.storeAddr = dto.getStoreAddr();
		this.pictureUrl = dto.getPictureUrl();
		this.deliveryDistanceKm = dto.getDeliveryDistanceKm();
		this.summary = dto.getSummary();
		this.deliveryTip = dto.getDeliveryTip();
		this.commission = dto.getCommission();
		this.lat = dto.getLat();
		this.lon = dto.getLon();
	}
}