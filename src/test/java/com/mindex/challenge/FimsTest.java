package com.mindex.challenge;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@SpringBootTest
public class FimsTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private record Result(String url, String lat, String lon) {
    }

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

        StringBuilder builder = new StringBuilder("PROP_SEQ_NO,LAT,LON,API_URL\n");

        Map<String, Result> resultMap = new HashMap<>(5000);

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

                    String resultMapKey = street + ":" + city + ":" + state + ":" + zipCode;

                    if (resultMap.containsKey(resultMapKey)) {

                        Result result = resultMap.get(resultMapKey);

                        builder.append(propSeqNo);
                        builder.append(",");
                        builder.append(result.lat);
                        builder.append(",");
                        builder.append(result.lon);
                        builder.append(",");
                        builder.append(result.url);
                        builder.append("\n");

                        return;
                    }

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

                    boolean triedZipCodeOnly = false;

                    if (streetExists && zipCodeExists && stateExists) {
                        if (!street.startsWith(zipCode)) {
                            url = String.format(url + "&street=%s&postalcode=%s&state=%s", street, zipCode, state);
                        } else {
                            url = String.format(url + "&postalcode=%s", zipCode);
                            triedZipCodeOnly = true;
                        }
                    } else if (streetExists && stateExists) {
                        url = String.format(url + "&street=%s&state=%s", street, state);
                    } else if (zipCodeExists) {
                        url = String.format(url + "&postalcode=%s", zipCode);
                        triedZipCodeOnly = true;
                    } else {
                        url = String.format(url + "&state=%s&city=%s", state, city);
                    }

                    List<?> results = restTemplate.getForObject(url, List.class);

                    if (CollectionUtils.isEmpty(results) && (zipCodeExists || (cityExists && stateExists))) {
                        url = "https://nominatim.openstreetmap.org/search?format=json&country=US";
                        if (!triedZipCodeOnly && zipCodeExists) {
                            url = String.format(url + "&postalcode=%s", zipCode);
                            results = restTemplate.getForObject(url, List.class);
                        }
                        if (CollectionUtils.isEmpty(results) && cityExists && stateExists) {
                            url = String.format(url + "&state=%s&city=%s", state, city);
                            results = restTemplate.getForObject(url, List.class);
                        }
                    }

                    if (!CollectionUtils.isEmpty(results)) {

                        Map<?, ?> map = (Map<?, ?>) results.get(0);

                        String lat = (String) map.get("lat");
                        String lon = (String) map.get("lon");

                        builder.append(propSeqNo);
                        builder.append(",");
                        builder.append(lat);
                        builder.append(",");
                        builder.append(lon);
                        builder.append(",");
                        builder.append(url);
                        builder.append("\n");

                        resultMap.put(resultMapKey, new Result(url, lat, lon));
                    }

                }

            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }

        });

        Files.write(Paths.get("/home/mike/tmp/coord_7.csv"),
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

    @Test
    public void logBadProperties() throws Exception {

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
                            prop_geo_st = geol_state_code (+) and
                            prop_seq_no = :propId and
                            prop_zip is not null
                       order by prop_seq_no
                """;

        CSVReader reader = new CSVReader(new FileReader("/home/mike/tmp/coord_3.csv"));

        List<String[]> rows = reader.readAll();

        Set<Integer> idSet = new HashSet<>(rows.size());

        for (int i = 1; i < rows.size(); i++) {
            String idValue = rows.get(i)[0];
            if (StringUtils.isNotBlank(idValue)) {
                idSet.add(Integer.parseInt(idValue.trim()));
            }
        }

        Set<Integer> propIdSet = new HashSet<>(rows.size());

        jdbcTemplate.query("select prop_seq_no from fims_tbl_property", rs -> {
            propIdSet.add(rs.getInt(1));
        });

        propIdSet.removeAll(idSet);

        for (Integer id : propIdSet) {

            namedParameterJdbcTemplate.query(sql, Collections.singletonMap("propId", id), rs -> {

                long propSeqNo = rs.getLong(1);
                String street = format(rs.getString(2));
                String zipCode = format(rs.getString(3));
                String state = format(rs.getString(4));
                String city = format(rs.getString(5));
                String county = format(rs.getString(6));

                log.info(propSeqNo + ", street: " + street + ", zip: " + zipCode + ", state: " + state + ", city: " + city + ", county: " + county);
            });
        }

        reader.close();
    }

    private List<Set<Integer>> partition(Set<Integer> set, int chunk) {
        if (set == null || set.isEmpty() || chunk < 1) {
            return new ArrayList<>();
        }
        List<Set<Integer>> partitionedList = new ArrayList<>();
        double loopSize = Math.ceil((double) set.size() / (double) chunk);
        for (int i = 0; i < loopSize; i++) {
            partitionedList.add(set.stream().skip((long) i * chunk).limit(chunk).collect(Collectors.toSet()));
        }
        return partitionedList;
    }

}