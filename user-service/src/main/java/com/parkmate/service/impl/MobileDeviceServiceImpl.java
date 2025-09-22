package com.parkmate.service.impl;

import com.parkmate.dto.request.CreateMobileDeviceRequest;
import com.parkmate.dto.request.UpdateMobileDeviceRequest;
import com.parkmate.dto.response.MobileDeviceResponse;
import com.parkmate.entity.MobileDevice;
import com.parkmate.entity.User;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.mapper.MobileDeviceMapper;
import com.parkmate.repository.MobileDeviceRepository;
import com.parkmate.repository.UserRepository;
import com.parkmate.service.MobileDeviceService;
import com.parkmate.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

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
