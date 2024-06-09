package com.mindex.challenge;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@SpringBootTest
public class JobTest {

    @Data
    private static class Job {
        private String id;
        private Set<Job> dependentJobs;

        public Job(String id) {
            this.id = id;
        }
    }

    private void populateCountMap(Collection<Job> jobs, Map<String, Integer> countMap) {
        for (Job job : jobs) {
            countMap.putIfAbsent(job.getId(), 0);
            Set<Job> dependentJobs = job.getDependentJobs();
            if (CollectionUtils.isNotEmpty(dependentJobs)) {
                for (Job dependentJob : dependentJobs) {
                    countMap.put(dependentJob.getId(), countMap.computeIfAbsent(dependentJob.getId(), v -> 0) + 1);
                }
                populateCountMap(dependentJobs, countMap);
            }
        }
    }

    private Map<String, Integer> populateCountMap(Collection<Job> jobs) {
        Map<String, Integer> countMap = new HashMap<>();
        populateCountMap(jobs, countMap);
        return countMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    @Test
    public void test() {

        Job job1 = new Job("job1");
        Job job2 = new Job("job2");
        Job job3 = new Job("job3");
        Job job4 = new Job("job4");
        Job job5 = new Job("job5");
        Job job6 = new Job("job6");

        job4.setDependentJobs(Set.of(job6));
        job3.setDependentJobs(Set.of(job5, job2));
        job2.setDependentJobs(Set.of(job6, job5));
        job1.setDependentJobs(Set.of(job2, job3));

        // job2 -> job5
        // job1 -> job2 -> job5
        // job1 -> job3 -> job5
        // job1 -> job3 -> job2 -> job5
        // job3 -> job5
        // job3 -> job2 -> job5

        Map<String, Integer> countMap = populateCountMap(Arrays.asList(job1, job2, job3, job4, job5, job6));

        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            log.info(entry.getKey() + " : " + entry.getValue());
        }
    }
}
