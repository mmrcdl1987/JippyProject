    package com.jippy.foodandmart.repository;

    import com.jippy.foodandmart.entity.FmSpecializedOutlet;
    import com.jippy.foodandmart.projections.FmOutletProjection;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;

    @Repository
    public interface FmSpecializedOutletRepository
            extends JpaRepository<FmSpecializedOutlet, Integer> {

        @Query(value = """
SELECT DISTINCT
    o.outlet_id AS outletId,
    o.outlet_name AS outletName
FROM jippy_fm.specialized_outlets so
JOIN jippy_fm.outlets o
    ON so.outlet_id = o.outlet_id
WHERE so.area_id = :areaId
AND o.is_active = 'Y'
""", nativeQuery = true)
        List<FmOutletProjection>
        fetchSpecializedOutletsByAreaId(
                @Param("areaId") Integer areaId
        );
    }