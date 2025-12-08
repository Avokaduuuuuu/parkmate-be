package com.parkmate.device;

import com.parkmate.device.dto.req.DeviceCreateRequest;
import com.parkmate.device.dto.req.DeviceUpdateRequest;
import com.parkmate.device.dto.resp.DeviceResponse;
import com.parkmate.device.enums.DeviceStatus;
import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService{
    private final DeviceRepository deviceRepository;
    private final ParkingLotRepository parkingLotRepository;
    @Override
    public Page<DeviceResponse> getDevices(int page, int size, String sortBy, String sortOrder, DeviceFilterParams filterParams) {
        Page<DeviceEntity> deviceEntities = deviceRepository.findAll(filterParams.getSpecification(),PageRequest.of(page, size, Sort.Direction.valueOf(sortOrder), sortBy));
        return deviceEntities.map(DeviceMapper.INSTANCE::toResponse);
    }

    @Override
    public DeviceResponse getDeviceById(Long id) {
        return DeviceMapper.INSTANCE.toResponse(deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Device with id " + id + " not found")));
    }

    @Override
    @Transactional
    public List<DeviceResponse> createDevices(Long lotId, List<DeviceCreateRequest> requests) {

        ParkingLotEntity lotEntity = parkingLotRepository.findById(lotId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.PARKING_NOT_FOUND,
                        "Lot with id " + lotId + " not found"
                ));

        List<DeviceEntity> newDevices = requests.stream()
                .map(request -> {
                    if (deviceRepository.existsByDeviceId(request.deviceId()))
                        throw new AppException(ErrorCode.DEVICE_ID_EXISTS, "Device with device Id " + request.deviceId() + " exists");
                    DeviceEntity device = new DeviceEntity();
                    device.setParkingLot(lotEntity);
                    device.setDeviceId(request.deviceId());
                    device.setDeviceName(request.deviceName());
                    device.setDeviceType(request.deviceType());
                    device.setPartnerId(request.partnerId());
                    device.setModel(request.model());
                    device.setSerialNumber(request.serialNumber());
                    device.setStatus(DeviceStatus.ACTIVE);
                    device.setNotes(request.notes());
                    return device;
                })
                .collect(Collectors.toList()); // ✅ MUST be mutable

        // ✅ Append instead of override
        lotEntity.getDevices().addAll(newDevices);

        parkingLotRepository.save(lotEntity);

        return newDevices.stream()
                .map(DeviceMapper.INSTANCE::toResponse)
                .toList();
    }


    @Override
    public DeviceResponse updateDevice(Long id, DeviceUpdateRequest request) {
        DeviceEntity deviceEntity = deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Device with id " + id + " not found"));

        if (request.deviceId() != null) {
            if (deviceRepository.existsByDeviceId(request.deviceId())) {
                throw new AppException(ErrorCode.DEVICE_ID_EXISTS, "Device with device id " + request.deviceId() + " already exists");
            }
            deviceEntity.setDeviceId(request.deviceId());
        }
        if (request.deviceName() != null) {
            deviceEntity.setDeviceName(request.deviceName());
        }
        if (request.deviceStatus() != null) {
            deviceEntity.setStatus(request.deviceStatus());
        }
        if (request.model() != null) {
            deviceEntity.setModel(request.model());
        }
        if (request.serialNumber() != null) {
            deviceEntity.setSerialNumber(request.serialNumber());
        }
        if (request.notes() != null) {
            deviceEntity.setNotes(request.notes());
        }

        return DeviceMapper.INSTANCE.toResponse(deviceRepository.save(deviceEntity));
    }

    @Override
    public void deleteDevice(Long id) {
        DeviceEntity deviceEntity = deviceRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEVICE_NOT_FOUND, "Device with id " + id + " not found"));

        deviceRepository.delete(deviceEntity);
    }

    @Override
    public Long countDevices() {
        return deviceRepository.count();
    }
}
