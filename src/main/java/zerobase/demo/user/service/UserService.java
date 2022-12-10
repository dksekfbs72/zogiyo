package zerobase.demo.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import zerobase.demo.common.type.ResponseCode;
import zerobase.demo.user.dto.UserDto;
import zerobase.demo.user.dto.UserUpdateDto;

public interface UserService {

	/**
	 * 신규 유저 생성
	 */
	boolean createUser(UserDto userDto);

	/**
	 * 관리자 유저 변경
	 */
	boolean adminUpdateUser(UserUpdateDto userDto, String myId);

	/**
	 * 접속중인 유저 정보 불러오기
	 */
	UserDto readMyInfo(String userId);

	/**
	 * 유저 패스워드 변경
	 */
	boolean changePassword(String myId, String password, String newPassword);

	/**
	 * 유저 탈퇴
	 */
	boolean userUnregister(String name, String password);

	/**
	 * 로그인 에러 코드 찾기
	 */
	ResponseCode getErrorCode(String errorCode);
}
