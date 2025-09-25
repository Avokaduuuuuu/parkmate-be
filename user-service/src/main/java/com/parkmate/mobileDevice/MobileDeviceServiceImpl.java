package com.parkmate.mobileDevice;

import com.parkmate.mobileDevice.dto.MobileDeviceSearchCriteria;
import com.parkmate.mobileDevice.dto.CreateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.UpdateMobileDeviceRequest;
import com.parkmate.mobileDevice.dto.MobileDeviceResponse;
import com.parkmate.user.User;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.user.UserRepository;
import com.parkmate.common.util.PaginationUtil;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileDeviceServiceImpl implements MobileDeviceService {


    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "id", "deviceName", "deviceId", "deviceOs", "isActive", "lastActiveAt", "createdAt"
    );

    private final MobileDeviceMapper mobileDeviceMapper;
    private final MobileDeviceRepository mobileDeviceRepository;
    private final UserRepository userRepository;
    @Override
    public Page<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria, Pageable pageable) {
        Predicate predicate = MobileDeviceSpecification.buildPredicate(criteria);
        Page<MobileDevice> devicePage = mobileDeviceRepository.findAll(predicate, pageable);
        return devicePage.map(mobileDeviceMapper::toDTO);
    }
    @Override
    public List<MobileDeviceResponse> searchDevices(MobileDeviceSearchCriteria criteria) {
        Predicate predicate = MobileDeviceSpecification.buildPredicate(criteria);
        Iterable<MobileDevice> devices = mobileDeviceRepository.findAll(predicate);
        List<MobileDevice> deviceList = new ArrayList<>();
        devices.forEach(deviceList::add);
        return deviceList.stream()
                .map(mobileDeviceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MobileDeviceResponse createMobileDevice(CreateMobileDeviceRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, request.userId()));

        MobileDevice mobileDevice = mobileDeviceMapper.toEntity(request);
        mobileDevice.setUser(user);
        MobileDevice savedMobileDevice = mobileDeviceRepository.save(mobileDevice);

        return mobileDeviceMapper.toDTO(savedMobileDevice);
    }

    @Override
    public MobileDeviceResponse update(UUID id, UpdateMobileDeviceRequest request) {

        MobileDevice mobileDevice = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, id));

        mobileDeviceMapper.updateEntityFromDTO(request, mobileDevice);
        MobileDevice savedMobileDevice = mobileDeviceRepository.save(mobileDevice);

        return mobileDeviceMapper.toDTO(savedMobileDevice);
    }

    @Override
    public MobileDeviceResponse findById(UUID id) {
        MobileDevice mobileDevice = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, id));

        return mobileDeviceMapper.toDTO(mobileDevice);
    }

    @Override
    public Page<MobileDeviceResponse> findAll(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, PaginationUtil.parseSort(sort, VALID_SORT_FIELDS));

        Page<MobileDevice> mobileDevicePage = mobileDeviceRepository.findAll(pageable);

        return mobileDevicePage.map(mobileDeviceMapper::toDTO);
    }

    @Override
    public void delete(UUID id) {
        MobileDevice mobileDevice = mobileDeviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, id));
        mobileDevice.setIsActive(false);
        mobileDeviceRepository.save(mobileDevice);
    }
}
