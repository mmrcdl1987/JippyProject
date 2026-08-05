package com.jippy.customerandorder.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.customerandorder.entity.CoCommunity;
import com.jippy.customerandorder.repository.CoCommunityRepository;
import com.jippy.customerandorder.repository.CoCustomerCommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Polygon;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerMappingToCommunitySchedular {

    private final ObjectMapper objectMapper;
    private final CoCustomerCommunityRepository customerCommunityRepository;
    private final CoCommunityRepository communityRepository;

    // Cron syntax: Second, Minute, Hour, Day of Month, Month, Day of Week
    @Scheduled(cron = "0 0 2 * * ?")
   // @Scheduled(cron = "0 */2 * * * ?")
    public void runCommunityMapping() {

        log.info("Starting 2 AM Community Mapping Scheduler Job...");

        try{
            List<CoCommunity> communityList = communityRepository.findAll();
            log.info("Fetched {} communities from the database.", communityList.size());

            // 2. Loop through each community zone
            for (CoCommunity community:communityList) {
                Integer communityId = community.getCommunityId();

                String communityName = community.getCommunityName();
                Polygon polygon =  community.getBoundary();

                // Convert the internal linked boundary map cleanly back into a JSON String for PostGIS
                String geoJsonStr = objectMapper.writeValueAsString(polygon);

                // 3. Run the batch location assignment query
                customerCommunityRepository.linkCustomersToCommunity(communityId, geoJsonStr);

            }

            log.info("2 AM Community Mapping Scheduler job completed successfully.");

        }catch (Exception e){
            log.error("Critical error encountered in 2 AM Community Mapping Scheduler: {} ", e.getMessage());
        }

    }
}
