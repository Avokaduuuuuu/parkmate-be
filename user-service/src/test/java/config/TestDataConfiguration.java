package config;

import com.parkmate.mobileDevice.MobileDevice;
import com.parkmate.mobileDevice.DeviceOs;
import com.parkmate.mobileDevice.MobileDeviceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@TestConfiguration
public class TestDataConfiguration {

    @Bean
    @Profile("performance-test")
    public CommandLineRunner createTestData(MobileDeviceRepository repository) {
        return args -> {
            if (repository.count() < 10000) {
                createMobileDeviceTestData(repository);
            }
        };
    }

    private void createMobileDeviceTestData(MobileDeviceRepository repository) {
        List<MobileDevice> devices = new ArrayList<>();

        for (int i = 0; i < 10000; i++) {
            MobileDevice device = new MobileDevice();
            device.setDeviceId("device_" + String.format("%05d", i));
            device.setDeviceName("Model_" + (i % 50)); // 50 different models
            device.setDeviceOs(DeviceOs.ANDROID); // Android 10-14
            device.setUser(null);

            devices.add(device);

            // Batch insert mỗi 1000 records
            if (i % 1000 == 0) {
                repository.saveAll(devices);
                devices.clear();
            }
        }

        if (!devices.isEmpty()) {
            repository.saveAll(devices);
        }

        System.out.println("Created 10,000 test mobile devices");
    }
}
