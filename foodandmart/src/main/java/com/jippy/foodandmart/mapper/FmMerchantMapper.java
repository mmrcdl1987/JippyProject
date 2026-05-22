//	package com.jippy.foodandmart.mapper;
//
//    import com.jippy.foodandmart.constants.AppConstants;
//    import com.jippy.foodandmart.dto.MerchantRequestDTO;
//    import com.jippy.foodandmart.entity.*;
//
//    import java.time.LocalDateTime;
//
//	/**
//	 * Static utility class for mapping merchant-related DTOs to JPA entities.
//	 * All field mapping lives here; business validation lives in the service layer.
//	 */
//	public final class MerchantMapper {
//
//	    private MerchantMapper() {}
//
//	    public static Merchant toEntity(MerchantRequestDTO dto) {
//	        String fullName = dto.getFirstName().trim() + " " + dto.getLastName().trim();
//	        Merchant merchant = new Merchant();
//	        merchant.setMerchantName(fullName);
//	        merchant.setMerchantEmail(dto.getEmail().toLowerCase().trim());
//	        merchant.setMerchantPhone(dto.getPhone().trim());
//	        merchant.setMerchantBusinessType(dto.getOutletType());
//	        merchant.setIsActive(AppConstants.FLAG_YES);
//	        merchant.setStatus(AppConstants.STATUS_PENDING);
//	        merchant.setIsApproved(Boolean.valueOf(AppConstants.UN_APPROVED));
//	        return merchant;
//	    }
//
//	    public static MerchantKyc toKycEntity(MerchantRequestDTO dto, Merchant merchant) {
//	        MerchantKyc kyc = new MerchantKyc();
//	        kyc.setMerchant(merchant);
//	        kyc.setAadhaarNumber(dto.getAdhar() != null ? dto.getAdhar().trim() : null);
//	        kyc.setPanNumber(dto.getPan() != null ? dto.getPan().toUpperCase().trim() : null);
//	        kyc.setFssaiNumber(dto.getFssai() != null ? dto.getFssai().trim() : null);
//	        kyc.setVerified(Boolean.valueOf(AppConstants.UN_APPROVED));
//	        return kyc;
//	    }
//
//	    public static MerchantBankDetails toBankEntity(MerchantRequestDTO dto, Integer merchantId) {
//	        MerchantBankDetails bank = new MerchantBankDetails();
//	        bank.setAccountNumber(dto.getAccountNumber());
//	        bank.setBankName(dto.getBankLocation());
//	        bank.setAccountHolderName(dto.getNameInBankAccount());
//	        bank.setIfscCode(dto.getIfscCode() != null ? dto.getIfscCode().toUpperCase().trim() : null);
//	        bank.setUserType(AppConstants.TYPE_MERCHANT);
//	        bank.setRecipientId(merchantId);
//	        return bank;
//	    }
//
//	    public static Employee toEmployeeEntity(MerchantRequestDTO dto) {
//	        Employee employee = new Employee();
//	        String fullName = dto.getFirstName().trim() + " " + dto.getLastName().trim();
//	        employee.setEmployeeName(fullName);
//	        employee.setEmail(dto.getEmail().toLowerCase().trim());
//	        employee.setMobileNumber(dto.getPhone().trim());
//	        employee.setIsActive(AppConstants.FLAG_YES);
//	        return employee;
//	    }
//
//	    public static User toUserEntity(String userName, String password, Integer merchantId) {
//	        User user = new User();
//	        user.setUsername(userName);
//	        user.setPassword(password);
//	        user.setEmployeeId(merchantId);
//	        user.setUserType(AppConstants.TYPE_MERCHANT);
//	        user.setIsActive(AppConstants.FLAG_YES);
//	        return user;
//	    }
//
//		public static UserRolePermissions toUserRolesEntity(User user, RolePermissions rp) {
//
//			UserRolePermissions urp = new UserRolePermissions();
//
//			urp.setUserId(user.getUserId()); // correct user PK
//			urp.setRolePermissionId(rp.getRolePermissionId()); // link to role_permissions
//			urp.setCreatedAt(LocalDateTime.now());
//
//			return urp;
//		}
//
////		public static RolePerissions toRolePermissionsEntity(Roles role) {
////
////			RolePerissions rolePerissions= new RolePerissions();
////
////			return null;
////		}
//	}

package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmMerchantDto;
import com.jippy.foodandmart.dto.FmMerchantWithBankDto;
import com.jippy.foodandmart.dto.FmMerchantRequestDTO;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.projections.FmMerchantWithBankProjection;

import java.time.LocalDateTime;

/**
 * Unified Static utility class for mapping merchant-related DTOs, Entities, and Projections.
 */
public final class FmMerchantMapper {

	private FmMerchantMapper() {
		// Private constructor to prevent instantiation
	}

	// --- Merchant Entity Mappings ---

	/**
	 * Maps MerchantRequestDTO to Merchant Entity (Commonly used for Registration/Creation).
	 */
	public static FmMerchant toEntity(FmMerchantRequestDTO dto) {
		String fullName = dto.getFirstName().trim() + " " + dto.getLastName().trim();
		FmMerchant merchant = new FmMerchant();
		merchant.setMerchantName(fullName);
		merchant.setMerchantEmail(dto.getEmail().toLowerCase().trim());
		merchant.setMerchantPhone(dto.getPhone().trim());
		merchant.setMerchantBusinessType(dto.getOutletType());
		merchant.setIsActive(FmAppConstants.FLAG_YES);
		merchant.setStatus(FmAppConstants.STATUS_PENDING);
		merchant.setIsApproved(Boolean.valueOf(FmAppConstants.UN_APPROVED));
		merchant.setCreatedAt(LocalDateTime.now());
		return merchant;
	}

	/**
	 * Maps FmMerchantDto to FmMerchant Entity (Standard CRUD mapping).
	 */
	public static FmMerchant mapToMerchantEntity(FmMerchantDto merchantDto) {
		FmMerchant entity = new FmMerchant();
		entity.setMerchantId(merchantDto.getMerchantId());
		entity.setMerchantName(merchantDto.getMerchantName());
		entity.setMerchantEmail(merchantDto.getMerchantEmail());
		entity.setMerchantPhone(merchantDto.getMerchantPhone());
		entity.setMerchantBusinessType(merchantDto.getMerchantBusinessType());
		entity.setStatus(merchantDto.getStatus());
		entity.setCreatedAt(merchantDto.getCreatedAt());
		entity.setCreatedBy(merchantDto.getCreatedBy());
		entity.setUpdatedAt(merchantDto.getUpdatedAt());
		entity.setUpdatedBy(merchantDto.getUpdatedBy());
		entity.setIsActive(merchantDto.getIsActive());
		entity.setIsApproved(merchantDto.getIsApproved());
		return entity;
	}

	/**
	 * Maps FmMerchant Entity to FmMerchantDto.
	 */
	public static FmMerchantDto mapToMerchantDto(FmMerchant entityFromDb) {
		FmMerchantDto dto = new FmMerchantDto();
		dto.setMerchantId(entityFromDb.getMerchantId());
		dto.setMerchantName(entityFromDb.getMerchantName());
		dto.setMerchantEmail(entityFromDb.getMerchantEmail());
		dto.setMerchantPhone(entityFromDb.getMerchantPhone());
		dto.setMerchantBusinessType(entityFromDb.getMerchantBusinessType());
		dto.setStatus(entityFromDb.getStatus());
		dto.setCreatedAt(entityFromDb.getCreatedAt());
		dto.setCreatedBy(entityFromDb.getCreatedBy());
		dto.setUpdatedAt(entityFromDb.getUpdatedAt());
		dto.setUpdatedBy(entityFromDb.getUpdatedBy());
		dto.setIsActive(entityFromDb.getIsActive());
		dto.setIsApproved(entityFromDb.getIsApproved());
		return dto;
	}

	// --- Specialized Entity Mappings ---

	public static FmMerchantKyc toKycEntity(FmMerchantRequestDTO dto, FmMerchant merchant) {
		FmMerchantKyc kyc = new FmMerchantKyc();
		kyc.setMerchant(merchant);
		kyc.setAadhaarNumber(dto.getAdhar() != null ? dto.getAdhar().trim() : null);
		kyc.setPanNumber(dto.getPan() != null ? dto.getPan().toUpperCase().trim() : null);
		kyc.setFssaiNumber(dto.getFssai() != null ? dto.getFssai().trim() : null);
		kyc.setVerified(Boolean.valueOf(FmAppConstants.UN_APPROVED));
		return kyc;
	}

	public static FmMerchantBankDetails toBankEntity(FmMerchantRequestDTO dto, Integer merchantId) {
		FmMerchantBankDetails bank = new FmMerchantBankDetails();
		bank.setAccountNumber(dto.getAccountNumber());
		bank.setBankName(dto.getBankLocation());
		bank.setAccountHolderName(dto.getNameInBankAccount());
		bank.setIfscCode(dto.getIfscCode() != null ? dto.getIfscCode().toUpperCase().trim() : null);
		bank.setUserType(FmAppConstants.TYPE_MERCHANT);
		bank.setRecipientId(merchantId);
		return bank;
	}

	public static FmEmployee toEmployeeEntity(FmMerchantRequestDTO dto) {
		FmEmployee employee = new FmEmployee();
		String fullName = dto.getFirstName().trim() + " " + dto.getLastName().trim();
		employee.setEmployeeName(fullName);
		employee.setEmail(dto.getEmail().toLowerCase().trim());
		employee.setMobileNumber(dto.getPhone().trim());
		employee.setIsActive(FmAppConstants.FLAG_YES);
		return employee;
	}

	public static FmUser toUserEntity(String userName, String password, Integer merchantId,String userType) {
		FmUser user = new FmUser();
		user.setUsername(userName);
		user.setPassword(password);
		user.setUserId(merchantId);
		user.setUserType(userType);
		user.setIsActive(FmAppConstants.FLAG_YES);
		return user;
	}

	public static FmUserRolePermissions toUserRolesEntity(FmUser user, FmRolePermissions rp) {
		FmUserRolePermissions urp = new FmUserRolePermissions();
		urp.setUserId(user.getUserId());
		urp.setRolePermission(rp);
		urp.setCreatedAt(LocalDateTime.now());
		return urp;
	}

	// --- Projection Mappings ---

	/**
	 * Maps Projection (joined data) to a combined Merchant and Bank DTO.
	 */
	public static FmMerchantWithBankDto mapToMerchantWithBankDto(FmMerchantWithBankProjection data) {
		if (data == null) {
			return null;
		}
		FmMerchantWithBankDto dto = new FmMerchantWithBankDto();

		// merchant details from projection
		dto.setMerchantId(data.getMerchantId());
		dto.setMerchantName(data.getMerchantName());
		dto.setMerchantEmail(data.getMerchantEmail());
		dto.setMerchantPhone(data.getMerchantPhone());
		dto.setBusinessType(data.getBusinessType());
		dto.setStatus(data.getStatus());

		// bank details from projection
		dto.setBankId(data.getBankId());
		dto.setRecipientId(data.getRecipientId());
		dto.setAccountNumber(data.getAccountNumber());
		dto.setIfscCode(data.getIfscCode());
		dto.setBankName(data.getBankName());
		dto.setAccountHolderName(data.getAccountHolderName());
		dto.setUserType(data.getUserType());

		return dto;
	}
}
