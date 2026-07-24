package com.jippy.customerandorder.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoZoneResponseDto;
import com.jippy.customerandorder.feignClients.DriverFeignClient;
import com.jippy.customerandorder.repository.CoCustomerCommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerMappingToCommunitySchedular {

    private final DriverFeignClient driverFeignClient;
    private final ObjectMapper objectMapper;
    private final CoCustomerCommunityRepository customerCommunityRepository;

    // Cron syntax: Second, Minute, Hour, Day of Month, Month, Day of Week
    @Scheduled(cron = "0 0 2 * * ?")
   // @Scheduled(cron = "0 */2 * * * ?")
    public void runCommunityMapping() {

        log.info("Starting 2 AM Community Mapping Scheduler Job...");

        try{
            ResponseEntity<List<CoZoneResponseDto>> communityZonesResponse = driverFeignClient
                    .getZonesByType(COConstants.ZONE_TYPE_COMMUNITY);

            if (communityZonesResponse.getStatusCode().is2xxSuccessful() && communityZonesResponse.getBody() != null) {
                List<CoZoneResponseDto> communityZones = communityZonesResponse.getBody();

                log.info("Fetched {} communities from Driver Service. Processing boundaries...", communityZones.size());

                // 2. Loop through each community zone
                for (CoZoneResponseDto zoneResponseDto:communityZones) {
                    Integer communityId = zoneResponseDto.getZoneId();

                    String communityName = zoneResponseDto.getZoneName();
                    JsonNode boundary =  zoneResponseDto.getBoundary();

                    // Convert the internal linked boundary map cleanly back into a JSON String for PostGIS
                    String geoJsonStr = objectMapper.writeValueAsString(boundary);

                    // 3. Run the batch location assignment query
                    customerCommunityRepository.linkCustomersToCommunity(communityId, geoJsonStr);

                }

                log.info("2 AM Community Mapping Scheduler job completed successfully.");
            } else {
                log.error("Failed to fetch zones from Driver Service. Status code: {}", communityZonesResponse.getStatusCode());
            }
        }catch (Exception e){
            log.error("Critical error encountered in 2 AM Community Mapping Scheduler: {} ", e.getMessage());
        }




    }
}
