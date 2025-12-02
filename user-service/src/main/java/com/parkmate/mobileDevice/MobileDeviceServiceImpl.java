package com.parkmate.mobileDevice;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.common.util.PaginationUtil;
import com.parkmate.mobileDevice.dto.CreateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.UpdateMobileDeviceRequest;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MobileDeviceServiceImpl implements MobileDeviceService {

    private final MobileDeviceMapper mobileDeviceMapper;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final UserRepository userRepository;

    @Override
    public Page<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria, int page, int size, String sortBy, String sortOrder) {
        Page<MobileDevice> devicePage = mobileDeviceRepository.findAll(
                MobileDeviceSpecification.buildPredicate(criteria),
                PaginationUtil.parsePageable(page, size, sortBy, sortOrder));
        return devicePage.map(mobileDeviceMapper::toDTO);
    }


    /*
     * Use cases xử lý:
     *   1. ✅ User đăng ký lần đầu → Tạo device mới
     *   2. ✅ User đăng ký lại với cùng deviceId + pushToken → Update thông tin device
     *   3. ✅ User logout rồi login lại (cùng deviceId) → Kích hoạt lại device cũ
     *   4. ✅ FCM token refresh (cùng deviceId, khác pushToken) → Update pushToken mới
     *   5. ✅ Token bị đổi chủ (reinstall app, đổi account) → Vô hiệu hóa device cũ, tạo mới
     *   6. ✅ DeviceId bị đổi chủ → Vô hiệu hóa device cũ của user khác
     * */
    @Override
    @Transactional
    public MobileDeviceResponse createMobileDevice(CreateMobileDeviceRequest request, String userIdHeader) {
        if (Boolean.TRUE.equals(request.getOwnedByMe()) && userIdHeader != null) {
            request.setUserId(Long.parseLong(userIdHeader));
        }

        if (request.getUserId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "User ID is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.getUserId()));

        // Case 1: Check deviceId đã tồn tại chưa (bất kể user nào)
        Optional<MobileDevice> existingByDeviceId = mobileDeviceRepository.findByDeviceId(request.getDeviceId());

        if (existingByDeviceId.isPresent()) {
            MobileDevice device = existingByDeviceId.orElseThrow();

            // Case 1a: DeviceId thuộc về user hiện tại → Update thông tin
            if (device.getUser().getId().equals(request.getUserId())) {
                // Kích hoạt lại nếu bị vô hiệu hóa
                if (Boolean.FALSE.equals(device.getIsActive())) {
                    device.setIsActive(true);
                }

                // Update thông tin mới (FCM token có thể đã refresh)
                device.setPushToken(request.getPushToken());
                device.setDeviceName(request.getDeviceName());
                device.setDeviceOs(request.getDeviceOs());
                device.setLastActiveAt(LocalDateTime.now());

                MobileDevice updatedDevice = mobileDeviceRepository.save(device);
                return mobileDeviceMapper.toDTO(updatedDevice);
            }
            // Case 1b: DeviceId thuộc về user khác → Chuyển quyền sở hữu
            else {
                // Vô hiệu hóa và chuyển sang user mới
                device.setUser(user);
                device.setIsActive(true);
                device.setPushToken(request.getPushToken());
                device.setDeviceName(request.getDeviceName());
                device.setDeviceOs(request.getDeviceOs());
                device.setLastActiveAt(LocalDateTime.now());

                MobileDevice updatedDevice = mobileDeviceRepository.save(device);
                return mobileDeviceMapper.toDTO(updatedDevice);
            }
        }

        // Case 2: Check pushToken có đang được user khác sử dụng không
        Optional<MobileDevice> tokenUsedByOthers = mobileDeviceRepository.findByPushToken(request.getPushToken());
        if (tokenUsedByOthers.isPresent()) {
            MobileDevice oldDevice = tokenUsedByOthers.orElseThrow();
            if (!oldDevice.getUser().getId().equals(request.getUserId())) {
                // PushToken đang được user khác sử dụng → vô hiệu hóa device cũ
                oldDevice.setIsActive(false);
                mobileDeviceRepository.save(oldDevice);
            }
        }

        // Case 3: Tạo device mới (deviceId và pushToken đều chưa tồn tại)
        MobileDevice mobileDevice = mobileDeviceMapper.toEntity(request);
        mobileDevice.setUser(user);
        mobileDevice.setLastActiveAt(LocalDateTime.now());
        mobileDevice.setIsActive(true);

        MobileDevice savedMobileDevice = mobileDeviceRepository.save(mobileDevice);
        return mobileDeviceMapper.toDTO(savedMobileDevice);
    }

    @Override
    public MobileDeviceResponse update(Long id, UpdateMobileDeviceRequest request) {

        MobileDevice mobileDevice = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, id));

        mobileDeviceMapper.updateEntityFromDTO(request, mobileDevice);
        MobileDevice savedMobileDevice = mobileDeviceRepository.save(mobileDevice);

        return mobileDeviceMapper.toDTO(savedMobileDevice);
    }

    @Override
    public MobileDeviceResponse findById(Long id) {
        MobileDevice mobileDevice = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, id));

        return mobileDeviceMapper.toDTO(mobileDevice);
    }

    @Override
    public void delete(Long id) {
        MobileDevice mobileDevice = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, id));
        mobileDevice.setIsActive(false);
        mobileDeviceRepository.save(mobileDevice);
    }
}
