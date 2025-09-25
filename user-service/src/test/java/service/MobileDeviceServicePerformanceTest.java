package service;

import com.parkmate.UserApplication;
import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
import com.parkmate.account.Account;
import com.parkmate.mobileDevice.MobileDevice;
import com.parkmate.user.User;
import com.parkmate.common.enums.AccountRole;
import com.parkmate.common.enums.AccountStatus;
import com.parkmate.mobileDevice.DeviceOs;
import com.parkmate.repository.AccountRepository;
import com.parkmate.mobileDevice.MobileDeviceRepository;
import com.parkmate.user.UserRepository;
import com.parkmate.mobileDevice.MobileDeviceService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(classes = UserApplication.class)
@ActiveProfiles("performance-test")
@Transactional
public class MobileDeviceServicePerformanceTest {

    @Autowired
    private MobileDeviceService service;

    @Autowired
    private MobileDeviceRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setupTestData() {
        // Xóa data cũ
        repository.deleteAll();
        userRepository.deleteAll();
        accountRepository.deleteAll();

        // Tạo test data
        createTestData();

        // Verify
        long count = repository.count();
        System.out.println("=== Test Setup Complete ===");
        System.out.println("Mobile devices created: " + count);
        assertEquals(10000, count, "Should have exactly 10,000 test records");
    }

    @Test
    public void debugAccountSave() {
        System.out.println("=== Starting Account Debug Test ===");

        try {
            Account account = new Account();
            account.setEmail("debug@test.com");
            account.setUsername("debuguser");
            account.setPassword("password123");
            account.setRole(AccountRole.ADMIN);  // Try ADMIN first
            account.setStatus(AccountStatus.ACTIVE);
            account.setEmailVerified(true);
            account.setPhoneVerified(true);

            System.out.println("Account created with role: " + account.getRole());
            System.out.println("Account created with status: " + account.getStatus());

            Account saved = accountRepository.saveAndFlush(account);
            System.out.println("SUCCESS! Account saved with ID: " + saved.getId());

        } catch (Exception e) {
            System.out.println("=== EXCEPTION DETAILS ===");
            System.out.println("Exception: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());

            Throwable cause = e.getCause();
            while (cause != null) {
                System.out.println("Caused by: " + cause.getClass().getSimpleName());
                System.out.println("Cause message: " + cause.getMessage());
                cause = cause.getCause();
            }

            e.printStackTrace();
            throw e; // Re-throw to fail the test
        }
    }

    private void createTestData() {
        // Tạo users với accounts
        List<User> users = createTestUsers(100); // 100 users
        userRepository.saveAll(users); // Cascade sẽ save accounts

        // Tạo mobile devices
        List<MobileDevice> devices = new ArrayList<>();
        DeviceOs[] osValues = DeviceOs.values();

        for (int i = 0; i < 1000000; i++) {
            User randomUser = users.get(i % users.size());

            MobileDevice device = MobileDevice.builder()
                    .deviceId("device_" + String.format("%05d", i))
                    .deviceName("Test Device " + i)
                    .deviceOs(osValues[i % osValues.length])
                    .pushToken("push_token_" + i)
                    .isActive(i % 10 != 0) // 90% active devices
                    .lastActiveAt(LocalDateTime.now().minusHours(i % 168))
                    .user(randomUser)
                    .build();

            devices.add(device);

            // Batch insert
            if ((i + 1) % 1000 == 0) {
                repository.saveAll(devices);
                devices.clear();

                if ((i + 1) % 5000 == 0) {
                    System.out.println("Created " + (i + 1) + " devices...");
                }
            }
        }

        if (!devices.isEmpty()) {
            repository.saveAll(devices);
        }
    }

    private List<User> createTestUsers(int count) {
        List<Account> accounts = new ArrayList<>();
        List<User> users = new ArrayList<>();

        // Tạo tất cả accounts trước
        for (int i = 0; i < count; i++) {
            Account account = new Account();
            account.setEmail("testuser" + i + "@example.com");
            account.setUsername("testuser" + i);
            account.setPassword("$2a$10$encrypted_password");
            account.setRole(AccountRole.ADMIN);
            account.setStatus(AccountStatus.ACTIVE);
            account.setEmailVerified(true);
            account.setPhoneVerified(true);

            accounts.add(account);
        }

        // Save tất cả accounts
        accountRepository.saveAll(accounts);

        // Tạo users với accounts đã được saved
        for (int i = 0; i < count; i++) {
            Account savedAccount = accounts.get(i);

            User user = User.builder()
                    .phone("090" + String.format("%07d", i))
                    .firstName("User" + i)
                    .lastName("Test")
                    .account(savedAccount)
                    .build();

            // Set bidirectional relationship
            savedAccount.setUser(user);

            users.add(user);
        }

        return users;
    }

    @Test
    public void testSearchPerformanceWith10kRecords() {
        MobileDeviceSearchCriteria criteria = new MobileDeviceSearchCriteria();

        // Test 1: Search all
        long startTime = System.nanoTime();
        List<MobileDeviceResponse> allResults = service.searchDevices(criteria);
        long duration1 = System.nanoTime() - startTime;

        System.out.println("=== Search All Results ===");
        System.out.println("Time: " + duration1 / 1_000_000 + " ms");
        System.out.println("Records: " + allResults.size());

        // Test 2: Search with pagination
        Pageable pageable = PageRequest.of(0, 100);
        startTime = System.nanoTime();
        Page<MobileDeviceResponse> pagedResults = service.searchDevices(criteria, pageable);
        long duration2 = System.nanoTime() - startTime;

        System.out.println("=== Search With Pagination (100 records) ===");
        System.out.println("Time: " + duration2 / 1_000_000 + " ms");
        System.out.println("Records: " + pagedResults.getContent().size());
        System.out.println("Total pages: " + pagedResults.getTotalPages());

        // Test 3: Search with filter
        criteria.setDeviceOs(DeviceOs.ANDROID); // Assuming you have this filter
        startTime = System.nanoTime();
        List<MobileDeviceResponse> filteredResults = service.searchDevices(criteria);
        long duration3 = System.nanoTime() - startTime;

        System.out.println("=== Search With Filter (Android only) ===");
        System.out.println("Time: " + duration3 / 1_000_000 + " ms");
        System.out.println("Records: " + filteredResults.size());

        // Assertions
        assertTrue("Should return close to 10k records", allResults.size() >= 9900);
        assertTrue("Paged search should return max 100 records", pagedResults.getContent().size() <= 100);
        assertTrue("Filtered search should return fewer records", filteredResults.size() < allResults.size());
        assertTrue("Paged search should be faster than full search", duration2 < duration1);
    }

    @Test
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        System.gc();

        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

        MobileDeviceSearchCriteria criteria = new MobileDeviceSearchCriteria();
        List<MobileDeviceResponse> results = service.searchDevices(criteria);

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;

        System.out.println("=== Memory Usage Test ===");
        System.out.println("Records: " + results.size());
        System.out.println("Memory used: " + memoryUsed / (1024 * 1024) + " MB");
        System.out.println("Memory per record: " + (results.isEmpty() ? 0 : memoryUsed / results.size()) + " bytes");

        // Warning if memory usage is too high
        if (memoryUsed / (1024 * 1024) > 100) {
            System.out.println("WARNING: High memory usage detected (>100MB)");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 50, 100, 500, 1000})
    public void testDifferentPageSizes(int pageSize) {
        MobileDeviceSearchCriteria criteria = new MobileDeviceSearchCriteria();
        Pageable pageable = PageRequest.of(0, pageSize);

        long startTime = System.nanoTime();
        Page<MobileDeviceResponse> results = service.searchDevices(criteria, pageable);
        long duration = System.nanoTime() - startTime;

        System.out.println("Page size: " + pageSize +
                " | Time: " + duration / 1_000_000 + " ms" +
                " | Records: " + results.getContent().size());

        assertTrue("Response time should be < 1 second", duration < 1_000_000_000);
        assertTrue("Should not exceed page size", results.getContent().size() <= pageSize);
    }
}