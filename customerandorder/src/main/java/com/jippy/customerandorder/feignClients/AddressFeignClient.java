    package com.jippy.customerandorder.feignClients;

    import com.jippy.customerandorder.dto.CoAddressRequestDto;
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    @FeignClient(
            name = "foodandmart",
            contextId = "addressFeignClient"
    )
    public interface AddressFeignClient {

        @PostMapping("/api/outlets/saveAddressDetails")
        ResponseEntity<CoAddressRequestDto> saveAddressDetails(
                @RequestBody CoAddressRequestDto fmAddressRequestDto);

        @GetMapping("/api/outlets/getAddressDetails")
        ResponseEntity<CoAddressRequestDto> getAddressDetails(
                @RequestParam Integer driverId);
    }
