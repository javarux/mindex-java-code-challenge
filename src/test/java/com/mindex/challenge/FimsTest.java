
package com.mindex.challenge;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class FimsTest {

    private static final Object NOMINATIM_LOCK = new Object();
    private static long lastNominatimRequest = 0;

    private static final String NOMINATIM_USER_AGENT = "FimsPropertyGeocoder/1.0 (javarux@yahoo.com)";

    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/search";


    private final static List<Long> propSeqNumbers = Arrays.asList(
            225921L, 226064L, 226127L, 226128L, 226089L, 226007L, 226008L, 225638L, 225639L, 225902L,
            225901L, 225640L, 225781L, 225629L, 225627L, 225626L, 225634L, 225635L, 225636L, 225637L,
            225383L, 226133L, 226132L, 225539L, 225895L, 225974L, 225382L, 226054L, 226174L, 225441L,
            226055L, 225923L, 225420L, 225421L, 225719L, 225708L, 225709L, 225422L, 225704L, 225706L,
            225940L, 225941L, 225942L, 225943L, 225944L, 225945L, 225707L, 226130L, 226131L, 225705L,
            226068L, 226072L, 226080L, 226075L, 226081L, 226074L, 226069L, 226078L, 226076L, 226079L,
            226077L, 226073L, 226070L, 226071L, 225542L, 225543L, 225389L, 225462L, 225461L, 225393L,
            225819L, 225561L, 225541L, 226173L, 225390L, 225820L, 225821L, 225391L, 225400L, 225822L,
            225394L, 225546L, 225866L, 225506L, 226171L, 225388L, 225880L, 225440L, 226134L, 225547L,
            226172L, 225425L, 225621L, 225622L, 225891L, 225886L, 225743L, 225899L, 225699L, 225700L,
            225887L, 225630L, 225599L, 225701L, 225702L, 225703L, 225399L, 225802L, 225780L, 225742L,
            225623L, 225641L, 225642L, 225643L, 225644L, 225645L, 225646L, 226169L, 226170L, 225973L,
            225968L, 225424L, 225960L, 225892L, 225404L, 225624L, 225600L, 225419L, 226176L, 225893L,
            225403L, 225888L, 225889L, 225890L, 225406L, 226010L, 225865L, 225870L, 225894L, 225869L,
            225883L, 225867L, 225882L, 225903L, 225365L, 225958L, 225954L, 225955L, 225956L, 225957L,
            225900L, 225407L, 226011L, 225959L, 226049L, 226050L, 226066L, 226067L, 225740L, 225868L,
            225631L, 225739L, 225741L, 226051L, 226052L, 225871L, 225864L, 225392L, 225884L, 225885L,
            225863L, 225398L, 225395L, 225396L, 225397L, 225841L, 225827L, 225540L, 226059L, 226060L,
            226061L, 226062L, 226063L, 225437L, 225619L, 225438L, 226107L, 226108L, 226147L, 226155L,
            225799L, 225433L, 225434L, 225435L, 225436L, 225387L, 226109L, 225800L, 225801L, 225385L,
            225386L, 225384L, 225423L, 225504L, 225505L, 225920L, 225503L, 226175L, 225679L, 225519L,
            225502L, 225782L, 225783L, 225521L, 225520L, 225459L, 225431L, 226168L, 225759L, 225432L,
            225579L, 225463L, 225987L, 225977L, 225779L, 226009L, 225460L, 225428L, 225559L, 225922L,
            225786L, 225784L, 225785L, 225661L, 225662L, 225660L, 225948L, 225949L
    );

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private record Result(String url, String lat, String lon) {
    }

    /*
     * All Nominatim requests must go through this method.
     *
     * Synchronization ensures that requests from this instance
     * are serialized and separated by at least 15 seconds.
     */
    private List<?> nominatimGet(String url) {

        synchronized (NOMINATIM_LOCK) {
            try {
                long now = System.currentTimeMillis();
                long elapsed = now - lastNominatimRequest;

                if (elapsed < 1100) {
                    Thread.sleep(1100 - elapsed);
                }

                HttpHeaders headers = new HttpHeaders();

                // Replace with your actual contact email.
                headers.set(HttpHeaders.USER_AGENT, NOMINATIM_USER_AGENT);

                headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

                URI uri = UriComponentsBuilder
                        .fromUriString(url)
                        .build()
                        .encode()
                        .toUri();

                log.info("Nominatim request: {}", uri);

                ResponseEntity<List> response = restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        List.class
                );

                lastNominatimRequest = System.currentTimeMillis();

                return response.getBody();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Nominatim request interrupted", e);
            }
        }
    }

    /*
     * Creates a URL with properly encoded query parameters.
     */
    private String nominatimUrl(String... parameters) {

        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(NOMINATIM_BASE_URL)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .queryParam("addressdetails", 1)
                        .queryParam("country", "US");

        for (int i = 0; i < parameters.length; i += 2) {
            builder.queryParam(parameters[i], parameters[i + 1]);
        }

        return builder.encode().toUriString();
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
                 where prop_geo_city = geol_city_code (+)
                   and prop_geo_county = geol_county_code (+)
                   and prop_geo_st = geol_state_code (+)
                   and prop_seq_no in (:ids)
                 order by prop_seq_no
                """;

        StringBuilder sqlBuilder = new StringBuilder();

        StringBuilder builder =
                new StringBuilder("PROP_SEQ_NO,LAT,LON,API_URL\n");

        // Cache successful geocoding results.
        Map<String, Result> resultMap = new HashMap<>(5000);

        // Cache unsuccessful addresses too.
        Set<String> failedAddresses = new HashSet<>();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", propSeqNumbers);

        namedParameterJdbcTemplate.query(sql, params, rs -> {

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

                if (!(streetExists ||
                        zipCodeExists ||
                        (stateExists && cityExists))) {
                    return;
                }

                log.info(
                        "{}, street: {}, zip: {}, state: {}, city: {}, county: {}",
                        propSeqNo,
                        street,
                        zipCode,
                        state,
                        city,
                        county
                );

                String resultMapKey =
                        street + ":" + city + ":" + state + ":" + zipCode;

                // Use cached successful result.
                Result cachedResult = resultMap.get(resultMapKey);

                if (cachedResult != null) {

                    appendResult(
                            propSeqNo,
                            cachedResult,
                            builder,
                            sqlBuilder
                    );

                    return;
                }

                // Skip addresses that previously returned no results.
                if (failedAddresses.contains(resultMapKey)) {
                    log.info("Skipping previously unsuccessful address: {}",
                            resultMapKey);
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

                String url;
                List<?> results;

                boolean triedZipCodeOnly = false;

                /*
                 * First search.
                 */
                if (streetExists && zipCodeExists && stateExists) {

                    if (!street.startsWith(zipCode)) {

                        url = nominatimUrl(
                                "street", street,
                                "postalcode", zipCode,
                                "state", state
                        );

                    } else {

                        url = nominatimUrl(
                                "postalcode", zipCode
                        );

                        triedZipCodeOnly = true;
                    }

                } else if (streetExists && stateExists) {

                    url = nominatimUrl(
                            "street", street,
                            "state", state
                    );

                } else if (zipCodeExists) {

                    url = nominatimUrl(
                            "postalcode", zipCode
                    );

                    triedZipCodeOnly = true;

                } else {

                    url = nominatimUrl(
                            "state", state,
                            "city", city
                    );
                }

                results = nominatimGet(url);

                /*
                 * Fallback searches.
                 */
                if (CollectionUtils.isEmpty(results) &&
                        (zipCodeExists || (cityExists && stateExists))) {

                    // Try ZIP code only.
                    if (!triedZipCodeOnly && zipCodeExists) {

                        url = nominatimUrl(
                                "postalcode", zipCode
                        );

                        results = nominatimGet(url);
                    }

                    // Try city and state.
                    if (CollectionUtils.isEmpty(results) &&
                            cityExists && stateExists) {

                        url = nominatimUrl(
                                "state", state,
                                "city", city
                        );

                        results = nominatimGet(url);
                    }
                }

                /*
                 * Validate the returned ZIP code and state.
                 */
                if (!CollectionUtils.isEmpty(results)) {

                    Map<?, ?> map = (Map<?, ?>) results.get(0);

                    Map<?, ?> address =
                            (Map<?, ?>) map.get("address");

                    String postcode = address != null
                            ? (String) address.get("postcode")
                            : null;

                    String isoState = address != null
                            ? (String) address.get("ISO3166-2-lvl4")
                            : null;

                    if (zipCodeExists &&
                            ((StringUtils.isNotBlank(postcode) &&
                                    !zipCode.equals(postcode)) ||
                                    (StringUtils.isNotBlank(isoState) &&
                                            !isoState.endsWith(state)))) {

                        log.info(
                                "ZIP/state mismatch for property {}. Trying ZIP-only lookup.",
                                propSeqNo
                        );

                        url = nominatimUrl(
                                "postalcode", zipCode
                        );

                        results = nominatimGet(url);

                        map = !CollectionUtils.isEmpty(results)
                                ? (Map<?, ?>) results.get(0)
                                : null;

                    }

                    if (map != null) {

                        String lat = (String) map.get("lat");
                        String lon = (String) map.get("lon");

                        if (StringUtils.isNotBlank(lat) &&
                                StringUtils.isNotBlank(lon)) {

                            Result result =
                                    new Result(url, lat, lon);

                            resultMap.put(resultMapKey, result);

                            appendResult(
                                    propSeqNo,
                                    result,
                                    builder,
                                    sqlBuilder
                            );

                            log.info(
                                    "Coordinates found for property {}: {}, {}",
                                    propSeqNo,
                                    lat,
                                    lon
                            );

                            return;
                        }
                    }
                }

                // Remember unsuccessful searches.
                failedAddresses.add(resultMapKey);

                log.warn(
                        "No coordinates found for property {}: {}",
                        propSeqNo,
                        resultMapKey
                );

            } catch (HttpClientErrorException.Forbidden e) {

                /*
                 * Do not continue making requests after a 403.
                 * Abort the batch.
                 */
                log.error(
                        "Nominatim returned HTTP 403. Stopping geocoding.",
                        e
                );

                throw e;

            } catch (Exception e) {

                log.error(
                        "Error processing property {}",
                        rs.getLong(1),
                        e
                );
            }
        });

        /*
         * Write generated SQL and CSV files.
         */
        Files.write(
                Paths.get("/home/mike/tmp/coord_2026_1.sql"),
                (sqlBuilder + "\n").getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        Files.write(
                Paths.get("/home/mike/tmp/coord_2026_1.csv"),
                (builder + "\n").getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        log.info("Successful cached addresses: {}", resultMap.size());
        log.info("Unsuccessful addresses: {}", failedAddresses.size());
    }

    /*
     * Appends a coordinate result to the CSV and SQL output.
     */
    private void appendResult(
            long propSeqNo,
            Result result,
            StringBuilder builder,
            StringBuilder sqlBuilder) {

        builder.append(propSeqNo)
                .append(",")
                .append(result.lat())
                .append(",")
                .append(result.lon())
                .append(",")
                .append(result.url())
                .append("\n");

        sqlBuilder.append(
                        "update fims_tbl_property set prop_lat = "
                )
                .append(result.lat())
                .append(", prop_long = ")
                .append(result.lon())
                .append(" where prop_seq_no = ")
                .append(propSeqNo)
                .append(";\n");
    }

    private static String format(String value) {

        return (value != null)
                ? StringUtils.normalizeSpace(value.trim())
                : "";
    }

    private boolean isNotBlank(String value) {

        return StringUtils.isNotBlank(value) &&
                !"null".equalsIgnoreCase(value.trim());
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
                 where prop_geo_city = geol_city_code (+)
                   and prop_geo_county = geol_county_code (+)
                   and prop_geo_st = geol_state_code (+)
                   and prop_seq_no = :propId
                   and prop_zip is not null
                 order by prop_seq_no
                """;

        try (CSVReader reader =
                     new CSVReader(
                             new FileReader("/home/mike/tmp/coord_3.csv")
                     )) {

            List<String[]> rows = reader.readAll();

            Set<Integer> idSet = new HashSet<>(rows.size());

            for (int i = 1; i < rows.size(); i++) {

                String idValue = rows.get(i)[0];

                if (StringUtils.isNotBlank(idValue)) {
                    idSet.add(Integer.parseInt(idValue.trim()));
                }
            }

            Set<Integer> propIdSet = new HashSet<>(rows.size());

            namedParameterJdbcTemplate.query(
                    "SELECT prop_seq_no FROM fims_tbl_property",
                    rs -> {
                        while (rs.next()) {
                            propIdSet.add(rs.getInt("prop_seq_no"));
                        }
                    }
            );

            propIdSet.removeAll(idSet);

            for (Integer id : propIdSet) {

                namedParameterJdbcTemplate.query(
                        sql,
                        Collections.singletonMap("propId", id),
                        rs -> {

                            long propSeqNo = rs.getLong(1);

                            String street = format(rs.getString(2));
                            String zipCode = format(rs.getString(3));
                            String state = format(rs.getString(4));
                            String city = format(rs.getString(5));
                            String county = format(rs.getString(6));

                            log.info(
                                    "{}, street: {}, zip: {}, state: {}, city: {}, county: {}",
                                    propSeqNo,
                                    street,
                                    zipCode,
                                    state,
                                    city,
                                    county
                            );
                        }
                );
            }
        }
    }

    private List<Set<Integer>> partition(Set<Integer> set, int chunk) {

        if (set == null || set.isEmpty() || chunk < 1) {
            return new ArrayList<>();
        }

        List<Set<Integer>> partitionedList = new ArrayList<>();

        double loopSize =
                Math.ceil((double) set.size() / (double) chunk);

        for (int i = 0; i < loopSize; i++) {

            partitionedList.add(
                    set.stream()
                            .skip((long) i * chunk)
                            .limit(chunk)
                            .collect(Collectors.toSet())
            );
        }

        return partitionedList;
    }
}
