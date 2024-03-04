package com.mindex.challenge;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
public class FimsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void getCoordinates() throws Exception {

        String sql = """
                     select prop_seq_no,
                            prop_main_loc,
                            prop_zip,
                            geol_state_abbrev,
                            geol_city_name,
                            geol_county_name
                       from fims_tbl_property,
                            fims_tbl_lu_geo_location
                       where
                            prop_geo_city = geol_city_code (+) and
                            prop_geo_county = geol_county_code (+) and
                            prop_geo_st = geol_state_code (+)
                       order by prop_seq_no
                """;

        StringBuilder builder = new StringBuilder("PROP_SEQ_NO,LAT,LON,BOUNDING_BOX\n");

        jdbcTemplate.query(sql, rs -> {

            try {

                long propSeqNo = rs.getLong(1);
                String street = format(rs.getString(2));
                String zipCode = format(rs.getString(3));
                String state = format(rs.getString(4));
                String city = format(rs.getString(5));
                String county = format(rs.getString(6));

                boolean zipCodeExists = isNotBlank(zipCode);
                boolean streetExists = isNotBlank(street);
                boolean stateExists = isNotBlank(state);
                boolean cityExists = isNotBlank(city);

                if (streetExists || zipCodeExists || (stateExists && cityExists)) {

                    log.info(propSeqNo + ", street: " + street + ", zip: " + zipCode + ", state: " + state + ", city: " + city + ", county: " + county);

                    if (streetExists) {
                        street = street.replace("#", "");
                    }

                    if (zipCodeExists) {
                        zipCode = zipCode.trim();
                        if (zipCode.length() > 5) {
                            zipCode = zipCode.substring(0, 5);
                        }
                    }

                    String url = "https://nominatim.openstreetmap.org/search?format=json&country=US";

                    if (streetExists && zipCodeExists) {
                        url = !street.startsWith(zipCode) ? String.format(url + "&street=%s&postalcode=%s", street, zipCode) : String.format(url + "&postalcode=%s", zipCode);
                    } else if (streetExists) {
                        url = String.format(url + "&street=%s", street);
                    } else if (zipCodeExists) {
                        url = String.format(url + "&postalcode=%s", zipCode);
                    } else {
                        url = String.format(url + "&state=%s&city=%s", state, city);
                    }

                    List<?> list = restTemplate.getForObject(url, List.class);

                    assertNotNull(list);

                    if (!list.isEmpty()) {

                        Map<?, ?> map = (Map<?, ?>) list.get(0);

                        builder.append(propSeqNo);
                        builder.append(",");
                        builder.append(map.get("lat"));
                        builder.append(",");
                        builder.append(map.get("lon"));
                        builder.append(",\"");
                        builder.append(map.get("boundingbox"));
                        builder.append("\"\n");
                    }

                }

            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }

        });

        Files.write(Paths.get("/home/mike/tmp/coord_2.csv"),
                (builder + "\n").getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

    }

    private static String format(String value) {
        return (value != null) ? StringUtils.normalizeSpace(value.trim()) : "";
    }

    private boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value) && !"null".equalsIgnoreCase(value.trim());
    }

}