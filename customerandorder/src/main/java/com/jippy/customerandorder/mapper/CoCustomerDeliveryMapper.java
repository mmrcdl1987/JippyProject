package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.CoCustomerDeliveryAddressRequestDto;
import com.jippy.customerandorder.dto.CoCustomerDeliveryAddressResponseDto;
import com.jippy.customerandorder.dto.CoCustomerUnreachableRequestDto;
import com.jippy.customerandorder.entity.CoCustomerDeliveryAddress;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderRejection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

public class CoCustomerDeliveryMapper {

    private CoCustomerDeliveryMapper() {
    }

    public static CoOrderRejection mapToOrderRejectionEntity(CoCustomerUnreachableRequestDto requestDto, CoOrder order) {

        CoOrderRejection rejectionEntity = new CoOrderRejection();

        rejectionEntity.setOrderId(requestDto.getOrderId());

        // CUSTOMER CAUSED REJECTION
        rejectionEntity.setRejectedById(order.getCustomerId());

        rejectionEntity.setType("CUSTOMER");

        rejectionEntity.setReason(requestDto.getReason());

        rejectionEntity.setIsActive(true);

        rejectionEntity.setCreatedAt(LocalDateTime.now());

        // DRIVER WHO CREATED THIS ENTRY
        rejectionEntity.setCreatedBy(requestDto.getDriverId().intValue());

        return rejectionEntity;
    }

    //     to post the customer delivery address details to the database
    public static CoCustomerDeliveryAddress mapToEntity(CoCustomerDeliveryAddressRequestDto dto) {

        CoCustomerDeliveryAddress entity = new CoCustomerDeliveryAddress();

        entity.setCustomerId(dto.getCustomerId());

        GeometryFactory geometryFactory = new GeometryFactory();

//         this creates a point geometry using the provided
//         longitude and latitude from the DTO, and sets the SRID to 4326 (WGS 84)
//        for example, if the DTO has longitude = 77.5946 and latitude = 12.9716,
//        the resulting point will represent the location of Bangalore,
//        India in the WGS 84 coordinate system.
        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));

        point.setSRID(4326);

        entity.setLocation(point);

        entity.setDoorNo(dto.getDoorNo());
        entity.setBuildingName(dto.getBuildingName());
        entity.setLaneNo(dto.getLaneNo());
        entity.setArea(dto.getArea());
        entity.setCity(dto.getCity());

        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    public static CoCustomerDeliveryAddressResponseDto mapToResponseDto(CoCustomerDeliveryAddress customerDeliveryAddress) {

        CoCustomerDeliveryAddressResponseDto responseDto = new CoCustomerDeliveryAddressResponseDto();

        responseDto.setCustomerAddressId(customerDeliveryAddress.getCustomerAddressId());

        responseDto.setCustomerId(customerDeliveryAddress.getCustomerId());

//        checks if the location is not null before trying to access
//        its coordinates to avoid potential NullPointerExceptions.
//        If the location is null, it simply won't set the latitude and longitude in the response DTO.
        if (customerDeliveryAddress.getLocation() != null) {

            responseDto.setLatitude(customerDeliveryAddress.getLocation().getY());

            responseDto.setLongitude(customerDeliveryAddress.getLocation().getX());
        }

        responseDto.setDoorNo(customerDeliveryAddress.getDoorNo());

        responseDto.setBuildingName(customerDeliveryAddress.getBuildingName());

        responseDto.setLaneNo(customerDeliveryAddress.getLaneNo());

        responseDto.setArea(customerDeliveryAddress.getArea());

        responseDto.setCity(customerDeliveryAddress.getCity());

        return responseDto;

    }

}