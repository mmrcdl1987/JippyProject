package com.jippy.customerandorder.controller;
import com.jippy.customerandorder.dto.CustomerDeliveryChargeSettingsDTO;
import com.jippy.customerandorder.iservice.CustomerDeliveryChargeSettingsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/co/customer-delivery-charge-settings")
@RequiredArgsConstructor
@Validated
public class CustomerDeliveryChargeSettingsController {

    private final CustomerDeliveryChargeSettingsService service;

    @PostMapping
    public ResponseEntity<CustomerDeliveryChargeSettingsDTO> create(@Valid @RequestBody CustomerDeliveryChargeSettingsDTO dto, @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        CustomerDeliveryChargeSettingsDTO response = service.create(dto, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDeliveryChargeSettingsDTO>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDeliveryChargeSettingsDTO> getById(@PathVariable Integer id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<CustomerDeliveryChargeSettingsDTO>> getByCityId(@PathVariable Integer cityId) {

        return ResponseEntity.ok(service.getByCityId(cityId));
    }

    @GetMapping("/applicable")
    public ResponseEntity<CustomerDeliveryChargeSettingsDTO> getApplicablePlan(@RequestParam @NotNull Integer cityId, @RequestParam @NotNull @DecimalMin(value = "0.00", message = "Order value cannot be negative") BigDecimal orderValue) {

        return ResponseEntity.ok(service.getApplicablePlan(cityId, orderValue));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDeliveryChargeSettingsDTO> update(@PathVariable Integer id, @Valid @RequestBody CustomerDeliveryChargeSettingsDTO dto, @RequestHeader(value = "X-User-Id", required = false) Integer userId) {

        return ResponseEntity.ok(service.update(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}