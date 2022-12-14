package zerobase.demo.customer.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Id;

import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.demo.common.entity.Store;
import zerobase.demo.common.model.BaseResponse;
import zerobase.demo.common.type.ResponseCode;
import zerobase.demo.common.type.SelectStoreOpenType;
import zerobase.demo.common.type.SortType;
import zerobase.demo.common.type.StoreOpenCloseStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerStoreInfo {

	private Integer id;
	private String name;
	private String storeAddr;
	private Integer orderCount;
	private String pictureUrl;
	private Double deliveryDistanceKm;
	private String summary;
	private StoreOpenCloseStatus openClose;
	private Integer deliveryTip;
	private LocalDateTime openCloseDt;
	private LocalDateTime regDt;
	private Double distanceKm;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class ListParam {
		Double userLat;
		Double userLon;

		//Optional
		Double maxDistanceKm; //default 3km
		SortType sortType; //default distance
		SelectStoreOpenType openType; //default open
		String keyword;
	}

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class SelectParam {
		Double userLat;
		Double userLon;

		Integer storeId;
	}


	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class Response extends BaseResponse {
		private List<CustomerStoreInfo> list;

		public Response(ResponseCode code, List<CustomerStoreInfo> list) {
			super(code);
			this.list = list;
		}
	}
}
